package com.pedromoura.laprobqi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

public class RegisterActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void clickRegister(View v){
        EditText nameInput = findViewById(R.id.nameRegisterInput);
        EditText emailInput = findViewById(R.id.emailRegisterInput);
        EditText numberInput = findViewById(R.id.numberRegisterInput);
        EditText passwordInput = findViewById(R.id.passwordRegisterInput);
        EditText passwordConfirmInput = findViewById(R.id.passwordConfirmRegisterInput);
        
        String name = nameInput.getText().toString();
        String email = emailInput.getText().toString();
        String number = numberInput.getText().toString();
        String password = passwordInput.getText().toString();
        String passwordConfirm = passwordConfirmInput.getText().toString();
        
        // Validação básica
        if (password.equals(passwordConfirm)) {
            try {
                JSONObject userData = new JSONObject();
                userData.put("name", name);
                userData.put("email", email);
                userData.put("number", number);
                userData.put("password", password);
                
                SharedPreferences prefs = getSharedPreferences("users", MODE_PRIVATE);
                prefs.edit().putString(email, userData.toString()).apply();
                
                Toast.makeText(this, "Usuário registrado com sucesso!", Toast.LENGTH_SHORT).show();
                Intent it = new Intent(this, LoginActivity.class);
                startActivity(it);
            } catch (Exception e) {
                Toast.makeText(this, "Erro ao registrar usuário!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Senhas não coincidem!", Toast.LENGTH_SHORT).show();
        }
    }
}