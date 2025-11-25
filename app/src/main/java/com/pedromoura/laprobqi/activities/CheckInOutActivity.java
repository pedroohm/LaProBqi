package com.pedromoura.laprobqi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.adapters.PresencaLabAdapter;
import com.pedromoura.laprobqi.models.PresencaLab;
import com.pedromoura.laprobqi.models.Usuario;
import com.pedromoura.laprobqi.repository.PresencaLabRepository;
import com.pedromoura.laprobqi.repository.UsuarioRepository;
import com.pedromoura.laprobqi.repository.impl.PresencaLabRepositoryFirestore;
import com.pedromoura.laprobqi.repository.impl.UsuarioRepositoryFirebase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckInOutActivity extends AppCompatActivity {
    
    private static final String TAG = "CheckInOutActivity";
    
    private TextView txtStatus, txtDetalhes, txtEmptyHistorico;
    private Button btnCheckIn, btnCheckOut;
    private RecyclerView recyclerHistorico;
    private ImageView btnConfig;
    
    private PresencaLabRepository presencaRepository;
    private UsuarioRepository usuarioRepository;
    private PresencaLabAdapter adapter;
    private List<PresencaLab> presencas;
    
    private FirebaseUser currentUser;
    private Usuario usuario;
    private PresencaLab presencaAtiva;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_out);
        
        inicializarComponentes();
        configurarListeners();
        carregarUsuario();
    }
    
    private void inicializarComponentes() {
        txtStatus = findViewById(R.id.txtStatus);
        txtDetalhes = findViewById(R.id.txtDetalhes);
        txtEmptyHistorico = findViewById(R.id.txtEmptyHistorico);
        btnCheckIn = findViewById(R.id.btnCheckIn);
        btnCheckOut = findViewById(R.id.btnCheckOut);
        btnConfig = findViewById(R.id.btnConfig);
        recyclerHistorico = findViewById(R.id.recyclerHistorico);
        
        presencas = new ArrayList<>();
        adapter = new PresencaLabAdapter(this, presencas);
        recyclerHistorico.setLayoutManager(new LinearLayoutManager(this));
        recyclerHistorico.setAdapter(adapter);
        
        presencaRepository = new PresencaLabRepositoryFirestore();
        usuarioRepository = new UsuarioRepositoryFirebase();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }
    
    private void configurarListeners() {
        btnCheckIn.setOnClickListener(v -> realizarCheckIn());
        btnCheckOut.setOnClickListener(v -> realizarCheckOut());
        
        btnConfig.setOnClickListener(v -> {
            Intent intent = new Intent(CheckInOutActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
    
    private void carregarUsuario() {
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        usuarioRepository.obterUsuarioAtual(new UsuarioRepository.OnSuccessListener<Usuario>() {
            @Override
            public void onSuccess(Usuario u) {
                usuario = u;
                verificarPresencaAtiva();
                carregarHistorico();
            }
        });
    }
    
    private void verificarPresencaAtiva() {
        presencaRepository.buscarPresencaAtiva(currentUser.getUid(), new PresencaLabRepository.OnPresencaListener() {
            @Override
            public void onSuccess(PresencaLab presenca) {
                presencaAtiva = presenca;
                atualizarUI(true);
            }
            
            @Override
            public void onNotFound() {
                presencaAtiva = null;
                atualizarUI(false);
            }
            
            @Override
            public void onFailure(String mensagem) {
                Log.e(TAG, "Erro ao verificar presença: " + mensagem);
                atualizarUI(false);
            }
        });
    }
    
    private void atualizarUI(boolean presente) {
        if (presente && presencaAtiva != null) {
            txtStatus.setText("✅ Você está no laboratório");
            txtDetalhes.setText("Check-in: " + formatarData(presencaAtiva.getDataEntrada()) + 
                              " às " + presencaAtiva.getHoraEntrada());
            txtDetalhes.setVisibility(View.VISIBLE);
            btnCheckIn.setEnabled(false);
            btnCheckOut.setEnabled(true);
        } else {
            txtStatus.setText("❌ Você não está no laboratório");
            txtDetalhes.setVisibility(View.GONE);
            btnCheckIn.setEnabled(true);
            btnCheckOut.setEnabled(false);
        }
    }
    
    private void realizarCheckIn() {
        if (usuario == null) {
            Toast.makeText(this, "Erro: usuário não carregado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date now = new Date();
        
        String data = dateFormat.format(now);
        String hora = timeFormat.format(now);
        
        PresencaLab presenca = new PresencaLab(
            currentUser.getUid(),
            usuario.getNome(),
            usuario.getEmail(),
            data,
            hora
        );
        
        btnCheckIn.setEnabled(false);
        presencaRepository.registrarEntrada(presenca, (sucesso, mensagem) -> {
            if (sucesso) {
                Toast.makeText(CheckInOutActivity.this, "✅ Check-in realizado!", Toast.LENGTH_SHORT).show();
                verificarPresencaAtiva();
                carregarHistorico();
            } else {
                Toast.makeText(CheckInOutActivity.this, mensagem, Toast.LENGTH_LONG).show();
                btnCheckIn.setEnabled(true);
            }
        });
    }
    
    private void realizarCheckOut() {
        if (presencaAtiva == null) {
            Toast.makeText(this, "Erro: nenhuma presença ativa encontrada", Toast.LENGTH_SHORT).show();
            return;
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date now = new Date();
        
        String data = dateFormat.format(now);
        String hora = timeFormat.format(now);
        
        btnCheckOut.setEnabled(false);
        presencaRepository.registrarSaida(presencaAtiva.getId(), data, hora, (sucesso, mensagem) -> {
            if (sucesso) {
                Toast.makeText(CheckInOutActivity.this, "🚪 Check-out realizado!", Toast.LENGTH_SHORT).show();
                presencaAtiva = null;
                atualizarUI(false);
                carregarHistorico();
            } else {
                Toast.makeText(CheckInOutActivity.this, mensagem, Toast.LENGTH_LONG).show();
                btnCheckOut.setEnabled(true);
            }
        });
    }
    
    private void carregarHistorico() {
        if (currentUser == null) return;
        
        // Buscar últimos 30 dias
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date now = new Date();
        Date inicio = new Date(now.getTime() - (30L * 24 * 60 * 60 * 1000)); // 30 dias atrás
        
        String dataInicio = dateFormat.format(inicio);
        String dataFim = dateFormat.format(now);
        
        presencaRepository.listarPresencasPorPeriodo(dataInicio, dataFim, new PresencaLabRepository.OnListPresencasListener() {
            @Override
            public void onSuccess(List<PresencaLab> todasPresencas) {
                // Filtrar apenas as presenças do usuário atual
                List<PresencaLab> minhasPresencas = new ArrayList<>();
                for (PresencaLab p : todasPresencas) {
                    if (p.getUsuarioId().equals(currentUser.getUid())) {
                        minhasPresencas.add(p);
                    }
                }
                
                presencas.clear();
                presencas.addAll(minhasPresencas);
                adapter.notifyDataSetChanged();
                
                if (minhasPresencas.isEmpty()) {
                    txtEmptyHistorico.setVisibility(View.VISIBLE);
                    recyclerHistorico.setVisibility(View.GONE);
                } else {
                    txtEmptyHistorico.setVisibility(View.GONE);
                    recyclerHistorico.setVisibility(View.VISIBLE);
                }
            }
            
            @Override
            public void onFailure(String mensagem) {
                Log.e(TAG, "Erro ao carregar histórico: " + mensagem);
            }
        });
    }
    
    private String formatarData(String data) {
        if (data == null || data.isEmpty()) return "";
        try {
            String[] partes = data.split("-");
            if (partes.length == 3) {
                return partes[2] + "/" + partes[1] + "/" + partes[0];
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao formatar data", e);
        }
        return data;
    }
}
