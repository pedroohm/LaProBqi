package com.pedromoura.laprobqi.activities;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.di.RepositoryProvider;
import com.pedromoura.laprobqi.models.SolicitacaoCoordenador;
import com.pedromoura.laprobqi.repository.SolicitacaoCoordenadorRepository;
import com.pedromoura.laprobqi.repository.UsuarioRepository;
import com.pedromoura.laprobqi.repository.impl.SolicitacaoCoordenadorRepositoryFirestore;

public class RegisterActivity extends AppCompatActivity {

    private EditText editName, editEmail, editPassword, editConfirmPassword;
    private ImageView btnTogglePasswordRegister, btnTogglePasswordConfirm;
    private Spinner spinnerNivelAcesso;
    private Button btnRegister;
    private ProgressBar progressBar;
    private UsuarioRepository usuarioRepository;
    private SolicitacaoCoordenadorRepository solicitacaoCoordenadorRepository;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usuarioRepository = RepositoryProvider.getInstance(this).getUsuarioRepository();
        solicitacaoCoordenadorRepository = new SolicitacaoCoordenadorRepositoryFirestore();

        editName = findViewById(R.id.nameRegisterInput);
        editEmail = findViewById(R.id.emailRegisterInput);
        editPassword = findViewById(R.id.passwordRegisterInput);
        editConfirmPassword = findViewById(R.id.passwordConfirmRegisterInput);
        btnTogglePasswordRegister = findViewById(R.id.btnTogglePasswordRegister);
        btnTogglePasswordConfirm = findViewById(R.id.btnTogglePasswordConfirm);
        spinnerNivelAcesso = findViewById(R.id.spinnerNivelAcesso);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progress_bar);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"ALUNO", "COORDENADOR"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNivelAcesso.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> registrar());
        btnTogglePasswordRegister.setOnClickListener(v -> togglePasswordVisibility());
        btnTogglePasswordConfirm.setOnClickListener(v -> toggleConfirmPasswordVisibility());
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePasswordRegister.setImageResource(android.R.drawable.ic_secure);
            isPasswordVisible = false;
        } else {
            editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnTogglePasswordRegister.setImageResource(android.R.drawable.ic_menu_view);
            isPasswordVisible = true;
        }
        editPassword.setSelection(editPassword.getText().length());
    }

    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            editConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePasswordConfirm.setImageResource(android.R.drawable.ic_secure);
            isConfirmPasswordVisible = false;
        } else {
            editConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnTogglePasswordConfirm.setImageResource(android.R.drawable.ic_menu_view);
            isConfirmPasswordVisible = true;
        }
        editConfirmPassword.setSelection(editConfirmPassword.getText().length());
    }

    private void registrar() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();
        String nivelAcesso = spinnerNivelAcesso.getSelectedItem().toString();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showToast("Preencha todos os campos");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showToast("As senhas não coincidem");
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);
        
        if ("COORDENADOR".equals(nivelAcesso)) {
            criarSolicitacaoCoordenador(name, email, password);
        } else {
            registrarUsuario(name, email, password, nivelAcesso);
        }
    }
    
    private void criarSolicitacaoCoordenador(String nome, String email, String senha) {
        solicitacaoCoordenadorRepository.verificarEmailJaSolicitado(email, 
            new SolicitacaoCoordenadorRepository.OnBooleanListener() {
                @Override
                public void onSuccess(boolean jaSolicitado) {
                    if (jaSolicitado) {
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);
                        showToast("⚠️ Já existe uma solicitação pendente para este email");
                    } else {
                        verificarSolicitacaoAprovada(nome, email, senha);
                    }
                }
                
                @Override
                public void onFailure(String mensagem) {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    showToast("Erro ao verificar solicitação: " + mensagem);
                }
            });
    }
    
    private void verificarSolicitacaoAprovada(String nome, String email, String senha) {
        solicitacaoCoordenadorRepository.verificarEmailAprovado(email,
            new SolicitacaoCoordenadorRepository.OnBooleanListener() {
                @Override
                public void onSuccess(boolean aprovado) {
                    if (aprovado) {
                        registrarUsuario(nome, email, senha, "COORDENADOR");
                    } else {
                        criarNovaSolicitacao(nome, email);
                    }
                }
                
                @Override
                public void onFailure(String mensagem) {
                    criarNovaSolicitacao(nome, email);
                }
            });
    }
    
    private void criarNovaSolicitacao(String nome, String email) {
        SolicitacaoCoordenador solicitacao = new SolicitacaoCoordenador(nome, email);
        solicitacaoCoordenadorRepository.criarSolicitacao(solicitacao, 
            new SolicitacaoCoordenadorRepository.OnSolicitacaoListener() {
                @Override
                public void onSuccess(SolicitacaoCoordenador solicitacao) {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    showToast("✅ Solicitação enviada! Aguarde a aprovação de um coordenador.");
                    finish();
                }
                
                @Override
                public void onFailure(String mensagem) {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    showToast(mensagem);
                }
            });
    }
    
    private void registrarUsuario(String name, String email, String password, String nivelAcesso) {

        usuarioRepository.registrar(name, email, password, nivelAcesso, (sucesso, mensagem) -> {
            progressBar.setVisibility(View.GONE);
            btnRegister.setEnabled(true);

            showToast(mensagem);

            if (sucesso) {
                finish();
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}