package com.pedromoura.laprobqi.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.adapters.SolicitacaoAdapter;
import com.pedromoura.laprobqi.di.RepositoryProvider;
import com.pedromoura.laprobqi.models.SolicitacaoCoordenador;
import com.pedromoura.laprobqi.models.Usuario;
import com.pedromoura.laprobqi.repository.SolicitacaoCoordenadorRepository;
import com.pedromoura.laprobqi.repository.UsuarioRepository;
import com.pedromoura.laprobqi.repository.impl.SolicitacaoCoordenadorRepositoryFirestore;

import java.util.ArrayList;
import java.util.List;

public class AprovarSolicitacoesActivity extends AppCompatActivity {
    
    private RecyclerView recyclerSolicitacoes;
    private TextView txtEmpty;
    private ProgressBar progressBar;
    private ImageView btnConfig;
    
    private SolicitacaoCoordenadorRepository solicitacaoRepository;
    private UsuarioRepository usuarioRepository;
    private SolicitacaoAdapter adapter;
    private List<SolicitacaoCoordenador> solicitacoes;
    private Usuario usuarioAtual;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aprovar_solicitacoes);
        
        inicializarComponentes();
        carregarUsuarioAtual();
    }
    
    private void inicializarComponentes() {
        recyclerSolicitacoes = findViewById(R.id.recyclerSolicitacoes);
        txtEmpty = findViewById(R.id.txtEmpty);
        progressBar = findViewById(R.id.progressBar);
        btnConfig = findViewById(R.id.btnConfig);
        
        solicitacoes = new ArrayList<>();
        adapter = new SolicitacaoAdapter(this, solicitacoes, 
            this::mostrarDialogAprovar, this::mostrarDialogRejeitar);
        recyclerSolicitacoes.setLayoutManager(new LinearLayoutManager(this));
        recyclerSolicitacoes.setAdapter(adapter);
        
        solicitacaoRepository = new SolicitacaoCoordenadorRepositoryFirestore();
        usuarioRepository = RepositoryProvider.getInstance(this).getUsuarioRepository();
        
        btnConfig.setOnClickListener(v -> {
            Intent intent = new Intent(AprovarSolicitacoesActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
    
    private void carregarUsuarioAtual() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        usuarioRepository.obterUsuarioAtual(new UsuarioRepository.OnSuccessListener<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                usuarioAtual = usuario;
                
                if (!usuario.isCoordenador()) {
                    Toast.makeText(AprovarSolicitacoesActivity.this, 
                        "Apenas coordenadores podem aprovar solicitações", 
                        Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                
                carregarSolicitacoes();
            }
        });
    }
    
    private void carregarSolicitacoes() {
        progressBar.setVisibility(View.VISIBLE);
        
        solicitacaoRepository.listarSolicitacoesPendentes(
            new SolicitacaoCoordenadorRepository.OnListSolicitacoesListener() {
                @Override
                public void onSuccess(List<SolicitacaoCoordenador> lista) {
                    progressBar.setVisibility(View.GONE);
                    solicitacoes.clear();
                    solicitacoes.addAll(lista);
                    adapter.notifyDataSetChanged();
                    
                    if (lista.isEmpty()) {
                        txtEmpty.setVisibility(View.VISIBLE);
                        recyclerSolicitacoes.setVisibility(View.GONE);
                    } else {
                        txtEmpty.setVisibility(View.GONE);
                        recyclerSolicitacoes.setVisibility(View.VISIBLE);
                    }
                }
                
                @Override
                public void onFailure(String mensagem) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AprovarSolicitacoesActivity.this, mensagem, 
                        Toast.LENGTH_LONG).show();
                }
            });
    }
    
    private void mostrarDialogAprovar(SolicitacaoCoordenador solicitacao) {
        new AlertDialog.Builder(this)
            .setTitle("Aprovar Solicitação")
            .setMessage("Deseja aprovar o cadastro de " + solicitacao.getNome() + 
                       " como coordenador?")
            .setPositiveButton("Aprovar", (dialog, which) -> {
                aprovarSolicitacao(solicitacao);
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    private void mostrarDialogRejeitar(SolicitacaoCoordenador solicitacao) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rejeitar_solicitacao, null);
        EditText editMotivo = dialogView.findViewById(R.id.editMotivo);
        
        new AlertDialog.Builder(this)
            .setTitle("Rejeitar Solicitação")
            .setMessage("Informe o motivo da rejeição:")
            .setView(dialogView)
            .setPositiveButton("Rejeitar", (dialog, which) -> {
                String motivo = editMotivo.getText().toString().trim();
                if (motivo.isEmpty()) {
                    Toast.makeText(this, "Informe o motivo da rejeição", 
                        Toast.LENGTH_SHORT).show();
                } else {
                    rejeitarSolicitacao(solicitacao, motivo);
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    private void aprovarSolicitacao(SolicitacaoCoordenador solicitacao) {
        progressBar.setVisibility(View.VISIBLE);
        
        solicitacaoRepository.aprovarSolicitacao(
            solicitacao.getId(),
            usuarioAtual.getId(),
            usuarioAtual.getNome(),
            new SolicitacaoCoordenadorRepository.OnCompleteListener() {
                @Override
                public void onComplete(boolean sucesso, String mensagem) {
                    progressBar.setVisibility(View.GONE);
                    
                    if (sucesso) {
                        new AlertDialog.Builder(AprovarSolicitacoesActivity.this)
                            .setTitle("✅ Solicitação Aprovada")
                            .setMessage(solicitacao.getNome() + " foi aprovado como coordenador!\n\n" +
                                      "📧 O usuário deve ser notificado para criar sua conta com o email:\n" +
                                      solicitacao.getEmail() + "\n\n" +
                                      "Ele poderá se registrar normalmente e o sistema identificará " +
                                      "automaticamente como coordenador aprovado.")
                            .setPositiveButton("OK", (dialog, which) -> {
                                carregarSolicitacoes();
                            })
                            .show();
                    } else {
                        Toast.makeText(AprovarSolicitacoesActivity.this, 
                            mensagem, Toast.LENGTH_LONG).show();
                    }
                }
            });
    }
    
    private void rejeitarSolicitacao(SolicitacaoCoordenador solicitacao, String motivo) {
        progressBar.setVisibility(View.VISIBLE);
        
        solicitacaoRepository.rejeitarSolicitacao(
            solicitacao.getId(),
            motivo,
            new SolicitacaoCoordenadorRepository.OnCompleteListener() {
                @Override
                public void onComplete(boolean sucesso, String mensagem) {
                    progressBar.setVisibility(View.GONE);
                    
                    if (sucesso) {
                        Toast.makeText(AprovarSolicitacoesActivity.this, 
                            "❌ " + mensagem, Toast.LENGTH_SHORT).show();
                        carregarSolicitacoes();
                    } else {
                        Toast.makeText(AprovarSolicitacoesActivity.this, 
                            mensagem, Toast.LENGTH_LONG).show();
                    }
                }
            });
    }
}
