package com.pedromoura.laprobqi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.app.Activity;

public class WarehouseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warehouse);
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