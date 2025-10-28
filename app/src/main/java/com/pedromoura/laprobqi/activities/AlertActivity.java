package com.pedromoura.laprobqi.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pedromoura.laprobqi.BancoDadosProduto;
import com.pedromoura.laprobqi.Produto;
import com.pedromoura.laprobqi.adapters.ProdutoAlertaAdapter;
import com.pedromoura.laprobqi.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AlertActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProdutoAlertaAdapter adapter;
    private BancoDadosProduto banco;
    private Spinner spinnerDias;
    private LinearLayout emptyStateLayout;
    private List<Produto> todosProdutos;
    private SimpleDateFormat dateFormat;
    private int diasFiltro = 30; // Padrão: 30 dias

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        
        banco = BancoDadosProduto.getInstancia(this);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        recyclerView = findViewById(R.id.recyclerViewAlertas);
        spinnerDias = findViewById(R.id.spinnerDias);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        configurarSpinner();
        carregarProdutosEmAlerta();
    }
    
    private void configurarSpinner() {
        // Opções de filtro por dias
        String[] opcoes = {
            "Próximos 7 dias",
            "Próximos 15 dias",
            "Próximos 30 dias",
            "Próximos 60 dias",
            "Próximos 90 dias",
            "Todos os produtos"
        };
        
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
            this, 
            android.R.layout.simple_spinner_item, 
            opcoes
        );
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDias.setAdapter(adapterSpinner);
        spinnerDias.setSelection(2); // Seleciona "Próximos 30 dias" como padrão
        
        spinnerDias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: diasFiltro = 7; break;
                    case 1: diasFiltro = 15; break;
                    case 2: diasFiltro = 30; break;
                    case 3: diasFiltro = 60; break;
                    case 4: diasFiltro = 90; break;
                    case 5: diasFiltro = Integer.MAX_VALUE; break;
                }
                carregarProdutosEmAlerta();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        carregarProdutosEmAlerta();
    }
    
    private void carregarProdutosEmAlerta() {
        todosProdutos = banco.listarProdutos();
        List<Produto> produtosEmAlerta = filtrarProdutosPorValidade();
        
        if (produtosEmAlerta.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            
            if (adapter == null) {
                adapter = new ProdutoAlertaAdapter(produtosEmAlerta, this);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.atualizarLista(produtosEmAlerta);
            }
        }
    }
    
    private List<Produto> filtrarProdutosPorValidade() {
        List<Produto> produtosFiltrados = new ArrayList<>();
        Date hoje = new Date();
        
        for (Produto produto : todosProdutos) {
            String validade = produto.getValidade();
            if (validade != null && !validade.isEmpty()) {
                try {
                    Date dataValidade = dateFormat.parse(validade);
                    long diferenca = dataValidade.getTime() - hoje.getTime();
                    long diasRestantes = TimeUnit.MILLISECONDS.toDays(diferenca);
                    
                    // Inclui produtos já vencidos ou que vencerão dentro do filtro
                    if (diasRestantes <= diasFiltro) {
                        produtosFiltrados.add(produto);
                    }
                    
                } catch (ParseException e) {
                    // Ignora produtos com data inválida
                }
            }
        }
        
        // Ordena por data de validade (mais próximo primeiro)
        Collections.sort(produtosFiltrados, new Comparator<Produto>() {
            @Override
            public int compare(Produto p1, Produto p2) {
                try {
                    Date data1 = dateFormat.parse(p1.getValidade());
                    Date data2 = dateFormat.parse(p2.getValidade());
                    return data1.compareTo(data2);
                } catch (ParseException e) {
                    return 0;
                }
            }
        });
        
        return produtosFiltrados;
    }
}