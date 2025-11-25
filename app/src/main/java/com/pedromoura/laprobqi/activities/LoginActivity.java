package com.pedromoura.laprobqi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.di.RepositoryProvider;
import com.pedromoura.laprobqi.repository.UsuarioRepository;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private ImageView btnTogglePassword;
    private Button btnLogin;
    private TextView textRegister, textForgotPassword;
    private ProgressBar progressBar;
    private UsuarioRepository usuarioRepository;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        // Inicializar repository
        usuarioRepository = RepositoryProvider.getInstance(this).getUsuarioRepository();

        // Inicializar views
        editEmail = findViewById(R.id.inputLogin);
        editPassword = findViewById(R.id.inputSenha);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnLogin = findViewById(R.id.btnEntrar);
        textRegister = findViewById(R.id.btnCadastrar);
        textForgotPassword = findViewById(R.id.forgotPassword);
        progressBar = findViewById(R.id.progress_bar);

        btnLogin.setOnClickListener(v -> fazerLogin());
        textRegister.setOnClickListener(v -> abrirTelaCadastro());
        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        textForgotPassword.setOnClickListener(v -> mostrarDialogoRecuperacaoSenha());

        // Verificar se já está logado
        verificarSessao();
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Ocultar senha
            editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePassword.setImageResource(android.R.drawable.ic_secure);
            isPasswordVisible = false;
        } else {
            // Mostrar senha
            editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnTogglePassword.setImageResource(android.R.drawable.ic_menu_view);
            isPasswordVisible = true;
        }
        // Mover cursor para o final
        editPassword.setSelection(editPassword.getText().length());
    }

    private void verificarSessao() {
        progressBar.setVisibility(View.VISIBLE);
        usuarioRepository.obterUsuarioAtual(usuario -> {
            progressBar.setVisibility(View.GONE);
            if (usuario != null) {
                navegarParaMenuInicial(usuario.getNome());
            }
        });
    }

    private void fazerLogin() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Preencha todos os campos");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        usuarioRepository.autenticar(email, password, new UsuarioRepository.OnAuthListener() {
            @Override
            public void onSuccess(String userId, String nome, String nivelAcesso) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                navegarParaMenuInicial(nome);
            }

            @Override
            public void onFailure(String mensagem) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                showToast(mensagem); // "E-mail ou senha inválidos" (RF01)
            }
        });
    }

    private void navegarParaMenuInicial(String userName) {
        showToast("Bem-vindo(A), " + userName + "!");
        Intent intent = new Intent(this, InitialMenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void abrirTelaCadastro() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void mostrarDialogoRecuperacaoSenha() {
        // Criar EditText para email
        final EditText emailInput = new EditText(this);
        emailInput.setHint("Digite seu e-mail");
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setPadding(50, 30, 50, 30);
        
        // Pré-preencher com email já digitado (se houver)
        String emailPreenchido = editEmail.getText().toString().trim();
        if (!emailPreenchido.isEmpty()) {
            emailInput.setText(emailPreenchido);
            emailInput.setSelection(emailPreenchido.length());
        }

        new AlertDialog.Builder(this)
            .setTitle("Recuperar Senha")
            .setMessage("Digite seu e-mail para receber as instruções de recuperação de senha.")
            .setView(emailInput)
            .setPositiveButton("Enviar", (dialog, which) -> {
                String email = emailInput.getText().toString().trim();
                
                if (email.isEmpty()) {
                    showToast("Digite um e-mail válido");
                    return;
                }
                
                progressBar.setVisibility(View.VISIBLE);
                
                usuarioRepository.resetarSenha(email, new UsuarioRepository.OnCompleteListener() {
                    @Override
                    public void onComplete(boolean sucesso, String mensagem) {
                        progressBar.setVisibility(View.GONE);
                        
                        if (sucesso) {
                            showToast("✓ Email enviado! Verifique sua caixa de entrada.");
                        } else {
                            showToast("✗ " + mensagem);
                        }
                    }
                });
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}