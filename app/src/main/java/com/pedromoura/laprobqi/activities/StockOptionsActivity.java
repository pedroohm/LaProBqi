package com.pedromoura.laprobqi.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.pedromoura.laprobqi.Produto;
import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.di.RepositoryProvider;
import com.pedromoura.laprobqi.repository.ProdutoRepository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StockOptionsActivity extends AppCompatActivity {

    private ProdutoRepository produtoRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_options);

        produtoRepository = RepositoryProvider.getInstance(this).getProdutoRepository();

        // Configurar header
        ImageView configLogo = findViewById(R.id.configLogo);
        configLogo.setOnClickListener(v -> {
            Intent intent = new Intent(StockOptionsActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        Button btnRegisterEntry = findViewById(R.id.btnRegisterEntry);
        Button btnRegisterExit = findViewById(R.id.btnRegisterExit);
        Button btnExportarEstoque = findViewById(R.id.btnExportarEstoque);

        // Botões de entrada e saída disponíveis para todos os usuários

        btnRegisterEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StockOptionsActivity.this, StockActivity.class);
                intent.putExtra("ACTION", "ENTRY");
                startActivity(intent);
            }
        });

        btnRegisterExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StockOptionsActivity.this, StockActivity.class);
                intent.putExtra("ACTION", "EXIT");
                startActivity(intent);
            }
        });

        btnExportarEstoque.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportarInventario();
            }
        });
    }

    /**
     * Exporta todo o inventário de produtos para arquivo CSV
     */
    private void exportarInventario() {
        Toast.makeText(this, "Carregando produtos...", Toast.LENGTH_SHORT).show();

        produtoRepository.listarProdutos(new ProdutoRepository.OnSuccessListener<List<Produto>>() {
            @Override
            public void onSuccess(List<Produto> produtos) {
                if (produtos.isEmpty()) {
                    Toast.makeText(StockOptionsActivity.this, "Nenhum produto cadastrado para exportar", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    // Criar diretório para exports
                    File exportDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "LaProBqi");
                    if (!exportDir.exists()) {
                        exportDir.mkdirs();
                    }

                    // Nome do arquivo com timestamp
                    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Calendar.getInstance().getTime());
                    String fileName = "inventario_produtos_" + timestamp + ".csv";
                    File csvFile = new File(exportDir, fileName);

                    // Escrever CSV
                    FileWriter writer = new FileWriter(csvFile);

                    // Cabeçalho com todas as características do produto
                    writer.append("Nome,Categoria,Tipo,Quantidade,Unidade,Validade,Observações,Código,Cor\n");

                    // Dados de cada produto
                    for (Produto produto : produtos) {
                        writer.append("\"").append(escapeCsv(produto.getNome())).append("\",");
                        writer.append("\"").append(escapeCsv(produto.getCategoria() != null ? produto.getCategoria() : "N/A")).append("\",");
                        writer.append("\"").append(escapeCsv(produto.getTipo() != null ? produto.getTipo() : "N/A")).append("\",");
                        writer.append(String.valueOf(produto.getQuantidade())).append(",");
                        writer.append("\"").append(escapeCsv(produto.getUnidade() != null ? produto.getUnidade() : "N/A")).append("\",");
                        writer.append("\"").append(escapeCsv(produto.getValidade() != null ? produto.getValidade() : "N/A")).append("\",");
                        writer.append("\"").append(escapeCsv(produto.getObservacoes() != null ? produto.getObservacoes() : "")).append("\",");
                        writer.append("\"").append(escapeCsv(produto.getCodigo() != null ? produto.getCodigo() : "N/A")).append("\",");
                        writer.append("\"").append(escapeCsv(produto.getCor() != null ? produto.getCor() : "N/A")).append("\"\n");
                    }

                    writer.flush();
                    writer.close();

                    // Compartilhar arquivo
                    Uri fileUri = FileProvider.getUriForFile(
                            StockOptionsActivity.this,
                            getPackageName() + ".fileprovider",
                            csvFile
                    );

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/csv");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Inventário de Produtos - LaProBqi");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, 
                        "Inventário completo do laboratório.\n" +
                        "Total de produtos: " + produtos.size() + "\n" +
                        "Data da exportação: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Calendar.getInstance().getTime()));
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    startActivity(Intent.createChooser(shareIntent, "Compartilhar inventário"));

                    Toast.makeText(StockOptionsActivity.this, 
                        "Inventário exportado! Total: " + produtos.size() + " produtos", 
                        Toast.LENGTH_LONG).show();

                } catch (IOException e) {
                    Toast.makeText(StockOptionsActivity.this, 
                        "Erro ao exportar: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Escapa caracteres especiais para CSV (aspas duplas)
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
