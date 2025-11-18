package com.pedromoura.laprobqi.models;

public class Usuario {
    private String id;
    private String nome;
    private String email;
    private String nivelAcesso; // "ALUNO" ou "COORDENADOR"

    public Usuario() {
        // Construtor vazio necessário para Firebase
    }

    public Usuario(String id, String nome, String email, String nivelAcesso) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.nivelAcesso = nivelAcesso;
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }


    public String getNivelAcesso() { return nivelAcesso; }
    public void setNivelAcesso(String nivelAcesso) { this.nivelAcesso = nivelAcesso; }

    public boolean isCoordenador() {
        return "COORDENADOR".equals(nivelAcesso);
    }
}