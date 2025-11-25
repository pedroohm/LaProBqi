package com.pedromoura.laprobqi.models;

import java.io.Serializable;

/**
 * Modelo para registro de presença no laboratório (Check-in/Check-out).
 * Registra entrada e saída de membros para controle de frequência.
 */
public class PresencaLab implements Serializable {
    
    private String id;
    private String usuarioId;
    private String usuarioNome;
    private String usuarioEmail;
    private String dataEntrada;      // Formato: yyyy-MM-dd
    private String horaEntrada;      // Formato: HH:mm
    private String dataSaida;        // Formato: yyyy-MM-dd
    private String horaSaida;        // Formato: HH:mm
    private String status;           // "PRESENTE" ou "SAIU"
    
    // Construtor vazio necessário para Firebase
    public PresencaLab() {
    }
    
    // Construtor para check-in (entrada)
    public PresencaLab(String usuarioId, String usuarioNome, String usuarioEmail, 
                       String dataEntrada, String horaEntrada) {
        this.usuarioId = usuarioId;
        this.usuarioNome = usuarioNome;
        this.usuarioEmail = usuarioEmail;
        this.dataEntrada = dataEntrada;
        this.horaEntrada = horaEntrada;
        this.status = "PRESENTE";
        this.dataSaida = null;
        this.horaSaida = null;
    }
    
    // Método para registrar saída (check-out)
    public void registrarSaida(String dataSaida, String horaSaida) {
        this.dataSaida = dataSaida;
        this.horaSaida = horaSaida;
        this.status = "SAIU";
    }
    
    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    
    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }
    
    public String getUsuarioEmail() { return usuarioEmail; }
    public void setUsuarioEmail(String usuarioEmail) { this.usuarioEmail = usuarioEmail; }
    
    public String getDataEntrada() { return dataEntrada; }
    public void setDataEntrada(String dataEntrada) { this.dataEntrada = dataEntrada; }
    
    public String getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(String horaEntrada) { this.horaEntrada = horaEntrada; }
    
    public String getDataSaida() { return dataSaida; }
    public void setDataSaida(String dataSaida) { this.dataSaida = dataSaida; }
    
    public String getHoraSaida() { return horaSaida; }
    public void setHoraSaida(String horaSaida) { this.horaSaida = horaSaida; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    // Métodos auxiliares
    public boolean isPresente() {
        return "PRESENTE".equals(status);
    }
    
    public boolean isSaiu() {
        return "SAIU".equals(status);
    }
    
    public String getStatusDisplay() {
        return isPresente() ? "🟢 Presente" : "🔴 Saiu";
    }
}
