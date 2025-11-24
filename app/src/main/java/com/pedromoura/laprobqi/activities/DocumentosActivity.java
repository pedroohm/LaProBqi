package com.pedromoura.laprobqi.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.model.ConfiguracaoDocumentos;
import com.pedromoura.laprobqi.repository.DocumentoRepository;
import com.pedromoura.laprobqi.repository.impl.DocumentoRepositoryFirestore;

/**
 * Activity para visualizar e acessar documentos (POPs e Manuais) no Google Drive.
 */
public class DocumentosActivity extends AppCompatActivity {

    private DocumentoRepository documentoRepository;
    private TextView textInstrucoes;
    private TextView textUltimaAtualizacao;
    private TextView textAtualizadoPor;
    private MaterialButton btnAbrirPasta;
    private ImageView btnConfigurar;
    private View layoutVazio;
    private ConfiguracaoDocumentos configuracaoAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentos);

        inicializarComponentes();
        configurarListeners();
        verificarPermissaoCoordenador();
        carregarConfiguracao();
    }

    private void inicializarComponentes() {
        documentoRepository = new DocumentoRepositoryFirestore();
        
        textInstrucoes = findViewById(R.id.textInstrucoes);
        textUltimaAtualizacao = findViewById(R.id.textUltimaAtualizacao);
        textAtualizadoPor = findViewById(R.id.textAtualizadoPor);
        btnAbrirPasta = findViewById(R.id.btnAbrirPasta);
        btnConfigurar = findViewById(R.id.btnConfigurar);
        layoutVazio = findViewById(R.id.layoutVazio);
        
        ImageView btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());
    }

    private void configurarListeners() {
        btnAbrirPasta.setOnClickListener(v -> abrirPastaGoogleDrive());
        btnConfigurar.setOnClickListener(v -> abrirTelaConfiguracao());
    }

    /**
     * Verifica se o usuário atual é coordenador para mostrar botão de configuração.
     */
    private void verificarPermissaoCoordenador() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // TODO: Verificar tipo de usuário no Firestore
            // Por enquanto, vamos mostrar para todos (remover depois)
            btnConfigurar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Carrega as configurações dos documentos do Firestore.
     */
    private void carregarConfiguracao() {
        documentoRepository.obterConfiguracao(new DocumentoRepository.OnSuccessListener<ConfiguracaoDocumentos>() {
            @Override
            public void onSuccess(ConfiguracaoDocumentos config) {
                configuracaoAtual = config;
                
                if (config.getUrlPastaGoogleDrive() == null || config.getUrlPastaGoogleDrive().isEmpty()) {
                    // Mostrar estado vazio
                    layoutVazio.setVisibility(View.VISIBLE);
                    btnAbrirPasta.setEnabled(false);
                } else {
                    // Mostrar informações
                    layoutVazio.setVisibility(View.GONE);
                    btnAbrirPasta.setEnabled(true);
                    
                    if (config.getInstrucoes() != null && !config.getInstrucoes().isEmpty()) {
                        textInstrucoes.setText(config.getInstrucoes());
                    }
                    
                    if (config.getUltimaAtualizacao() != null && !config.getUltimaAtualizacao().isEmpty()) {
                        textUltimaAtualizacao.setText("Atualizado em: " + config.getUltimaAtualizacao());
                    }
                    
                    if (config.getAtualizadoPor() != null && !config.getAtualizadoPor().isEmpty()) {
                        textAtualizadoPor.setText("Por: " + config.getAtualizadoPor());
                    }
                }
            }
        });
    }

    /**
     * Abre a pasta do Google Drive no navegador.
     */
    private void abrirPastaGoogleDrive() {
        if (configuracaoAtual == null || configuracaoAtual.getUrlPastaGoogleDrive() == null 
                || configuracaoAtual.getUrlPastaGoogleDrive().isEmpty()) {
            Toast.makeText(this, "Link da pasta não configurado", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(configuracaoAtual.getUrlPastaGoogleDrive()));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao abrir link: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Abre a tela de configuração (apenas coordenador).
     */
    private void abrirTelaConfiguracao() {
        Intent intent = new Intent(this, ConfigurarDocumentosActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarrega as configurações ao voltar da tela de configuração
        carregarConfiguracao();
    }
}
