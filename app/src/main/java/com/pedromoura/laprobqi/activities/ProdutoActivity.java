package com.pedromoura.laprobqi.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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

    private EditText editNome, editValidade, editQuantidade, editUnidade, editObs;
    private Spinner spinnerCategoria;
    private View viewCorIndicador;
    private TextView txtCodigoCor, txtNomeCor;
    private TextView textLista;
    private ProdutoRepository produtoRepository;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;
    private String codigoSelecionado = "IND";
    
    // Formatador para exibir números com ponto e no máximo 3 casas decimais
    private static final DecimalFormat decimalFormat;
    
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');
        decimalFormat = new DecimalFormat("#,##0.###", symbols);
        decimalFormat.setMaximumFractionDigits(3);
        decimalFormat.setMinimumFractionDigits(0);
    }
    
    /**
     * Formata um número com ponto como separador decimal e no máximo 3 casas
     */
    private static String formatarNumero(double valor) {
        return decimalFormat.format(valor);
    }
    
    /**
     * Converte string com ponto para double (formato padrão)
     */
    private static double parseNumero(String texto) throws NumberFormatException {
        // Aceita apenas ponto como separador decimal
        String textoNormalizado = texto.trim();
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
        editValidade = findViewById(R.id.editValidade);
        editQuantidade = findViewById(R.id.editQuantidade);
        editUnidade = findViewById(R.id.editUnidade);
        editObs = findViewById(R.id.editObs);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        viewCorIndicador = findViewById(R.id.viewCorIndicador);
        txtCodigoCor = findViewById(R.id.txtCodigoCor);
        txtNomeCor = findViewById(R.id.txtNomeCor);
        textLista = findViewById(R.id.textLista);

        // Configurar campo de quantidade para aceitar decimais (use ponto como separador)
        editQuantidade.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editQuantidade.setHint("Ex: 10.5 ou 100.25 (use ponto)");

        produtoRepository = RepositoryProvider.getInstance(this).getProdutoRepository();
        calendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Configurar Spinner de categorias
        configurarSpinnerCategoria();

        // Configurar DatePicker para o campo de validade
        editValidade.setOnClickListener(v -> mostrarDatePicker());

        findViewById(R.id.btnSalvar).setOnClickListener(view -> salvarProduto());
        findViewById(R.id.btnListar).setOnClickListener(view -> listarProdutos());
        findViewById(R.id.btnLimpar).setOnClickListener(view -> limparCampos());
        
        // Config button
        findViewById(R.id.btnConfig).setOnClickListener(v -> {
            Intent intent = new Intent(ProdutoActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void configurarSpinnerCategoria() {
        String[] categorias = Produto.getCategorias();
        String[] codigos = Produto.getCodigos();
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, categorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapter);
        
        // Definir "Indefinidos" como padrão (última posição)
        spinnerCategoria.setSelection(categorias.length - 1);
        
        spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                codigoSelecionado = codigos[position];
                atualizarIndicadorCor();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                codigoSelecionado = "IND";
                atualizarIndicadorCor();
            }
        });
        
        atualizarIndicadorCor();
    }

    private void atualizarIndicadorCor() {
        Produto produtoTemp = new Produto("", "", "", 0, "", "");
        produtoTemp.setCategoria(codigoSelecionado);
        
        viewCorIndicador.setBackgroundColor(Color.parseColor(produtoTemp.getHexColor()));
        txtCodigoCor.setText("Código: " + produtoTemp.getCodigo());
        txtNomeCor.setText("Cor: " + produtoTemp.getCor());
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

            Produto p = new Produto(nome, "", validade, quantidade, unidade, obs);
            p.setCategoria(codigoSelecionado); // Definir categoria selecionada
            
            // Log para debug
            Log.d("ProdutoActivity", "Salvando produto: " + nome);
            Log.d("ProdutoActivity", "Código selecionado: " + codigoSelecionado);
            Log.d("ProdutoActivity", "Categoria aplicada: " + p.getCategoria());
            Log.d("ProdutoActivity", "Código do produto: " + p.getCodigo());
            Log.d("ProdutoActivity", "Cor: " + p.getCor());
            Log.d("ProdutoActivity", "HexColor: " + p.getHexColor());
            
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
            editQuantidade.setError("Quantidade inválida (use ponto para decimais, ex: 10.5)");
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
                    texto.append("━━━━━━━━━━━━━━━━━━━\n");
                    texto.append("Nome: ").append(p.getNome()).append("\n");
                    if (p.getCategoria() != null && !p.getCategoria().isEmpty()) {
                        texto.append("🏷️ ").append(p.getCategoria()).append(" [").append(p.getCodigo()).append("]\n");
                        texto.append("🎨 Cor: ").append(p.getCor()).append("\n");
                    }
                    texto.append("Validade: ").append(p.getValidade()).append("\n");
                    texto.append("Quantidade: ").append(formatarNumero(p.getQuantidade())).append(" ").append(p.getUnidade()).append("\n");
                    if (p.getObservacoes() != null && !p.getObservacoes().isEmpty()) {
                        texto.append("Obs: ").append(p.getObservacoes()).append("\n");
                    }
                }
                texto.append("━━━━━━━━━━━━━━━━━━━\n");
                textLista.setText(texto.toString());
            }
        });
    }

    private void limparCampos() {
        editNome.setText("");
        editValidade.setText("");
        editQuantidade.setText("");
        editUnidade.setText("");
        editObs.setText("");
        spinnerCategoria.setSelection(Produto.getCategorias().length - 1); // Indefinido
        editNome.requestFocus();
    }
}