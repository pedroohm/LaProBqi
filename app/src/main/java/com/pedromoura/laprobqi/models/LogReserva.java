package com.pedromoura.laprobqi.models;

import java.util.Date;

public class LogReserva {
    private String id;
    private String equipamentoId;
    private String equipamentoNome;
    private String usuarioId;
    private String usuarioNome;
    private String usuarioEmail;
    private Date dataHoraInicio;
    private Date dataHoraFim;
    private Date dataHoraReserva; // Quando a reserva foi feita
    private String status; // ATIVA, CONCLUIDA, CANCELADA
    private String observacao;
    private Date dataHoraCancelamento;
    private String motivoCancelamento;

    public LogReserva() {
        // Construtor vazio necessário para Firebase
    }

    public LogReserva(String equipamentoId, String equipamentoNome, String usuarioId, 
                      String usuarioNome, String usuarioEmail, Date dataHoraInicio, 
                      Date dataHoraFim, String status) {
        this.equipamentoId = equipamentoId;
        this.equipamentoNome = equipamentoNome;
        this.usuarioId = usuarioId;
        this.usuarioNome = usuarioNome;
        this.usuarioEmail = usuarioEmail;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.dataHoraReserva = new Date();
        this.status = status;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEquipamentoId() {
        return equipamentoId;
    }

    public void setEquipamentoId(String equipamentoId) {
        this.equipamentoId = equipamentoId;
    }

    public String getEquipamentoNome() {
        return equipamentoNome;
    }

    public void setEquipamentoNome(String equipamentoNome) {
        this.equipamentoNome = equipamentoNome;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public void setUsuarioNome(String usuarioNome) {
        this.usuarioNome = usuarioNome;
    }

    public String getUsuarioEmail() {
        return usuarioEmail;
    }

    public void setUsuarioEmail(String usuarioEmail) {
        this.usuarioEmail = usuarioEmail;
    }

    public Date getDataHoraInicio() {
        return dataHoraInicio;
    }

    public void setDataHoraInicio(Date dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public Date getDataHoraFim() {
        return dataHoraFim;
    }

    public void setDataHoraFim(Date dataHoraFim) {
        this.dataHoraFim = dataHoraFim;
    }

    public Date getDataHoraReserva() {
        return dataHoraReserva;
    }

    public void setDataHoraReserva(Date dataHoraReserva) {
        this.dataHoraReserva = dataHoraReserva;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Date getDataHoraCancelamento() {
        return dataHoraCancelamento;
    }

    public void setDataHoraCancelamento(Date dataHoraCancelamento) {
        this.dataHoraCancelamento = dataHoraCancelamento;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public void setMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
    }

    // Métodos auxiliares
    public String getStatusDisplay() {
        switch (status) {
            case "ATIVA":
                return "🟢 Ativa";
            case "CONCLUIDA":
                return "✅ Concluída";
            case "CANCELADA":
                return "❌ Cancelada";
            default:
                return status;
        }
    }

    public boolean isAtiva() {
        return "ATIVA".equals(status);
    }

    public boolean isConcluida() {
        return "CONCLUIDA".equals(status);
    }

    public boolean isCancelada() {
        return "CANCELADA".equals(status);
    }
}
