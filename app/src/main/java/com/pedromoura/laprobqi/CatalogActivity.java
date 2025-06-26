package com.pedromoura.laprobqi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CatalogActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProdutoAdapter adapter;
    private BancoDadosProduto banco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_catalog);
        
        banco = BancoDadosProduto.getInstancia(this);
        
        recyclerView = findViewById(R.id.recyclerViewProdutos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        carregarProdutos();
        
        // Botão para adicionar novo produto
        findViewById(R.id.btnAdicionarProduto).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProdutoActivity.class);
            startActivity(intent);
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        carregarProdutos(); // Recarrega a lista quando volta para esta tela
    }
    
    private void carregarProdutos() {
        List<Produto> produtos = banco.listarProdutos();
        adapter = new ProdutoAdapter(produtos, this);
        recyclerView.setAdapter(adapter);
    }
}