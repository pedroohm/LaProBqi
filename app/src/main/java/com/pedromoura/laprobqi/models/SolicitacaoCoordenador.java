package com.pedromoura.laprobqi.models;

import java.io.Serializable;

public class SolicitacaoCoordenador implements Serializable {
    private String id;
    private String nome;
    private String email;
    private String status; // "PENDENTE", "APROVADA", "REJEITADA"
    private String dataSolicitacao;
    private String coordenadorAprovadorId;
    private String coordenadorAprovadorNome;
    private String dataAprovacao;
    private String motivoRejeicao;
    
    public SolicitacaoCoordenador() {
    }
    
    public SolicitacaoCoordenador(String nome, String email) {
        this.nome = nome;
        this.email = email;
        this.status = "PENDENTE";
        this.dataSolicitacao = String.valueOf(System.currentTimeMillis());
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getDataSolicitacao() { return dataSolicitacao; }
    public void setDataSolicitacao(String dataSolicitacao) { this.dataSolicitacao = dataSolicitacao; }
    
    public String getCoordenadorAprovadorId() { return coordenadorAprovadorId; }
    public void setCoordenadorAprovadorId(String coordenadorAprovadorId) { 
        this.coordenadorAprovadorId = coordenadorAprovadorId; 
    }
    
    public String getCoordenadorAprovadorNome() { return coordenadorAprovadorNome; }
    public void setCoordenadorAprovadorNome(String coordenadorAprovadorNome) { 
        this.coordenadorAprovadorNome = coordenadorAprovadorNome; 
    }
    
    public String getDataAprovacao() { return dataAprovacao; }
    public void setDataAprovacao(String dataAprovacao) { this.dataAprovacao = dataAprovacao; }
    
    public String getMotivoRejeicao() { return motivoRejeicao; }
    public void setMotivoRejeicao(String motivoRejeicao) { this.motivoRejeicao = motivoRejeicao; }
    
    public boolean isPendente() {
        return "PENDENTE".equals(status);
    }
    
    public boolean isAprovada() {
        return "APROVADA".equals(status);
    }
    
    public boolean isRejeitada() {
        return "REJEITADA".equals(status);
    }
}
