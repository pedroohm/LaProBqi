package com.pedromoura.laprobqi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.di.RepositoryProvider;
import com.pedromoura.laprobqi.models.Equipamento;
import com.pedromoura.laprobqi.models.Usuario;
import com.pedromoura.laprobqi.repository.EquipamentoRepository;
import com.pedromoura.laprobqi.repository.UsuarioRepository;

public class DetalhesEquipamentoActivity extends AppCompatActivity {

    private TextView textViewNome, textViewDescricao, textViewStatus;
    private Button btnReservar, btnIniciarUso, btnFinalizarUso, btnVoltar, btnExcluir;
    private SwitchMaterial switchManutencao;
    private View layoutCoordenador;
    
    private Equipamento equipamento;
    private Usuario usuarioAtual;
    private EquipamentoRepository equipamentoRepository;
    private UsuarioRepository usuarioRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_equipamento);

        // Obter equipamento da intent
        equipamento = (Equipamento) getIntent().getSerializableExtra("equipamento");
        if (equipamento == null) {
            showToast("Equipamento não encontrado");
            finish();
            return;
        }

        // Inicializar repositories
        equipamentoRepository = RepositoryProvider.getInstance(this).getEquipamentoRepository();
        usuarioRepository = RepositoryProvider.getInstance(this).getUsuarioRepository();

        // Inicializar views
        inicializarViews();
        
        // Carregar usuário atual
        carregarUsuarioAtual();
        
        // Exibir detalhes do equipamento
        exibirDetalhes();
    }

    private void inicializarViews() {
        textViewNome = findViewById(R.id.textViewNome);
        textViewDescricao = findViewById(R.id.textViewDescricao);
        textViewStatus = findViewById(R.id.textViewStatus);
        btnReservar = findViewById(R.id.btnReservar);
        btnIniciarUso = findViewById(R.id.btnIniciarUso);
        btnFinalizarUso = findViewById(R.id.btnFinalizarUso);
        btnVoltar = findViewById(R.id.btnVoltar);
        
        // Controles de coordenador
        layoutCoordenador = findViewById(R.id.layoutCoordenador);
        switchManutencao = findViewById(R.id.switchManutencao);
        btnExcluir = findViewById(R.id.btnExcluir);

        // Configurar listeners
        btnReservar.setOnClickListener(v -> abrirTelaReservar());
        btnIniciarUso.setOnClickListener(v -> iniciarUso());
        btnFinalizarUso.setOnClickListener(v -> finalizarUso());
        btnVoltar.setOnClickListener(v -> finish());
        
        // Listeners de coordenador
        switchManutencao.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) { // Evita trigger no carregamento inicial
                alternarManutencao(isChecked);
            }
        });
        btnExcluir.setOnClickListener(v -> confirmarExclusao());
    }

    private void carregarUsuarioAtual() {
        usuarioRepository.obterUsuarioAtual(usuario -> {
            usuarioAtual = usuario;
            
            // Mostrar controles de coordenador apenas para coordenadores
            if (usuario != null && usuario.isCoordenador()) {
                layoutCoordenador.setVisibility(View.VISIBLE);
                switchManutencao.setChecked(equipamento.isEmManutencao());
            } else {
                layoutCoordenador.setVisibility(View.GONE);
            }
            
            atualizarBotoes();
        });
    }

    private void exibirDetalhes() {
        textViewNome.setText(equipamento.getNome());
        textViewDescricao.setText(equipamento.getDescricao());
        textViewStatus.setText("Status: " + equipamento.getStatusDisplay());
    }

    private void atualizarBotoes() {
        if (usuarioAtual == null) return;

        // Se equipamento está em manutenção, desabilita todos os botões de uso
        if (equipamento.isEmManutencao()) {
            btnReservar.setVisibility(View.GONE);
            btnIniciarUso.setVisibility(View.GONE);
            btnFinalizarUso.setVisibility(View.GONE);
            return;
        }

        // Lógica baseada no status do equipamento
        switch (equipamento.getStatus()) {
            case "DISPONIVEL":
                btnReservar.setVisibility(View.VISIBLE);
                btnIniciarUso.setVisibility(View.GONE);
                btnFinalizarUso.setVisibility(View.GONE);
                break;
                
            case "RESERVADO":
                btnReservar.setVisibility(View.GONE);
                btnIniciarUso.setVisibility(View.VISIBLE);
                btnFinalizarUso.setVisibility(View.GONE);
                break;
                
            case "EM_USO":
                btnReservar.setVisibility(View.GONE);
                btnIniciarUso.setVisibility(View.GONE);
                btnFinalizarUso.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void abrirTelaReservar() {
        Intent intent = new Intent(this, ReservarEquipamentoActivity.class);
        intent.putExtra("equipamento_id", equipamento.getId());
        startActivity(intent);
    }

    private void iniciarUso() {
        if (usuarioAtual == null) {
            showToast("Usuário não encontrado");
            return;
        }

        // Aqui você implementaria a lógica para iniciar o uso
        // Por enquanto, apenas atualizamos o status do equipamento
        equipamentoRepository.atualizarStatusEquipamento(equipamento.getId(), "EM_USO",
            new EquipamentoRepository.OnBooleanListener() {
                @Override
                public void onSuccess(boolean success) {
                    if (success) {
                        equipamento.setStatus("EM_USO");
                        exibirDetalhes();
                        atualizarBotoes();
                        showToast("Uso iniciado com sucesso!");
                    } else {
                        showToast("Erro ao iniciar uso");
                    }
                }

                @Override
                public void onFailure(String mensagem) {
                    showToast("Erro: " + mensagem);
                }
            });
    }

    private void finalizarUso() {
        if (usuarioAtual == null) {
            showToast("Usuário não encontrado");
            return;
        }

        // Aqui você implementaria a lógica para finalizar o uso
        // Por enquanto, apenas atualizamos o status do equipamento
        equipamentoRepository.atualizarStatusEquipamento(equipamento.getId(), "DISPONIVEL",
            new EquipamentoRepository.OnBooleanListener() {
                @Override
                public void onSuccess(boolean success) {
                    if (success) {
                        equipamento.setStatus("DISPONIVEL");
                        exibirDetalhes();
                        atualizarBotoes();
                        showToast("Uso finalizado com sucesso!");
                    } else {
                        showToast("Erro ao finalizar uso");
                    }
                }

                @Override
                public void onFailure(String mensagem) {
                    showToast("Erro: " + mensagem);
                }
            });
    }

    /**
     * Alterna o status de manutenção do equipamento.
     */
    private void alternarManutencao(boolean emManutencao) {
        equipamentoRepository.bloquearParaManutencao(equipamento.getId(), emManutencao,
            new EquipamentoRepository.OnBooleanListener() {
                @Override
                public void onSuccess(boolean success) {
                    if (success) {
                        equipamento.setEmManutencao(emManutencao);
                        exibirDetalhes();
                        atualizarBotoes();
                        String mensagem = emManutencao ? 
                            "Equipamento bloqueado para manutenção" : 
                            "Equipamento desbloqueado";
                        showToast(mensagem);
                    } else {
                        // Reverter switch se falhou
                        switchManutencao.setChecked(!emManutencao);
                        showToast("Erro ao atualizar status de manutenção");
                    }
                }

                @Override
                public void onFailure(String mensagem) {
                    // Reverter switch se falhou
                    switchManutencao.setChecked(!emManutencao);
                    showToast("Erro: " + mensagem);
                }
            });
    }

    /**
     * Confirma antes de excluir o equipamento.
     */
    private void confirmarExclusao() {
        new AlertDialog.Builder(this)
            .setTitle("Excluir Equipamento")
            .setMessage("Tem certeza que deseja excluir este equipamento?\n\n" +
                       "Esta ação não pode ser desfeita e todas as reservas associadas serão perdidas.")
            .setPositiveButton("Excluir", (dialog, which) -> excluirEquipamento())
            .setNegativeButton("Cancelar", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    /**
     * Exclui o equipamento do Firebase.
     */
    private void excluirEquipamento() {
        btnExcluir.setEnabled(false);
        
        equipamentoRepository.excluirEquipamento(equipamento.getId(),
            new EquipamentoRepository.OnBooleanListener() {
                @Override
                public void onSuccess(boolean success) {
                    if (success) {
                        showToast("Equipamento excluído com sucesso");
                        finish(); // Volta para a tela anterior
                    } else {
                        btnExcluir.setEnabled(true);
                        showToast("Erro ao excluir equipamento");
                    }
                }

                @Override
                public void onFailure(String mensagem) {
                    btnExcluir.setEnabled(true);
                    showToast("Erro: " + mensagem);
                }
            });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
