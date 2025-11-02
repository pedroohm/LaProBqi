package com.pedromoura.laprobqi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.pedromoura.laprobqi.R;

public class StockOptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_options);

        // Configurar header
        ImageView configLogo = findViewById(R.id.configLogo);
        configLogo.setOnClickListener(v -> {
            Intent intent = new Intent(StockOptionsActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        Button btnRegisterEntry = findViewById(R.id.btnRegisterEntry);
        Button btnRegisterExit = findViewById(R.id.btnRegisterExit);

        // Botões de entrada e saída disponíveis para todos os usuários

        btnRegisterEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StockOptionsActivity.this, StockActivity.class);
                intent.putExtra("ACTION", "ENTRY");
                startActivity(intent);
            }
        });

        btnRegisterExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StockOptionsActivity.this, StockActivity.class);
                intent.putExtra("ACTION", "EXIT");
                startActivity(intent);
            }
        });
    }
}
