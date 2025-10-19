package com.pedromoura.laprobqi.repository;

import com.pedromoura.laprobqi.models.Equipamento;
import java.util.List;

public interface EquipamentoRepository {
    
    interface OnEquipamentoListener {
        void onSuccess(Equipamento equipamento);
        void onFailure(String mensagem);
    }
    
    interface OnEquipamentosListener {
        void onSuccess(List<Equipamento> equipamentos);
        void onFailure(String mensagem);
    }
    
    interface OnBooleanListener {
        void onSuccess(boolean success);
        void onFailure(String mensagem);
    }
    
    // CRUD básico
    void salvarEquipamento(Equipamento equipamento, OnEquipamentoListener listener);
    void obterEquipamento(String id, OnEquipamentoListener listener);
    void obterTodosEquipamentos(OnEquipamentosListener listener);
    void atualizarEquipamento(Equipamento equipamento, OnBooleanListener listener);
    void excluirEquipamento(String id, OnBooleanListener listener);
    
    // Funcionalidades específicas
    void obterEquipamentosDisponiveis(OnEquipamentosListener listener);
    void buscarEquipamentosPorNome(String nome, OnEquipamentosListener listener);
    void obterEquipamentosDisponiveisPorPeriodo(String data, String horaInicio, String horaFim, OnEquipamentosListener listener);
    void atualizarStatusEquipamento(String equipamentoId, String novoStatus, OnBooleanListener listener);
}
