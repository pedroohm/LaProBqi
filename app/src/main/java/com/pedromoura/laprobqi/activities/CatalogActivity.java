package com.pedromoura.laprobqi.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pedromoura.laprobqi.BancoDadosProduto;
import com.pedromoura.laprobqi.Produto;
import com.pedromoura.laprobqi.ProdutoTabelaAdapter;
import com.pedromoura.laprobqi.R;

import java.util.ArrayList;
import java.util.List;

public class CatalogActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProdutoTabelaAdapter adapter;
    private BancoDadosProduto banco;
    private EditText editPesquisa;
    private List<Produto> todosProdutos;
    private List<Produto> produtosFiltrados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_catalog);
        
        banco = BancoDadosProduto.getInstancia(this);
        
        recyclerView = findViewById(R.id.recyclerViewProdutos);
        editPesquisa = findViewById(R.id.editPesquisa);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        todosProdutos = new ArrayList<>();
        produtosFiltrados = new ArrayList<>();
        
        carregarProdutos();
        configurarPesquisa();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        carregarProdutos(); // Recarrega a lista quando volta para esta tela
    }
    
    private void carregarProdutos() {
        todosProdutos = banco.listarProdutos();
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
                if (produto.getNome().toLowerCase().contains(textoLower) ||
                    String.valueOf(produto.getId()).contains(texto)) {
                    produtosFiltrados.add(produto);
                }
            }
        }
        
        if (adapter == null) {
            adapter = new ProdutoTabelaAdapter(produtosFiltrados, this);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.atualizarLista(produtosFiltrados);
        }
    }
}