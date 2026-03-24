package com.pedromoura.laprobqi.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
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
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    private static final double LAB_LATITUDE = -20.761104530047522;
    private static final double LAB_LONGITUDE = -42.86504495256774;
    private static final double RAIO_PERMITIDO_METROS = 50.0;
    
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
    private FusedLocationProviderClient fusedLocationClient;
    
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
        
        if (!verificarPermissoesLocalizacao()) {
            solicitarPermissoesLocalizacao();
            return;
        }
        
        verificarLocalizacao();
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
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date now = new Date();
        Date inicio = new Date(now.getTime() - (30L * 24 * 60 * 60 * 1000));
        
        String dataInicio = dateFormat.format(inicio);
        String dataFim = dateFormat.format(now);
        
        presencaRepository.listarPresencasPorPeriodo(dataInicio, dataFim, new PresencaLabRepository.OnListPresencasListener() {
            @Override
            public void onSuccess(List<PresencaLab> todasPresencas) {
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
    
    private boolean verificarPermissoesLocalizacao() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    private void solicitarPermissoesLocalizacao() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                             Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                realizarCheckIn();
            } else {
                Toast.makeText(this, "Permissão de localização necessária para registrar presença", 
                              Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void verificarLocalizacao() {
        if (!verificarPermissoesLocalizacao()) {
            Toast.makeText(this, "Permissão de localização não concedida", Toast.LENGTH_SHORT).show();
            return;
        }
        
        btnCheckIn.setEnabled(false);
        Toast.makeText(this, "Verificando localização...", Toast.LENGTH_SHORT).show();
        
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, 
                    cancellationTokenSource.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double distancia = calcularDistancia(location.getLatitude(), 
                                                            location.getLongitude());
                        Log.d(TAG, "Distância do laboratório: " + distancia + " metros");
                        
                        if (distancia <= RAIO_PERMITIDO_METROS) {
                            processarCheckIn();
                        } else {
                            Toast.makeText(CheckInOutActivity.this, 
                                    String.format("❌ Você precisa estar no laboratório para registrar presença.\n" +
                                                "Distância: %.0f metros (máximo: %.0f metros)",
                                                distancia, RAIO_PERMITIDO_METROS),
                                    Toast.LENGTH_LONG).show();
                            btnCheckIn.setEnabled(true);
                        }
                    } else {
                        Toast.makeText(CheckInOutActivity.this, 
                                "Não foi possível obter sua localização. Tente novamente.", 
                                Toast.LENGTH_LONG).show();
                        btnCheckIn.setEnabled(true);
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Erro ao obter localização", e);
                    Toast.makeText(CheckInOutActivity.this, 
                            "Erro ao verificar localização: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    btnCheckIn.setEnabled(true);
                });
        } catch (SecurityException e) {
            Log.e(TAG, "Erro de permissão ao acessar localização", e);
            Toast.makeText(this, "Erro de permissão de localização", Toast.LENGTH_SHORT).show();
            btnCheckIn.setEnabled(true);
        }
    }
    
    private double calcularDistancia(double lat1, double lon1) {
        double lat2 = LAB_LATITUDE;
        double lon2 = LAB_LONGITUDE;
        
        final int RAIO_TERRA_METROS = 6371000;
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return RAIO_TERRA_METROS * c;
    }
    
    private void processarCheckIn() {
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
}
