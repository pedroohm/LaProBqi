package com.pedromoura.laprobqi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.app.Activity;

import com.pedromoura.laprobqi.R;

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