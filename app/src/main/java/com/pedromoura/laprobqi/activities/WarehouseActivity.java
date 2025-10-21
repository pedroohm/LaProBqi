package com.pedromoura.laprobqi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import android.app.Activity;

import com.pedromoura.laprobqi.R;

public class WarehouseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warehouse);
        
        // Configurar header
        ImageView configLogo = findViewById(R.id.configLogo);
        configLogo.setOnClickListener(v -> {
            Intent intent = new Intent(WarehouseActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    public void clickCatalog(View view) {
        Intent it = new Intent(this, CatalogActivity.class);
        startActivity(it);
    }

    public void clickAlert(View view) {
        Intent it = new Intent(this, AlertActivity.class);
        startActivity(it);
    }

    public void clickStock(View view) {
        Intent it = new Intent(this, StockActivity.class);
        startActivity(it);
    }
}