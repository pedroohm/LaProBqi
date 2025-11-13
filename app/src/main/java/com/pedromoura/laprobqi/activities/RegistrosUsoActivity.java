package com.pedromoura.laprobqi.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.adapters.RegistroUsoAdapter;
import com.pedromoura.laprobqi.database.DatabaseHelper;
import com.pedromoura.laprobqi.models.RegistroUso;
import com.pedromoura.laprobqi.repository.RegistroUsoRepository;
import com.pedromoura.laprobqi.repository.impl.RegistroUsoRepositorySQLite;

import java.util.ArrayList;
import java.util.List;

public class RegistrosUsoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RegistroUsoAdapter adapter;
    private RegistroUsoRepository registroUsoRepository;
    private final List<RegistroUso> registroUsoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registros_uso);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerViewRegistros);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RegistroUsoAdapter(this, registroUsoList);
        recyclerView.setAdapter(adapter);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        registroUsoRepository = new RegistroUsoRepositorySQLite(this);

        carregarRegistros();
    }

    private void carregarRegistros() {

        registroUsoRepository.obterTodosRegistrosUso(new RegistroUsoRepository.OnRegistrosUsoListener() {

            @Override
            public void onSuccess(List<RegistroUso> registrosUso) {
                registroUsoList.clear();
                registroUsoList.addAll(registrosUso);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String mensagem) {
                Log.e("RegistrosUsoActivity", "Falha ao carregar registros: " + mensagem);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
