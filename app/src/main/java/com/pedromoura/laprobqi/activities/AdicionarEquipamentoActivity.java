package com.pedromoura.laprobqi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.di.RepositoryProvider;
import com.pedromoura.laprobqi.models.Equipamento;
import com.pedromoura.laprobqi.models.Usuario;
import com.pedromoura.laprobqi.repository.EquipamentoRepository;
import com.pedromoura.laprobqi.utils.PermissionHelper;

public class AdicionarEquipamentoActivity extends AppCompatActivity {

    private EditText editTextNome, editTextDescricao;
    private Button btnSalvar, btnCancelar;
    private ProgressBar progressBar;
    
    private EquipamentoRepository equipamentoRepository;
    private Usuario usuarioAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_equipamento);

        // RF02: Verificar permissão de coordenador ANTES de inicializar
        PermissionHelper.verificarPermissaoOuFechar(this, 
            "Apenas coordenadores podem adicionar equipamentos", 
            usuario -> {
                usuarioAtual = usuario;
                inicializarActivity();
            });

        editTextNome = findViewById(R.id.editTextNome);
        editTextDescricao = findViewById(R.id.editTextDescricao);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnCancelar = findViewById(R.id.btnCancelar);
    }

    private void inicializarActivity() {
        // Inicializar repository
        equipamentoRepository = RepositoryProvider.getInstance(this).getEquipamentoRepository();

        // Inicializar views
        inicializarViews();
    }

    private void inicializarViews() {
        editTextNome = findViewById(R.id.editTextNome);
        editTextDescricao = findViewById(R.id.editTextDescricao);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnCancelar = findViewById(R.id.btnCancelar);
        progressBar = findViewById(R.id.progressBar);

        // Configurar listeners
        btnSalvar.setOnClickListener(v -> salvarEquipamento());
        btnCancelar.setOnClickListener(v -> finish());
        
        // Config button
        findViewById(R.id.btnConfig).setOnClickListener(v -> {
            Intent intent = new Intent(AdicionarEquipamentoActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void salvarEquipamento() {
        String nome = editTextNome.getText().toString().trim();
        String descricao = editTextDescricao.getText().toString().trim();

        // Validações
        if (nome.isEmpty()) {
            editTextNome.setError("Nome é obrigatório");
            editTextNome.requestFocus();
            return;
        }

        if (descricao.isEmpty()) {
            editTextDescricao.setError("Descrição é obrigatória");
            editTextDescricao.requestFocus();
            return;
        }

        // Criar equipamento
        Equipamento equipamento = new Equipamento(nome, descricao);
        
        progressBar.setVisibility(View.VISIBLE);
        btnSalvar.setEnabled(false);

        equipamentoRepository.salvarEquipamento(equipamento, new EquipamentoRepository.OnEquipamentoListener() {
            @Override
            public void onSuccess(Equipamento equipamentoSalvo) {
                progressBar.setVisibility(View.GONE);
                btnSalvar.setEnabled(true);
                showToast("Equipamento adicionado com sucesso!");
                finish();
            }

            @Override
            public void onFailure(String mensagem) {
                progressBar.setVisibility(View.GONE);
                btnSalvar.setEnabled(true);
                showToast("Erro ao adicionar equipamento: " + mensagem);
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
