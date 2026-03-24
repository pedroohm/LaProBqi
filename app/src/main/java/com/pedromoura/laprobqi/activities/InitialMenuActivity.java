package com.pedromoura.laprobqi.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.di.RepositoryProvider;
import com.pedromoura.laprobqi.repository.UsuarioRepository;

public class InitialMenuActivity extends Activity {

    private UsuarioRepository usuarioRepository;
    private Button btnRelatorioPresencas;
    private Button btnAprovarSolicitacoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_menu);
        
        usuarioRepository = RepositoryProvider.getInstance(this).getUsuarioRepository();
        btnRelatorioPresencas = findViewById(R.id.btnRelatorioPresencas);
        btnAprovarSolicitacoes = findViewById(R.id.btnAprovarSolicitacoes);
        
        carregarUsuarioAtual();
    }
    
    private void carregarUsuarioAtual() {
        usuarioRepository.obterUsuarioAtual(usuario -> {
            if (usuario != null && usuario.isCoordenador()) {
                // Mostrar botões apenas para coordenadores
                btnRelatorioPresencas.setVisibility(View.VISIBLE);
                btnAprovarSolicitacoes.setVisibility(View.VISIBLE);
            } else {
                btnRelatorioPresencas.setVisibility(View.GONE);
                btnAprovarSolicitacoes.setVisibility(View.GONE);
            }
        });
    }

    public void clickWarehouse(View view) {
        Intent it = new Intent(this, StockOptionsActivity.class);
        startActivity(it);
    }

    public void clickSettings(View view) {
        Intent it = new Intent(this, SettingsActivity.class);
        startActivity(it);
    }

    public void clickCheckInOut(View view) {
        Intent intent = new Intent(this, CheckInOutActivity.class);
        startActivity(intent);
    }

    public void clickPopsAndGuides(View view) {
        Intent intent = new Intent(this, DocumentosActivity.class);
        startActivity(intent);
    }

    public void clickScheduleEquip(View view) {
        Intent it = new Intent(this, EquipamentosActivity.class);
        startActivity(it);
    }

    public void clickPoints(View view) {
        // TODO: Implementar sistema de pontos
        Toast.makeText(this, "Sistema de Pontuações", Toast.LENGTH_SHORT).show();
    }

    public void clickChallenge(View view) {
        // TODO: Implementar desafios
        Toast.makeText(this, "Sistema de Desafios de atividade física", Toast.LENGTH_SHORT).show();
    }
    
    public void clickHistoricoReservas(View view) {
        Intent intent = new Intent(this, HistoricoReservasActivity.class);
        startActivity(intent);
    }
    
    public void clickRelatorioPresencas(View view) {
        Intent intent = new Intent(this, RelatorioPresencasActivity.class);
        startActivity(intent);
    }
    
    public void clickAprovarSolicitacoes(View view) {
        Intent intent = new Intent(this, AprovarSolicitacoesActivity.class);
        startActivity(intent);
    }
}
