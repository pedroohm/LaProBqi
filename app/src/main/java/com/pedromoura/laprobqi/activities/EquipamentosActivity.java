package com.pedromoura.laprobqi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.di.RepositoryProvider;
import com.pedromoura.laprobqi.models.Equipamento;
import com.pedromoura.laprobqi.models.Usuario;
import com.pedromoura.laprobqi.repository.EquipamentoRepository;
import com.pedromoura.laprobqi.repository.UsuarioRepository;
import com.pedromoura.laprobqi.EquipamentoAdapter;

import java.util.ArrayList;
import java.util.List;

public class EquipamentosActivity extends AppCompatActivity {

    private ListView listViewEquipamentos;
    private EditText editTextBusca;
    private Button btnBuscar, btnAdicionarEquipamento, btnReservar, btnMinhasReservas;
    private Spinner spinnerFiltro;
    private ProgressBar progressBar;
    
    private EquipamentoRepository equipamentoRepository;
    private UsuarioRepository usuarioRepository;
    private Usuario usuarioAtual;
    private List<Equipamento> equipamentos;
    private EquipamentoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipamentos);

        // Inicializar repositories
        equipamentoRepository = RepositoryProvider.getInstance(this).getEquipamentoRepository();
        usuarioRepository = RepositoryProvider.getInstance(this).getUsuarioRepository();

        // Inicializar views
        inicializarViews();
        
        // Configurar spinner de filtro
        configurarSpinnerFiltro();
        
        // Carregar usuário atual
        carregarUsuarioAtual();
        
        // Carregar equipamentos
        carregarEquipamentos();
    }

    private void inicializarViews() {
        // Configurar header
        ImageView configLogo = findViewById(R.id.configLogo);
        configLogo.setOnClickListener(v -> {
            Intent intent = new Intent(EquipamentosActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        listViewEquipamentos = findViewById(R.id.listViewEquipamentos);
        editTextBusca = findViewById(R.id.editTextBusca);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnAdicionarEquipamento = findViewById(R.id.btnAdicionarEquipamento);
        btnReservar = findViewById(R.id.btnReservar);
        btnMinhasReservas = findViewById(R.id.btnMinhasReservas);
        spinnerFiltro = findViewById(R.id.spinnerFiltro);
        progressBar = findViewById(R.id.progressBar);

        // Configurar listeners
        btnBuscar.setOnClickListener(v -> buscarEquipamentos());
        btnAdicionarEquipamento.setOnClickListener(v -> abrirTelaAdicionarEquipamento());
        btnReservar.setOnClickListener(v -> abrirTelaReservar());
        btnMinhasReservas.setOnClickListener(v -> abrirTelaMinhasReservas());
        
        listViewEquipamentos.setOnItemClickListener(this::onEquipamentoClick);
    }

    private void configurarSpinnerFiltro() {
        String[] opcoesFiltro = {"Todos", "Disponíveis", "Reservados", "Em Uso"};
        ArrayAdapter<String> adapterFiltro = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, opcoesFiltro);
        adapterFiltro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltro.setAdapter(adapterFiltro);
        
        spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filtrarEquipamentos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void carregarUsuarioAtual() {
        usuarioRepository.obterUsuarioAtual(usuario -> {
            usuarioAtual = usuario;
            if (usuario != null) {
                // Mostrar/ocultar botão de adicionar equipamento baseado no nível de acesso
                btnAdicionarEquipamento.setVisibility(usuario.isCoordenador() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void carregarEquipamentos() {
        progressBar.setVisibility(View.VISIBLE);
        equipamentoRepository.obterTodosEquipamentos(new EquipamentoRepository.OnEquipamentosListener() {
            @Override
            public void onSuccess(List<Equipamento> equipamentosList) {
                progressBar.setVisibility(View.GONE);
                equipamentos = equipamentosList;
                atualizarLista();
            }

            @Override
            public void onFailure(String mensagem) {
                progressBar.setVisibility(View.GONE);
                showToast("Erro ao carregar equipamentos: " + mensagem);
            }
        });
    }

    private void buscarEquipamentos() {
        String termoBusca = editTextBusca.getText().toString().trim();
        if (termoBusca.isEmpty()) {
            carregarEquipamentos();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            equipamentoRepository.buscarEquipamentosPorNome(termoBusca, new EquipamentoRepository.OnEquipamentosListener() {
                @Override
                public void onSuccess(List<Equipamento> equipamentosList) {
                    progressBar.setVisibility(View.GONE);
                    equipamentos = equipamentosList;
                    atualizarLista();
                }

                @Override
                public void onFailure(String mensagem) {
                    progressBar.setVisibility(View.GONE);
                    showToast("Erro na busca: " + mensagem);
                }
            });
        }
    }

    private void filtrarEquipamentos() {
        if (equipamentos == null) return;
        
        String filtro = spinnerFiltro.getSelectedItem().toString();
        List<Equipamento> equipamentosFiltrados = new ArrayList<>();
        
        for (Equipamento equipamento : equipamentos) {
            switch (filtro) {
                case "Todos":
                    equipamentosFiltrados.add(equipamento);
                    break;
                case "Disponíveis":
                    if (equipamento.isDisponivel()) {
                        equipamentosFiltrados.add(equipamento);
                    }
                    break;
                case "Reservados":
                    if (equipamento.isReservado()) {
                        equipamentosFiltrados.add(equipamento);
                    }
                    break;
                case "Em Uso":
                    if (equipamento.isEmUso()) {
                        equipamentosFiltrados.add(equipamento);
                    }
                    break;
            }
        }
        
        adapter = new EquipamentoAdapter(this, equipamentosFiltrados);
        listViewEquipamentos.setAdapter(adapter);
    }

    private void atualizarLista() {
        filtrarEquipamentos();
    }

    private void onEquipamentoClick(AdapterView<?> parent, View view, int position, long id) {
        Equipamento equipamento = (Equipamento) parent.getItemAtPosition(position);
        if (equipamento != null) {
            abrirDetalhesEquipamento(equipamento);
        }
    }

    private void abrirDetalhesEquipamento(Equipamento equipamento) {
        Intent intent = new Intent(this, DetalhesEquipamentoActivity.class);
        intent.putExtra("equipamento", equipamento);
        startActivity(intent);
    }

    private void abrirTelaAdicionarEquipamento() {
        if (usuarioAtual != null && usuarioAtual.isCoordenador()) {
            Intent intent = new Intent(this, AdicionarEquipamentoActivity.class);
            startActivity(intent);
        } else {
            showToast("Apenas coordenadores podem adicionar equipamentos");
        }
    }

    private void abrirTelaReservar() {
        Intent intent = new Intent(this, ReservarEquipamentoActivity.class);
        startActivity(intent);
    }

    private void abrirTelaMinhasReservas() {
        Intent intent = new Intent(this, MinhasReservasActivity.class);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarregar equipamentos quando retornar à tela
        carregarEquipamentos();
    }
}
