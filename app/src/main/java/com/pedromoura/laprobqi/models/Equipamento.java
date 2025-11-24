package com.pedromoura.laprobqi.models;

import java.io.Serializable;

public class Equipamento implements Serializable {
    private String id;
    private String nome;
    private String descricao;
    private String status; // "DISPONIVEL", "RESERVADO", "EM_USO"
    private boolean emManutencao; // Bloqueado para manutenção
    private String dataCriacao;

    public Equipamento() {
        // Construtor vazio necessário para Firebase
    }

    public Equipamento(String id, String nome, String descricao, String status) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.status = status;
        this.dataCriacao = String.valueOf(System.currentTimeMillis());
    }

    // Construtor sem ID (para inserção)
    public Equipamento(String nome, String descricao) {
        this(null, nome, descricao, "DISPONIVEL");
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(String dataCriacao) { this.dataCriacao = dataCriacao; }

    public boolean isEmManutencao() { return emManutencao; }
    public void setEmManutencao(boolean emManutencao) { this.emManutencao = emManutencao; }

    // Métodos auxiliares
    public boolean isDisponivel() {
        return "DISPONIVEL".equals(status);
    }

    public boolean isReservado() {
        return "RESERVADO".equals(status);
    }

    public boolean isEmUso() {
        return "EM_USO".equals(status);
    }

    public String getStatusDisplay() {
        if (emManutencao) {
            return "🔧 Em Manutenção";
        }
        switch (status) {
            case "DISPONIVEL": return "✅ Disponível";
            case "RESERVADO": return "📅 Reservado";
            case "EM_USO": return "⚙️ Em Uso";
            default: return "Desconhecido";
        }
    }
}
