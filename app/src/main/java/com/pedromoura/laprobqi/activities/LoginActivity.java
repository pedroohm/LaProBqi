package com.pedromoura.laprobqi.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.pedromoura.laprobqi.R;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends Activity {

    private EditText emailInput;
    private EditText passwordInput;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        initializeViews();
        prefs = getSharedPreferences("users", MODE_PRIVATE);
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.inputLogin);
        passwordInput = findViewById(R.id.inputSenha);
    }

    public void clickLogin(View view) {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Preencha todos os campos.");
            return;
        }

        String userDataString = prefs.getString(email, null);
        if (userDataString == null) {
            showToast("Usuário não encontrado!");
            return;
        }

        try {
            JSONObject userData = new JSONObject(userDataString);
            String storedPassword = userData.getString("password");
            String name = userData.getString("name");

            if (password.equals(storedPassword)) {
                navigateToInitialMenu(name);
            } else {
                showToast("Senha incorreta!");
            }

        } catch (JSONException e) {
            showToast("Erro ao processar os dados do usuário!");
        }
    }

    private void navigateToInitialMenu(String userName) {
        showToast("Bem-vindo, " + userName + "!");
        Intent intent = new Intent(this, InitialMenuActivity.class);
        startActivity(intent);
        finish();
    }

    public void clickRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}