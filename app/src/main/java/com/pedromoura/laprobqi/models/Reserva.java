package com.pedromoura.laprobqi.models;

import java.io.Serializable;

public class Reserva implements Serializable {
    private String id;
    private String equipamentoId;
    private String equipamentoNome;
    private String usuarioId;
    private String usuarioNome;
    private String dataReserva; // Data de início
    private String dataFim;     // Data de fim (para reservas de múltiplos dias)
    private String horaInicio;
    private String horaFim;
    private String status; // "ATIVA", "CANCELADA", "FINALIZADA"
    private String dataCriacao;

    public Reserva() {
    }

    public Reserva(String id, String equipamentoId, String equipamentoNome, String usuarioId, 
                   String usuarioNome, String dataReserva, String horaInicio, String horaFim) {
        this.id = id;
        this.equipamentoId = equipamentoId;
        this.equipamentoNome = equipamentoNome;
        this.usuarioId = usuarioId;
        this.usuarioNome = usuarioNome;
        this.dataReserva = dataReserva;
        this.dataFim = dataReserva;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.status = "ATIVA";
        this.dataCriacao = String.valueOf(System.currentTimeMillis());
    }

    public Reserva(String equipamentoId, String equipamentoNome, String usuarioId, 
                   String usuarioNome, String dataReserva, String horaInicio, String horaFim) {
        this(null, equipamentoId, equipamentoNome, usuarioId, usuarioNome, 
             dataReserva, horaInicio, horaFim);
    }

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

    public String getDataReserva() { return dataReserva; }
    public void setDataReserva(String dataReserva) { this.dataReserva = dataReserva; }
    
    public String getDataFim() { return dataFim; }
    public void setDataFim(String dataFim) { this.dataFim = dataFim; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getHoraFim() { return horaFim; }
    public void setHoraFim(String horaFim) { this.horaFim = horaFim; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(String dataCriacao) { this.dataCriacao = dataCriacao; }

    public boolean isAtiva() {
        return "ATIVA".equals(status);
    }

    public boolean isCancelada() {
        return "CANCELADA".equals(status);
    }

    public boolean isFinalizada() {
        return "FINALIZADA".equals(status);
    }

    public String getStatusDisplay() {
        switch (status) {
            case "ATIVA": return "Ativa";
            case "CANCELADA": return "Cancelada";
            case "FINALIZADA": return "Finalizada";
            default: return "Desconhecido";
        }
    }

    public String getPeriodoReserva() {
        if (dataFim != null && !dataFim.equals(dataReserva)) {
            return dataReserva + " " + horaInicio + " - " + dataFim + " " + horaFim;
        }
        return horaInicio + " - " + horaFim;
    }
}
