package com.pedromoura.laprobqi.repository;

import com.pedromoura.laprobqi.models.Reserva;
import java.util.List;

public interface ReservaRepository {
    
    interface OnReservaListener {
        void onSuccess(Reserva reserva);
        void onFailure(String mensagem);
    }
    
    interface OnReservasListener {
        void onSuccess(List<Reserva> reservas);
        void onFailure(String mensagem);
    }
    
    interface OnBooleanListener {
        void onSuccess(boolean success);
        void onFailure(String mensagem);
    }
    
    // CRUD básico
    void salvarReserva(Reserva reserva, OnReservaListener listener);
    void obterReserva(String id, OnReservaListener listener);
    void obterTodasReservas(OnReservasListener listener);
    void atualizarReserva(Reserva reserva, OnBooleanListener listener);
    void excluirReserva(String id, OnBooleanListener listener);
    
    // Funcionalidades específicas
    void obterReservasPorUsuario(String usuarioId, OnReservasListener listener);
    void obterReservasPorEquipamento(String equipamentoId, OnReservasListener listener);
    void obterReservasAtivas(OnReservasListener listener);
    void cancelarReserva(String reservaId, OnBooleanListener listener);
    void verificarConflitoReserva(String equipamentoId, String data, String horaInicio, String horaFim, OnBooleanListener listener);
    void obterReservasPorPeriodo(String dataInicio, String dataFim, OnReservasListener listener);
}
