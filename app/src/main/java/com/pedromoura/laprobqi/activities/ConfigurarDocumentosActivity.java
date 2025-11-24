package com.pedromoura.laprobqi.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.model.ConfiguracaoDocumentos;
import com.pedromoura.laprobqi.repository.DocumentoRepository;
import com.pedromoura.laprobqi.repository.impl.DocumentoRepositoryFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity para configurar o link da pasta do Google Drive (apenas coordenador).
 */
public class ConfigurarDocumentosActivity extends AppCompatActivity {

    private DocumentoRepository documentoRepository;
    private EditText editUrlPasta;
    private EditText editInstrucoes;
    private MaterialButton btnTestarLink;
    private MaterialButton btnSalvar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configurar_documentos);

        inicializarComponentes();
        configurarListeners();
        carregarConfiguracaoAtual();
    }

    private void inicializarComponentes() {
        documentoRepository = new DocumentoRepositoryFirestore();
        
        editUrlPasta = findViewById(R.id.editUrlPasta);
        editInstrucoes = findViewById(R.id.editInstrucoes);
        btnTestarLink = findViewById(R.id.btnTestarLink);
        btnSalvar = findViewById(R.id.btnSalvar);
        
        ImageView btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());
    }

    private void configurarListeners() {
        btnTestarLink.setOnClickListener(v -> testarLink());
        btnSalvar.setOnClickListener(v -> salvarConfiguracao());
    }

    /**
     * Carrega a configuração atual para edição.
     */
    private void carregarConfiguracaoAtual() {
        documentoRepository.obterConfiguracao(new DocumentoRepository.OnSuccessListener<ConfiguracaoDocumentos>() {
            @Override
            public void onSuccess(ConfiguracaoDocumentos config) {
                if (config != null) {
                    if (config.getUrlPastaGoogleDrive() != null) {
                        editUrlPasta.setText(config.getUrlPastaGoogleDrive());
                    }
                    if (config.getInstrucoes() != null) {
                        editInstrucoes.setText(config.getInstrucoes());
                    }
                }
            }
        });
    }

    /**
     * Testa o link do Google Drive abrindo no navegador.
     */
    private void testarLink() {
        String url = editUrlPasta.getText().toString().trim();
        
        if (url.isEmpty()) {
            Toast.makeText(this, "Digite o link da pasta primeiro", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            Toast.makeText(this, "Link inválido. Use um link completo (https://...)", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
            Toast.makeText(this, "Abrindo link no navegador...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao abrir link: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Salva a configuração no Firestore.
     */
    private void salvarConfiguracao() {
        String url = editUrlPasta.getText().toString().trim();
        String instrucoes = editInstrucoes.getText().toString().trim();

        if (url.isEmpty()) {
            Toast.makeText(this, "Digite o link da pasta do Google Drive", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            Toast.makeText(this, "Link inválido. Use um link completo (https://...)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Confirmar antes de salvar
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmar Alterações")
                .setMessage("Deseja salvar as alterações na configuração de documentos?")
                .setPositiveButton("Salvar", (dialog, which) -> executarSalvamento(url, instrucoes))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Executa o salvamento no Firestore.
     */
    private void executarSalvamento(String url, String instrucoes) {
        // Obter dados do usuário atual
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String emailCoordenador = (user != null) ? user.getEmail() : "Desconhecido";
        
        // Data atual formatada
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String dataAtual = sdf.format(new Date());

        // Criar objeto de configuração
        ConfiguracaoDocumentos config = new ConfiguracaoDocumentos(
                url,
                instrucoes.isEmpty() ? null : instrucoes,
                dataAtual,
                emailCoordenador
        );

        // Salvar no Firestore
        documentoRepository.salvarConfiguracao(config, new DocumentoRepository.OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ConfigurarDocumentosActivity.this, 
                        "Configuração salva com sucesso!", 
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
