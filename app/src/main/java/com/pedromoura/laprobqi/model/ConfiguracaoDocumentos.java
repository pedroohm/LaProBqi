package com.pedromoura.laprobqi.model;

/**
 * Configurações de acesso aos documentos (POPs e Manuais) no Google Drive.
 */
public class ConfiguracaoDocumentos {
    private String urlPastaGoogleDrive;
    private String ultimaAtualizacao; // String no formato dd/MM/yyyy HH:mm
    private String atualizadoPor; // Email do coordenador
    private String instrucoes; // Instruções opcionais para os usuários

    public ConfiguracaoDocumentos() {
        // Construtor vazio necessário para Firestore
    }

    public ConfiguracaoDocumentos(String urlPastaGoogleDrive, String ultimaAtualizacao, String atualizadoPor, String instrucoes) {
        this.urlPastaGoogleDrive = urlPastaGoogleDrive;
        this.ultimaAtualizacao = ultimaAtualizacao;
        this.atualizadoPor = atualizadoPor;
        this.instrucoes = instrucoes;
    }

    // Getters e Setters
    public String getUrlPastaGoogleDrive() {
        return urlPastaGoogleDrive;
    }

    public void setUrlPastaGoogleDrive(String urlPastaGoogleDrive) {
        this.urlPastaGoogleDrive = urlPastaGoogleDrive;
    }

    public String getUltimaAtualizacao() {
        return ultimaAtualizacao;
    }

    public void setUltimaAtualizacao(String ultimaAtualizacao) {
        this.ultimaAtualizacao = ultimaAtualizacao;
    }

    public String getAtualizadoPor() {
        return atualizadoPor;
    }

    public void setAtualizadoPor(String atualizadoPor) {
        this.atualizadoPor = atualizadoPor;
    }

    public String getInstrucoes() {
        return instrucoes;
    }

    public void setInstrucoes(String instrucoes) {
        this.instrucoes = instrucoes;
    }
}
