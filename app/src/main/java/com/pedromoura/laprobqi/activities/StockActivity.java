package com.pedromoura.laprobqi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pedromoura.laprobqi.BancoDadosProduto;
import com.pedromoura.laprobqi.Produto;
import com.pedromoura.laprobqi.adapters.ProdutoAdapter;
import com.pedromoura.laprobqi.R;

import java.util.ArrayList;
import java.util.List;

public class StockActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProdutoAdapter adapter;
    private BancoDadosProduto banco;
    private EditText editPesquisa;
    private List<Produto> todosProdutos;
    private List<Produto> produtosFiltrados;
    private String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stock);
        
        // Configurar header
        ImageView configLogo = findViewById(R.id.configLogo);
        configLogo.setOnClickListener(v -> {
            Intent intent = new Intent(StockActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        
        // Get the action from the intent
        action = getIntent().getStringExtra("ACTION");
        if (action == null) {
            action = "ENTRY"; // Default action
        }

        banco = BancoDadosProduto.getInstancia(this);
        
        recyclerView = findViewById(R.id.recyclerViewEstoque);
        editPesquisa = findViewById(R.id.editPesquisa);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        todosProdutos = new ArrayList<>();
        produtosFiltrados = new ArrayList<>();
        
        carregarEstoque();
        configurarPesquisa();
        
        // Botão para adicionar novo produto
        View btnAdicionarProduto = findViewById(R.id.btnAdicionarProduto);
        if ("EXIT".equals(action)) {
            btnAdicionarProduto.setVisibility(View.GONE);
        } else {
            btnAdicionarProduto.setVisibility(View.VISIBLE);
        }

        btnAdicionarProduto.setOnClickListener(v -> {
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
        todosProdutos = banco.listarProdutos();
        
        if ("EXIT".equals(action)) {
            // Filtra apenas produtos com quantidade > 0 para o modo de saída
            List<Produto> produtosComEstoque = new ArrayList<>();
            for (Produto produto : todosProdutos) {
                if (produto.getQuantidade() > 0) {
                    produtosComEstoque.add(produto);
                }
            }
            todosProdutos = produtosComEstoque;
        }
        // Para o modo de entrada, mostra todos os produtos

        filtrarProdutos(editPesquisa.getText().toString());
    }
    
    private void configurarPesquisa() {
        editPesquisa.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarProdutos(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void filtrarProdutos(String texto) {
        produtosFiltrados.clear();
        
        if (texto.isEmpty()) {
            produtosFiltrados.addAll(todosProdutos);
        } else {
            String textoLower = texto.toLowerCase();
            for (Produto produto : todosProdutos) {
                if (produto.getNome().toLowerCase().contains(textoLower)) {
                    produtosFiltrados.add(produto);
                }
            }
        }
        
        if (adapter == null) {
            adapter = new ProdutoAdapter(produtosFiltrados, this, "EXIT".equals(action)); // true for exit mode, false for entry mode
            recyclerView.setAdapter(adapter);
        } else {
            adapter.setExitMode("EXIT".equals(action));
            adapter.atualizarLista(produtosFiltrados);
        }
    }
}