package com.pedromoura.laprobqi.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.adapters.PresencaLabAdapter;
import com.pedromoura.laprobqi.di.RepositoryProvider;
import com.pedromoura.laprobqi.models.PresencaLab;
import com.pedromoura.laprobqi.repository.PresencaLabRepository;
import com.pedromoura.laprobqi.utils.PermissionHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity para coordenadores visualizarem relatório completo de presenças do laboratório.
 * 
 * Funcionalidades:
 * - Visualizar todas as presenças registradas
 * - Filtrar por período de datas
 * - Estatísticas: total de visitas, membros únicos, média de tempo
 * - Lista completa com todos os check-ins/check-outs
 */
public class RelatorioPresencasActivity extends Activity {
    
    private PresencaLabRepository presencaRepository;
    
    // Views de estatísticas
    private TextView textTotalVisitas;
    private TextView textMembrosUnicos;
    private TextView textPeriodoAtual;
    
    // Views de filtro
    private Button btnDataInicio;
    private Button btnDataFim;
    private Button btnAplicarFiltro;
    private Button btnLimparFiltro;
    private Button btnExportar;
    
    // RecyclerView
    private RecyclerView recyclerViewPresencas;
    private PresencaLabAdapter adapter;
    private List<PresencaLab> listaPresencas;
    
    // Loading
    private ProgressBar progressBar;
    private TextView textMensagemVazia;
    
    // Filtros
    private String dataInicio = null; // Formato: YYYY-MM-DD
    private String dataFim = null;
    
    private SimpleDateFormat formatoExibicao = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat formatoBanco = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorio_presencas);
        
        // Verificar permissão de coordenador
        PermissionHelper.verificarPermissaoOuFechar(this, 
            "Acesso negado: apenas coordenadores podem visualizar relatórios", 
            usuario -> inicializarActivity());
    }
    
    private void inicializarActivity() {
        presencaRepository = RepositoryProvider.getInstance(this).getPresencaLabRepository();
        
        inicializarViews();
        configurarRecyclerView();
        configurarBotoes();
        
        // Carregar todas as presenças por padrão
        carregarPresencas();
    }
    
    private void inicializarViews() {
        // Header
        ImageView btnVoltar = findViewById(R.id.btnVoltarRelatorio);
        btnVoltar.setOnClickListener(v -> finish());
        
        // Estatísticas
        textTotalVisitas = findViewById(R.id.textTotalVisitas);
        textMembrosUnicos = findViewById(R.id.textMembrosUnicos);
        textPeriodoAtual = findViewById(R.id.textPeriodoAtual);
        
        // Filtros
        btnDataInicio = findViewById(R.id.btnDataInicio);
        btnDataFim = findViewById(R.id.btnDataFim);
        btnAplicarFiltro = findViewById(R.id.btnAplicarFiltro);
        btnLimparFiltro = findViewById(R.id.btnLimparFiltro);
        btnExportar = findViewById(R.id.btnExportar);
        
        // RecyclerView
        recyclerViewPresencas = findViewById(R.id.recyclerViewPresencasRelatorio);
        
        // Loading
        progressBar = findViewById(R.id.progressBarRelatorio);
        textMensagemVazia = findViewById(R.id.textMensagemVaziaRelatorio);
        
        // Período padrão: últimos 30 dias
        Calendar cal = Calendar.getInstance();
        dataFim = formatoBanco.format(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, -30);
        dataInicio = formatoBanco.format(cal.getTime());
        
        atualizarTextoPeriodo();
    }
    
    private void configurarRecyclerView() {
        listaPresencas = new ArrayList<>();
        adapter = new PresencaLabAdapter(this, listaPresencas);
        
        recyclerViewPresencas.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPresencas.setAdapter(adapter);
    }
    
    private void configurarBotoes() {
        btnDataInicio.setOnClickListener(v -> selecionarData(true));
        btnDataFim.setOnClickListener(v -> selecionarData(false));
        btnAplicarFiltro.setOnClickListener(v -> aplicarFiltro());
        btnLimparFiltro.setOnClickListener(v -> limparFiltro());
        btnExportar.setOnClickListener(v -> exportarCSV());
    }
    
    private void selecionarData(boolean isDataInicio) {
        Calendar calendar = Calendar.getInstance();
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, dayOfMonth);
                
                String dataSelecionada = formatoBanco.format(selected.getTime());
                
                if (isDataInicio) {
                    dataInicio = dataSelecionada;
                    btnDataInicio.setText("Início: " + formatoExibicao.format(selected.getTime()));
                } else {
                    dataFim = dataSelecionada;
                    btnDataFim.setText("Fim: " + formatoExibicao.format(selected.getTime()));
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }
    
    private void aplicarFiltro() {
        if (dataInicio == null || dataFim == null) {
            Toast.makeText(this, "Selecione as datas de início e fim", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validar que data início é antes de data fim
        try {
            if (formatoBanco.parse(dataInicio).after(formatoBanco.parse(dataFim))) {
                Toast.makeText(this, "Data de início deve ser anterior à data de fim", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao validar datas", Toast.LENGTH_SHORT).show();
            return;
        }
        
        atualizarTextoPeriodo();
        carregarPresencas();
    }
    
    private void limparFiltro() {
        // Voltar para últimos 30 dias
        Calendar cal = Calendar.getInstance();
        dataFim = formatoBanco.format(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, -30);
        dataInicio = formatoBanco.format(cal.getTime());
        
        btnDataInicio.setText("Data Início");
        btnDataFim.setText("Data Fim");
        
        atualizarTextoPeriodo();
        carregarPresencas();
    }
    
    private void atualizarTextoPeriodo() {
        try {
            String inicio = formatoExibicao.format(formatoBanco.parse(dataInicio));
            String fim = formatoExibicao.format(formatoBanco.parse(dataFim));
            textPeriodoAtual.setText("Período: " + inicio + " - " + fim);
        } catch (Exception e) {
            textPeriodoAtual.setText("Período: Todos os registros");
        }
    }
    
    private void carregarPresencas() {
        progressBar.setVisibility(View.VISIBLE);
        textMensagemVazia.setVisibility(View.GONE);
        recyclerViewPresencas.setVisibility(View.GONE);
        
        presencaRepository.listarPresencasPorPeriodo(dataInicio, dataFim, new PresencaLabRepository.OnListPresencasListener() {
            @Override
            public void onSuccess(List<PresencaLab> presencas) {
                progressBar.setVisibility(View.GONE);
                
                if (presencas.isEmpty()) {
                    textMensagemVazia.setVisibility(View.VISIBLE);
                    textMensagemVazia.setText("Nenhuma presença registrada no período selecionado");
                    recyclerViewPresencas.setVisibility(View.GONE);
                } else {
                    textMensagemVazia.setVisibility(View.GONE);
                    recyclerViewPresencas.setVisibility(View.VISIBLE);
                    
                    listaPresencas.clear();
                    listaPresencas.addAll(presencas);
                    adapter.notifyDataSetChanged();
                    
                    calcularEstatisticas(presencas);
                }
            }
            
            @Override
            public void onFailure(String erro) {
                progressBar.setVisibility(View.GONE);
                textMensagemVazia.setVisibility(View.VISIBLE);
                textMensagemVazia.setText("Erro ao carregar presenças: " + erro);
                Toast.makeText(RelatorioPresencasActivity.this, erro, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void calcularEstatisticas(List<PresencaLab> presencas) {
        int totalVisitas = presencas.size();
        
        // Contar membros únicos
        Map<String, Boolean> membrosUnicos = new HashMap<>();
        for (PresencaLab presenca : presencas) {
            membrosUnicos.put(presenca.getUsuarioId(), true);
        }
        int quantidadeMembros = membrosUnicos.size();
        
        // Atualizar estatísticas
        textTotalVisitas.setText(String.valueOf(totalVisitas));
        textMembrosUnicos.setText(String.valueOf(quantidadeMembros));
    }
    
    /**
     * Exporta o relatório para arquivo CSV e compartilha.
     */
    private void exportarCSV() {
        if (listaPresencas.isEmpty()) {
            Toast.makeText(this, "Nenhum dado para exportar", Toast.LENGTH_SHORT).show();
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
            String fileName = "relatorio_presencas_" + timestamp + ".csv";
            File csvFile = new File(exportDir, fileName);
            
            // Escrever CSV
            FileWriter writer = new FileWriter(csvFile);
            
            // Cabeçalho
            writer.append("Data,Horário Entrada,Horário Saída,Nome do Membro,Email,Status\n");
            
            // Dados
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            for (PresencaLab presenca : listaPresencas) {
                writer.append(presenca.getDataEntrada()).append(",");
                writer.append(presenca.getHoraEntrada() != null ? presenca.getHoraEntrada() : "-").append(",");
                writer.append(presenca.getHoraSaida() != null ? presenca.getHoraSaida() : "-").append(",");
                writer.append("\"").append(presenca.getUsuarioNome() != null ? presenca.getUsuarioNome() : "N/A").append("\"").append(",");
                writer.append(presenca.getUsuarioEmail() != null ? presenca.getUsuarioEmail() : "N/A").append(",");
                writer.append(presenca.getStatusDisplay()).append("\n");
            }
            
            writer.flush();
            writer.close();
            
            // Compartilhar arquivo
            Uri fileUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                csvFile
            );
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Relatório de Presenças - LaProBqi");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Relatório de presenças do laboratório.\nPeríodo: " + textPeriodoAtual.getText());
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(shareIntent, "Compartilhar relatório"));
            
            Toast.makeText(this, "Relatório exportado com sucesso!", Toast.LENGTH_LONG).show();
            
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao exportar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
