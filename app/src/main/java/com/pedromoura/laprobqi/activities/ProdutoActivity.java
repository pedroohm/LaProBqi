package com.pedromoura.laprobqi.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pedromoura.laprobqi.BancoDadosProduto;
import com.pedromoura.laprobqi.Produto;
import com.pedromoura.laprobqi.R;

import java.util.List;

public class ProdutoActivity extends AppCompatActivity {

    private EditText editNome, editTipo, editValidade, editQuantidade, editUnidade, editObs;
    private TextView textLista;
    private BancoDadosProduto banco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produto);

        editNome = findViewById(R.id.editNome);
        editTipo = findViewById(R.id.editTipo);
        editValidade = findViewById(R.id.editValidade);
        editQuantidade = findViewById(R.id.editQuantidade);
        editUnidade = findViewById(R.id.editUnidade);
        editObs = findViewById(R.id.editObs);
        textLista = findViewById(R.id.textLista);

        banco = BancoDadosProduto.getInstancia(this);

        findViewById(R.id.btnSalvar).setOnClickListener(view -> salvarProduto());
        findViewById(R.id.btnListar).setOnClickListener(view -> listarProdutos());
        findViewById(R.id.btnLimpar).setOnClickListener(view -> limparCampos());
    }

    private void salvarProduto() {
        String nome = editNome.getText().toString().trim();
        String tipo = editTipo.getText().toString().trim();
        String validade = editValidade.getText().toString().trim();
        String quantidadeStr = editQuantidade.getText().toString().trim();
        String unidade = editUnidade.getText().toString().trim();
        String obs = editObs.getText().toString().trim();

        // Validações
        if (nome.isEmpty()) {
            editNome.setError("Nome é obrigatório");
            return;
        }
        if (tipo.isEmpty()) {
            editTipo.setError("Tipo é obrigatório");
            return;
        }
        if (quantidadeStr.isEmpty()) {
            editQuantidade.setError("Quantidade é obrigatória");
            return;
        }

        try {
            double quantidade = Double.parseDouble(quantidadeStr);
            
            Produto p = new Produto(nome, tipo, validade, quantidade, unidade, obs);
            banco.inserirProduto(p);
            Toast.makeText(this, "Produto salvo com sucesso!", Toast.LENGTH_SHORT).show();
            
            limparCampos();
            listarProdutos(); // Atualiza a lista automaticamente
            
        } catch (NumberFormatException e) {
            editQuantidade.setError("Quantidade deve ser um número válido");
        }
    }

    private void listarProdutos() {
        List<Produto> produtos = banco.listarProdutos();
        if (produtos.isEmpty()) {
            textLista.setText("Nenhum produto cadastrado.");
            return;
        }
        
        StringBuilder texto = new StringBuilder();
        for (Produto p : produtos) {
            texto.append("ID: ").append(p.getId()).append("\n");
            texto.append("Nome: ").append(p.getNome()).append("\n");
            texto.append("Tipo: ").append(p.getTipo()).append("\n");
            texto.append("Validade: ").append(p.getValidade()).append("\n");
            texto.append("Quantidade: ").append(p.getQuantidade()).append(" ").append(p.getUnidade()).append("\n");
            texto.append("Obs: ").append(p.getObservacoes()).append("\n\n");
        }
        textLista.setText(texto.toString());
    }
    
    private void limparCampos() {
        editNome.setText("");
        editTipo.setText("");
        editValidade.setText("");
        editQuantidade.setText("");
        editUnidade.setText("");
        editObs.setText("");
        editNome.requestFocus();
    }
}