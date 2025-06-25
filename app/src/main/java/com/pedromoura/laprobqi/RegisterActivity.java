package com.pedromoura.laprobqi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends Activity {

    private EditText nameInput;
    private EditText emailInput;
    private EditText numberInput;
    private EditText passwordInput;
    private EditText passwordConfirmInput;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();
        prefs = getSharedPreferences("users", MODE_PRIVATE);
    }

    private void initializeViews() {
        nameInput = findViewById(R.id.nameRegisterInput);
        emailInput = findViewById(R.id.emailRegisterInput);
        numberInput = findViewById(R.id.numberRegisterInput);
        passwordInput = findViewById(R.id.passwordRegisterInput);
        passwordConfirmInput = findViewById(R.id.passwordConfirmRegisterInput);
    }

    public void clickRegister(View view) {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String number = numberInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String passwordConfirm = passwordConfirmInput.getText().toString();

        if (!validateInputs(name, email, number, password, passwordConfirm)) return;

        try {
            JSONObject userData = new JSONObject();
            userData.put("name", name);
            userData.put("email", email);
            userData.put("number", number);
            userData.put("password", password);

            prefs.edit().putString(email, userData.toString()).apply();

            showToast("Usuário registrado com sucesso!");
            navigateToLogin();

        } catch (JSONException e) {
            showToast("Erro ao registrar usuário!");
        }
    }

    private boolean validateInputs(String name, String email, String number, String password, String passwordConfirm) {
        if (name.isEmpty() || email.isEmpty() || number.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
            showToast("Preencha todos os campos.");
            return false;
        }

        if (!password.equals(passwordConfirm)) {
            showToast("Senhas não coincidem!");
            return false;
        }

        if (prefs.contains(email)) {
            showToast("Usuário já registrado com esse e-mail.");
            return false;
        }

        return true;
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish(); // Opcional: fecha tela de registro
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}