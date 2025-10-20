package com.pedromoura.laprobqi.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pedromoura.laprobqi.BancoDadosProduto;
import com.pedromoura.laprobqi.Produto;
import com.pedromoura.laprobqi.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ProdutoActivity extends AppCompatActivity {

    private EditText editNome, editTipo, editValidade, editQuantidade, editUnidade, editObs;
    private TextView textLista;
    private BancoDadosProduto banco;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;

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
        calendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Configurar DatePicker para o campo de validade
        editValidade.setOnClickListener(v -> mostrarDatePicker());

        findViewById(R.id.btnSalvar).setOnClickListener(view -> salvarProduto());
        findViewById(R.id.btnListar).setOnClickListener(view -> listarProdutos());
        findViewById(R.id.btnLimpar).setOnClickListener(view -> limparCampos());
    }

    private void mostrarDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                editValidade.setText(dateFormatter.format(calendar.getTime()));
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // Define a data mínima como hoje
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
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
            editNome.requestFocus();
            return;
        }
        if (tipo.isEmpty()) {
            editTipo.setError("Tipo é obrigatório");
            editTipo.requestFocus();
            return;
        }
        if (quantidadeStr.isEmpty()) {
            editQuantidade.setError("Quantidade é obrigatória");
            editQuantidade.requestFocus();
            return;
        }
        if (validade.isEmpty()) {
            Toast.makeText(this, "Por favor, selecione a data de validade", Toast.LENGTH_SHORT).show();
            editValidade.requestFocus();
            return;
        }

        try {
            double quantidade = Double.parseDouble(quantidadeStr);
            
            if (quantidade <= 0) {
                editQuantidade.setError("Quantidade deve ser maior que zero");
                return;
            }
            
            Produto p = new Produto(nome, tipo, validade, quantidade, unidade, obs);
            boolean sucesso = banco.inserirProduto(p);
            
            if (sucesso) {
                Toast.makeText(this, "✓ Produto salvo com sucesso!", Toast.LENGTH_SHORT).show();
                limparCampos();
                listarProdutos();
            } else {
                Toast.makeText(this, "✗ Erro ao salvar produto", Toast.LENGTH_SHORT).show();
            }
            
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