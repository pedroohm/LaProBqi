package com.pedromoura.laprobqi.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pedromoura.laprobqi.Produto;
import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.di.RepositoryProvider;
import com.pedromoura.laprobqi.repository.ProdutoRepository;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ProdutoActivity extends AppCompatActivity {

    private EditText editNome, editTipo, editValidade, editQuantidade, editUnidade, editObs;
    private TextView textLista;
    private ProdutoRepository produtoRepository;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;
    
    // Formatador para exibir números com vírgula e no máximo 3 casas decimais
    private static final DecimalFormat decimalFormat;
    
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "BR"));
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        decimalFormat = new DecimalFormat("#,##0.###", symbols);
        decimalFormat.setMaximumFractionDigits(3);
        decimalFormat.setMinimumFractionDigits(0);
    }
    
    /**
     * Formata um número com vírgula como separador decimal e no máximo 3 casas
     */
    private static String formatarNumero(double valor) {
        return decimalFormat.format(valor);
    }
    
    /**
     * Converte string com vírgula para double
     */
    private static double parseNumero(String texto) throws NumberFormatException {
        // Substitui vírgula por ponto para fazer o parse
        String textoNormalizado = texto.trim().replace(',', '.');
        return Double.parseDouble(textoNormalizado);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produto);

        // Permitido para todos os usuários (alunos e coordenadores)
        inicializarActivity();
    }

    private void inicializarActivity() {
        editNome = findViewById(R.id.editNome);
        editTipo = findViewById(R.id.editTipo);
        editValidade = findViewById(R.id.editValidade);
        editQuantidade = findViewById(R.id.editQuantidade);
        editUnidade = findViewById(R.id.editUnidade);
        editObs = findViewById(R.id.editObs);
        textLista = findViewById(R.id.textLista);

        // Configurar campo de quantidade para aceitar decimais
        editQuantidade.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editQuantidade.setHint("Ex: 10,5 ou 100,25");

        produtoRepository = RepositoryProvider.getInstance(this).getProdutoRepository();
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
            // Usar parseNumero que aceita vírgula
            double quantidade = parseNumero(quantidadeStr);

            if (quantidade <= 0) {
                editQuantidade.setError("Quantidade deve ser maior que zero");
                return;
            }

            Produto p = new Produto(nome, tipo, validade, quantidade, unidade, obs);
            
            produtoRepository.adicionarProduto(p, new ProdutoRepository.OnCompleteListener() {
                @Override
                public void onComplete(boolean sucesso, String mensagem) {
                    if (sucesso) {
                        Toast.makeText(ProdutoActivity.this, "✓ Produto salvo com sucesso!", Toast.LENGTH_SHORT).show();
                        limparCampos();
                        listarProdutos();
                    } else {
                        Toast.makeText(ProdutoActivity.this, "✗ Erro ao salvar produto: " + mensagem, Toast.LENGTH_LONG).show();
                    }
                }
            });

        } catch (NumberFormatException e) {
            editQuantidade.setError("Quantidade inválida (use vírgula para decimais, ex: 10,5)");
        }
    }

    private void listarProdutos() {
        produtoRepository.listarProdutos(new ProdutoRepository.OnSuccessListener<List<Produto>>() {
            @Override
            public void onSuccess(List<Produto> produtos) {
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
                    // Exibir quantidade com formatação brasileira (vírgula, máx 3 decimais)
                    texto.append("Quantidade: ").append(formatarNumero(p.getQuantidade())).append(" ").append(p.getUnidade()).append("\n");
                    texto.append("Obs: ").append(p.getObservacoes()).append("\n\n");
                }
                textLista.setText(texto.toString());
            }
        });
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