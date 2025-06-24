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

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
    }

    public void clickLogin(View v){
        EditText emailInput = findViewById(R.id.inputLogin);
        EditText passwordInput = findViewById(R.id.inputSenha);
        
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        
        SharedPreferences prefs = getSharedPreferences("users", MODE_PRIVATE);
        
        // Opção 1: Verificar campo por campo
        String senhaSalva = prefs.getString(email + "_password", null);
        
        // Opção 2: Verificar JSON
        String userDataString = prefs.getString(email, null);
        if (userDataString != null) {
            try {
                JSONObject userData = new JSONObject(userDataString);
                String senhaSalva = userData.getString("password");
                String nome = userData.getString("name");
                
                if (senhaSalva.equals(password)) {
                    Toast.makeText(this, "Bem-vindo, " + nome + "!", Toast.LENGTH_SHORT).show();
                    Intent it = new Intent(this, InitialMenuActivity.class);
                    startActivity(it);
                } else {
                    Toast.makeText(this, "Senha incorreta!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Erro ao fazer login!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Usuário não encontrado!", Toast.LENGTH_SHORT).show();
        }
    }

}