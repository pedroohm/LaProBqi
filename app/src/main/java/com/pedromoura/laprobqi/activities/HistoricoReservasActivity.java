package com.pedromoura.laprobqi.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.adapters.LogReservaAdapter;
import com.pedromoura.laprobqi.models.LogReserva;
import com.pedromoura.laprobqi.models.Reserva;
import com.pedromoura.laprobqi.models.Usuario;
import com.pedromoura.laprobqi.repositories.LogReservaRepository;
import com.pedromoura.laprobqi.repositories.LogReservaRepositoryFirestore;
import com.pedromoura.laprobqi.repository.ReservaRepository;
import com.pedromoura.laprobqi.repository.UsuarioRepository;
import com.pedromoura.laprobqi.repository.impl.UsuarioRepositoryFirebase;
import com.pedromoura.laprobqi.di.RepositoryProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HistoricoReservasActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private AutoCompleteTextView spinnerFiltroStatus;
    private TextInputEditText editBusca;
    private MaterialButton btnAplicarFiltros, btnLimparFiltros;
    private RecyclerView recyclerLogs;
    private View layoutEmpty;

    // private LogReservaRepository logRepository; // Removido
    private UsuarioRepository usuarioRepository;
    private ReservaRepository reservaRepository;
    private LogReservaAdapter adapter;
    private List<LogReserva> todosLogs = new ArrayList<>();
    private List<LogReserva> logsFiltrados = new ArrayList<>();

    private Usuario usuarioAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico_reservas);

        inicializarRepositorios();
        inicializarViews();
        configurarToolbar();
        carregarUsuario();
        configurarFiltros();
        configurarRecyclerView();
        // migrarReservasExistentes(); // Removido
    }

    private void inicializarRepositorios() {
        // logRepository = new LogReservaRepositoryFirestore(); // Removido
        usuarioRepository = new UsuarioRepositoryFirebase();
        reservaRepository = RepositoryProvider.getInstance(this).getReservaRepository();
    }

    private void inicializarViews() {
        toolbar = findViewById(R.id.toolbar);
        spinnerFiltroStatus = findViewById(R.id.spinnerFiltroStatus);
        editBusca = findViewById(R.id.editBusca);
        btnAplicarFiltros = findViewById(R.id.btnAplicarFiltros);
        btnLimparFiltros = findViewById(R.id.btnLimparFiltros);
        recyclerLogs = findViewById(R.id.recyclerLogs);
        layoutEmpty = findViewById(R.id.layoutEmpty);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void carregarUsuario() {
        usuarioRepository.obterUsuarioAtual(new UsuarioRepository.OnSuccessListener<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                usuarioAtual = usuario;
                carregarLogs();
            }
        });
    }

    private void configurarFiltros() {
        // Configurar dropdown de status
        String[] statusOptions = {"Todas", "Ativas", "Concluídas", "Canceladas"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, statusOptions);
        spinnerFiltroStatus.setAdapter(statusAdapter);
        spinnerFiltroStatus.setText("Todas", false);

        // Botão aplicar filtros
        btnAplicarFiltros.setOnClickListener(v -> aplicarFiltros());

        // Botão limpar filtros
        btnLimparFiltros.setOnClickListener(v -> limparFiltros());

        // Busca em tempo real
        editBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                aplicarFiltros();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void configurarRecyclerView() {
        adapter = new LogReservaAdapter(this, logsFiltrados);
        recyclerLogs.setLayoutManager(new LinearLayoutManager(this));
        recyclerLogs.setAdapter(adapter);
    }

    private void carregarLogs() {
        reservaRepository.obterTodasReservas(new ReservaRepository.OnReservasListener() {
            @Override
            public void onSuccess(List<Reserva> reservas) {
                todosLogs.clear();
                for (Reserva reserva : reservas) {
                    todosLogs.add(converterReservaParaLog(reserva));
                }
                aplicarFiltros();
            }

            @Override
            public void onFailure(String mensagem) {
                Toast.makeText(HistoricoReservasActivity.this,
                        "Erro ao carregar reservas: " + mensagem,
                        Toast.LENGTH_SHORT).show();
                atualizarVisibilidadeEmpty();
            }
        });
    }

    private LogReserva converterReservaParaLog(Reserva reserva) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
            String dataHoraInicioStr = reserva.getDataReserva() + " " + reserva.getHoraInicio();
            String dataHoraFimStr = reserva.getDataReserva() + " " + reserva.getHoraFim();

            java.util.Date dataHoraInicio = sdf.parse(dataHoraInicioStr);
            java.util.Date dataHoraFim = sdf.parse(dataHoraFimStr);

            // Determinar status baseado na data atual se não tiver status definido
            String status = "ATIVA"; // Default
            // Se a reserva tiver status, use-o. Se não, calcule.
            // Assumindo que Reserva não tem getStatus() exposto ou confiável, calculamos:
            java.util.Date agora = new java.util.Date();
            if (dataHoraFim != null && dataHoraFim.before(agora)) {
                status = "CONCLUIDA";
            }

            return new LogReserva(
                    reserva.getEquipamentoId(),
                    reserva.getEquipamentoNome(),
                    reserva.getUsuarioId(),
                    reserva.getUsuarioNome(),
                    "", // Email não disponível na reserva
                    dataHoraInicio,
                    dataHoraFim,
                    status
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void aplicarFiltros() {
        String statusSelecionado = spinnerFiltroStatus.getText().toString();
        String textoBusca = editBusca.getText().toString().toLowerCase().trim();

        logsFiltrados.clear();

        for (LogReserva log : todosLogs) {
            // Filtro por status
            boolean statusMatch = false;
            switch (statusSelecionado) {
                case "Todas":
                    statusMatch = true;
                    break;
                case "Ativas":
                    statusMatch = log.isAtiva();
                    break;
                case "Concluídas":
                    statusMatch = log.isConcluida();
                    break;
                case "Canceladas":
                    statusMatch = log.isCancelada();
                    break;
            }

            // Filtro por busca
            boolean buscaMatch = textoBusca.isEmpty() ||
                    log.getEquipamentoNome().toLowerCase().contains(textoBusca) ||
                    log.getUsuarioNome().toLowerCase().contains(textoBusca) ||
                    log.getUsuarioEmail().toLowerCase().contains(textoBusca);

            if (statusMatch && buscaMatch) {
                logsFiltrados.add(log);
            }
        }

        adapter.notifyDataSetChanged();
        atualizarVisibilidadeEmpty();
    }

    private void limparFiltros() {
        spinnerFiltroStatus.setText("Todas", false);
        editBusca.setText("");
        aplicarFiltros();
    }

    private void atualizarVisibilidadeEmpty() {
        if (logsFiltrados.isEmpty()) {
            recyclerLogs.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerLogs.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }
}
