package com.pedromoura.laprobqi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InitialMenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_menu);
    }
    public void clickWarehouse(View view) {
        Intent it = new Intent(this, WarehouseActivity.class);
        startActivity(it);
    }

    public void clickSettings(View view) {
        Intent it = new Intent(this, SettingsActivity.class);
        startActivity(it);
    }
}