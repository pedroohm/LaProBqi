package com.pedromoura.laprobqi.repository;

import com.pedromoura.laprobqi.models.RegistroUso;
import java.util.List;

public interface RegistroUsoRepository {
    
    interface OnRegistroUsoListener {
        void onSuccess(RegistroUso registroUso);
        void onFailure(String mensagem);
    }
    
    interface OnRegistrosUsoListener {
        void onSuccess(List<RegistroUso> registrosUso);
        void onFailure(String mensagem);
    }
    
    interface OnBooleanListener {
        void onSuccess(boolean success);
        void onFailure(String mensagem);
    }
    
    // CRUD básico
    void salvarRegistroUso(RegistroUso registroUso, OnRegistroUsoListener listener);
    void obterRegistroUso(String id, OnRegistroUsoListener listener);
    void obterTodosRegistrosUso(OnRegistrosUsoListener listener);
    void atualizarRegistroUso(RegistroUso registroUso, OnBooleanListener listener);
    void excluirRegistroUso(String id, OnBooleanListener listener);
    
    // Funcionalidades específicas
    void obterRegistrosUsoPorUsuario(String usuarioId, OnRegistrosUsoListener listener);
    void obterRegistrosUsoPorEquipamento(String equipamentoId, OnRegistrosUsoListener listener);
    void obterRegistrosUsoEmAndamento(OnRegistrosUsoListener listener);
    void finalizarRegistroUso(String registroId, String observacoes, OnBooleanListener listener);
    void obterRegistrosUsoPorPeriodo(String dataInicio, String dataFim, OnRegistrosUsoListener listener);
    void obterRegistrosUsoPorUsuarioEPeriodo(String usuarioId, String dataInicio, String dataFim, OnRegistrosUsoListener listener);
}
