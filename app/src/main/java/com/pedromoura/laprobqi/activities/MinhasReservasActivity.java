package com.pedromoura.laprobqi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.di.RepositoryProvider;
import com.pedromoura.laprobqi.models.Reserva;
import com.pedromoura.laprobqi.models.Usuario;
import com.pedromoura.laprobqi.repository.ReservaRepository;
import com.pedromoura.laprobqi.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;

public class MinhasReservasActivity extends AppCompatActivity {

    private ListView listViewReservas;
    private Button btnNovaReserva, btnVoltar;
    private ProgressBar progressBar;
    
    private ReservaRepository reservaRepository;
    private UsuarioRepository usuarioRepository;
    private Usuario usuarioAtual;
    private List<Reserva> reservas;
    private android.widget.ArrayAdapter<Reserva> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minhas_reservas);

        // Inicializar repositories
        reservaRepository = RepositoryProvider.getInstance(this).getReservaRepository();
        usuarioRepository = RepositoryProvider.getInstance(this).getUsuarioRepository();

        // Inicializar views
        inicializarViews();
        
        // Carregar usuário atual (que depois carregará as reservas)
        carregarUsuarioAtual();
    }

    private void inicializarViews() {
        // Configurar header
        ImageView configLogo = findViewById(R.id.configLogo);
        configLogo.setOnClickListener(v -> {
            Intent intent = new Intent(MinhasReservasActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        listViewReservas = findViewById(R.id.listViewReservas);
        btnNovaReserva = findViewById(R.id.btnNovaReserva);
        btnVoltar = findViewById(R.id.btnVoltar);
        progressBar = findViewById(R.id.progressBar);

        // Configurar listeners
        btnNovaReserva.setOnClickListener(v -> abrirTelaReservar());
        btnVoltar.setOnClickListener(v -> finish());
        
        listViewReservas.setOnItemClickListener(this::onReservaClick);
        listViewReservas.setOnItemLongClickListener(this::onReservaLongClick);
    }

    private void carregarUsuarioAtual() {
        usuarioRepository.obterUsuarioAtual(usuario -> {
            usuarioAtual = usuario;
            // Carregar reservas DEPOIS que o usuário for obtido
            carregarReservas();
        });
    }

    private void carregarReservas() {
        if (usuarioAtual == null) {
            showToast("Usuário não encontrado");
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        reservaRepository.obterReservasPorUsuario(usuarioAtual.getId(), new ReservaRepository.OnReservasListener() {
            @Override
            public void onSuccess(List<Reserva> reservasList) {
                progressBar.setVisibility(View.GONE);
                reservas = reservasList;
                atualizarLista();
            }

            @Override
            public void onFailure(String mensagem) {
                progressBar.setVisibility(View.GONE);
                showToast("Erro ao carregar reservas: " + mensagem);
            }
        });
    }

    private void atualizarLista() {
        if (reservas != null) {
            adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_list_item_2, 
                android.R.id.text1, reservas) {
                @Override
                public View getView(int position, View convertView, android.view.ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    Reserva reserva = getItem(position);
                    
                    android.widget.TextView text1 = view.findViewById(android.R.id.text1);
                    android.widget.TextView text2 = view.findViewById(android.R.id.text2);
                    
                    text1.setText(reserva.getEquipamentoNome());
                    text2.setText(reserva.getDataReserva() + " - " + reserva.getPeriodoReserva() + 
                                " (" + reserva.getStatusDisplay() + ")");
                    
                    return view;
                }
            };
            
            listViewReservas.setAdapter(adapter);
        }
    }

    private void onReservaClick(AdapterView<?> parent, View view, int position, long id) {
        Reserva reserva = (Reserva) parent.getItemAtPosition(position);
        if (reserva != null) {
            abrirDetalhesReserva(reserva);
        }
    }

    private boolean onReservaLongClick(AdapterView<?> parent, View view, int position, long id) {
        Reserva reserva = (Reserva) parent.getItemAtPosition(position);
        if (reserva != null && reserva.isAtiva()) {
            mostrarOpcoesReserva(reserva);
            return true;
        }
        return false;
    }

    private void abrirDetalhesReserva(Reserva reserva) {
        // Implementar tela de detalhes da reserva
        showToast("Detalhes da reserva: " + reserva.getEquipamentoNome());
    }

    private void mostrarOpcoesReserva(Reserva reserva) {
        String[] opcoes = {"Cancelar Reserva"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opções da Reserva");
        builder.setItems(opcoes, (dialog, which) -> {
            switch (which) {
                case 0:
                    cancelarReserva(reserva);
                    break;
            }
        });
        builder.show();
    }

    private void cancelarReserva(Reserva reserva) {
        new AlertDialog.Builder(this)
            .setTitle("Cancelar Reserva")
            .setMessage("Tem certeza que deseja cancelar esta reserva?")
            .setPositiveButton("Sim", (dialog, which) -> {
                progressBar.setVisibility(View.VISIBLE);
                reservaRepository.cancelarReserva(reserva.getId(), new ReservaRepository.OnBooleanListener() {
                    @Override
                    public void onSuccess(boolean success) {
                        progressBar.setVisibility(View.GONE);
                        if (success) {
                            showToast("Reserva cancelada com sucesso!");
                            carregarReservas(); // Recarregar lista
                        } else {
                            showToast("Erro ao cancelar reserva");
                        }
                    }

                    @Override
                    public void onFailure(String mensagem) {
                        progressBar.setVisibility(View.GONE);
                        showToast("Erro: " + mensagem);
                    }
                });
            })
            .setNegativeButton("Não", null)
            .show();
    }

    private void abrirTelaReservar() {
        Intent intent = new Intent(this, ReservarEquipamentoActivity.class);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarregar reservas quando retornar à tela
        if (usuarioAtual != null) {
            carregarReservas();
        }
    }
}
