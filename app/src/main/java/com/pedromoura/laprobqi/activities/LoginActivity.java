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

import androidx.appcompat.app.AppCompatActivity;

import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.di.RepositoryProvider;
import com.pedromoura.laprobqi.repository.UsuarioRepository;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private ImageView btnTogglePassword;
    private Button btnLogin;
    private TextView textRegister;
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
        progressBar = findViewById(R.id.progress_bar);

        btnLogin.setOnClickListener(v -> fazerLogin());
        textRegister.setOnClickListener(v -> abrirTelaCadastro());
        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

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

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}