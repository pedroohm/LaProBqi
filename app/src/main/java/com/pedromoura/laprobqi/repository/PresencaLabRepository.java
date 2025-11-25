package com.pedromoura.laprobqi.repository;

import com.pedromoura.laprobqi.models.PresencaLab;

import java.util.List;

/**
 * Interface para repositório de presença no laboratório (Check-in/Check-out).
 */
public interface PresencaLabRepository {
    
    /**
     * Registra a entrada (check-in) de um membro no laboratório.
     */
    void registrarEntrada(PresencaLab presenca, OnCompleteListener listener);
    
    /**
     * Registra a saída (check-out) de um membro do laboratório.
     */
    void registrarSaida(String presencaId, String dataSaida, String horaSaida, OnCompleteListener listener);
    
    /**
     * Busca a presença ativa (status PRESENTE) de um usuário específico.
     */
    void buscarPresencaAtiva(String usuarioId, OnPresencaListener listener);
    
    /**
     * Lista todas as presenças de um período específico.
     */
    void listarPresencasPorPeriodo(String dataInicio, String dataFim, OnListPresencasListener listener);
    
    /**
     * Lista todas as presenças (para relatório do coordenador).
     */
    void listarTodasPresencas(OnListPresencasListener listener);
    
    // Callbacks
    interface OnCompleteListener {
        void onComplete(boolean sucesso, String mensagem);
    }
    
    interface OnPresencaListener {
        void onSuccess(PresencaLab presenca);
        void onNotFound();
        void onFailure(String mensagem);
    }
    
    interface OnListPresencasListener {
        void onSuccess(List<PresencaLab> presencas);
        void onFailure(String mensagem);
    }
}
