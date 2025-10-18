package com.pedromoura.laprobqi.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.di.RepositoryProvider;
import com.pedromoura.laprobqi.repository.UsuarioRepository;

public class RegisterActivity extends AppCompatActivity {

    private EditText editName, editEmail, editPassword, editConfirmPassword;
    private Spinner spinnerNivelAcesso;
    private Button btnRegister;
    private ProgressBar progressBar;
    private UsuarioRepository usuarioRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar repository
        usuarioRepository = RepositoryProvider.getInstance(this).getUsuarioRepository();

        // Inicializar views
        editName = findViewById(R.id.nameRegisterInput);
        editEmail = findViewById(R.id.emailRegisterInput);
        editPassword = findViewById(R.id.passwordRegisterInput);
        editConfirmPassword = findViewById(R.id.passwordConfirmRegisterInput);
        spinnerNivelAcesso = findViewById(R.id.spinnerNivelAcesso);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progress_bar);

        // Configurar spinner de nível de acesso (RF01)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"ALUNO", "COORDENADOR"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNivelAcesso.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> registrar());
    }

    private void registrar() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();
        String nivelAcesso = spinnerNivelAcesso.getSelectedItem().toString();

        // Validações
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

        usuarioRepository.registrar(name, email, password, nivelAcesso, (sucesso, mensagem) -> {
            progressBar.setVisibility(View.GONE);
            btnRegister.setEnabled(true);

            showToast(mensagem);

            if (sucesso) {
                finish(); // Volta para tela de login
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}