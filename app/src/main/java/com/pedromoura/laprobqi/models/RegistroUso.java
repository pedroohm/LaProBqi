package com.pedromoura.laprobqi.models;

import java.io.Serializable;

public class RegistroUso implements Serializable {
    private String id;
    private String equipamentoId;
    private String equipamentoNome;
    private String usuarioId;
    private String usuarioNome;
    private String reservaId;
    private String dataInicio;
    private String horaInicio;
    private String dataFim;
    private String horaFim;
    private String status; // "EM_ANDAMENTO", "FINALIZADO"
    private String observacoes;

    public RegistroUso() {
        // Construtor vazio necessário para Firebase
    }

    public RegistroUso(String id, String equipamentoId, String equipamentoNome, String usuarioId, 
                      String usuarioNome, String reservaId, String dataInicio, String horaInicio) {
        this.id = id;
        this.equipamentoId = equipamentoId;
        this.equipamentoNome = equipamentoNome;
        this.usuarioId = usuarioId;
        this.usuarioNome = usuarioNome;
        this.reservaId = reservaId;
        this.dataInicio = dataInicio;
        this.horaInicio = horaInicio;
        this.status = "EM_ANDAMENTO";
        this.dataFim = null;
        this.horaFim = null;
    }

    // Construtor sem ID (para inserção)
    public RegistroUso(String equipamentoId, String equipamentoNome, String usuarioId, 
                      String usuarioNome, String reservaId, String dataInicio, String horaInicio) {
        this(null, equipamentoId, equipamentoNome, usuarioId, usuarioNome, 
             reservaId, dataInicio, horaInicio);
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEquipamentoId() { return equipamentoId; }
    public void setEquipamentoId(String equipamentoId) { this.equipamentoId = equipamentoId; }

    public String getEquipamentoNome() { return equipamentoNome; }
    public void setEquipamentoNome(String equipamentoNome) { this.equipamentoNome = equipamentoNome; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }

    public String getReservaId() { return reservaId; }
    public void setReservaId(String reservaId) { this.reservaId = reservaId; }

    public String getDataInicio() { return dataInicio; }
    public void setDataInicio(String dataInicio) { this.dataInicio = dataInicio; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getDataFim() { return dataFim; }
    public void setDataFim(String dataFim) { this.dataFim = dataFim; }

    public String getHoraFim() { return horaFim; }
    public void setHoraFim(String horaFim) { this.horaFim = horaFim; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    // Métodos auxiliares
    public boolean isEmAndamento() {
        return "EM_ANDAMENTO".equals(status);
    }

    public boolean isFinalizado() {
        return "FINALIZADO".equals(status);
    }

    public String getStatusDisplay() {
        switch (status) {
            case "EM_ANDAMENTO": return "Em Andamento";
            case "FINALIZADO": return "Finalizado";
            default: return "Desconhecido";
        }
    }

    public String getDuracaoUso() {
        if (dataFim != null && horaFim != null) {
            return "De " + dataInicio + " " + horaInicio + " até " + dataFim + " " + horaFim;
        }
        return "Iniciado em " + dataInicio + " " + horaInicio;
    }
}
