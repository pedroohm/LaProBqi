package com.pedromoura.laprobqi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StockActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProdutoAdapter adapter;
    private BancoDadosProduto banco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stock);
        
        banco = BancoDadosProduto.getInstancia(this);
        
        recyclerView = findViewById(R.id.recyclerViewEstoque);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        carregarEstoque();
        
        // Botão para adicionar novo produto
        findViewById(R.id.btnAdicionarProduto).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProdutoActivity.class);
            startActivity(intent);
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        carregarEstoque(); // Recarrega a lista quando volta para esta tela
    }
    
    private void carregarEstoque() {
        List<Produto> produtos = banco.listarProdutos();
        // Filtra apenas produtos com quantidade > 0
        produtos.removeIf(p -> p.getQuantidade() <= 0);
        adapter = new ProdutoAdapter(produtos, this);
        recyclerView.setAdapter(adapter);
    }
}