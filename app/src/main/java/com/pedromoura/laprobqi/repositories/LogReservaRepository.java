package com.pedromoura.laprobqi.repositories;

import com.pedromoura.laprobqi.models.LogReserva;

import java.util.Date;
import java.util.List;

public interface LogReservaRepository {
    
    interface OnLogSavedListener {
        void onSuccess(String logId);
        void onError(String mensagem);
    }
    
    interface OnLogsLoadedListener {
        void onSuccess(List<LogReserva> logs);
        void onError(String mensagem);
    }

    /**
     * Salva um log de reserva
     */
    void salvarLog(LogReserva log, OnLogSavedListener listener);

    /**
     * Busca todos os logs de reservas
     */
    void buscarTodosLogs(OnLogsLoadedListener listener);

    /**
     * Busca logs por equipamento
     */
    void buscarLogsPorEquipamento(String equipamentoId, OnLogsLoadedListener listener);

    /**
     * Busca logs por usuário
     */
    void buscarLogsPorUsuario(String usuarioId, OnLogsLoadedListener listener);

    /**
     * Busca logs por período
     */
    void buscarLogsPorPeriodo(Date dataInicio, Date dataFim, OnLogsLoadedListener listener);

    /**
     * Busca logs por status
     */
    void buscarLogsPorStatus(String status, OnLogsLoadedListener listener);

    /**
     * Atualiza o status de um log
     */
    void atualizarStatus(String logId, String novoStatus, OnLogSavedListener listener);

    /**
     * Cancela uma reserva (atualiza log)
     */
    void cancelarReserva(String logId, String motivo, OnLogSavedListener listener);
}
