package com.pedromoura.laprobqi;

/**
 * Classe que representa um Produto do sistema de estoque.
 * 
 * IMPORTANTE: O campo 'id' é usado apenas para compatibilidade com SQLite.
 * No Firebase, cada produto é identificado pelo ID do documento gerado automaticamente.
 * Por isso, ao criar novos produtos, o id é definido como -1 (valor placeholder).
 */
public class Produto {
    private int id; // Usado apenas no SQLite, não é enviado ao Firebase
    private String nome;
    private String tipo;
    private String validade;
    private double quantidade;
    private String unidade;
    private String observacoes;

    public Produto(int id, String nome, String tipo, String validade, double quantidade, String unidade, String observacoes) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.validade = validade;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.observacoes = observacoes;
    }

    // Construtor sem ID (para inserção no Firebase - não usa ID numérico)
    public Produto(String nome, String tipo, String validade, double quantidade, String unidade, String observacoes) {
        this.id = -1; // ID temporário, Firebase gerará seu próprio ID
        this.nome = nome;
        this.tipo = tipo;
        this.validade = validade;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.observacoes = observacoes;
    }

    // Getters e Setters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public String getValidade() { return validade; }
    public double getQuantidade() { return quantidade; }
    public String getUnidade() { return unidade; }
    public String getObservacoes() { return observacoes; }

    public void setId(int id) { this.id = id; }
    public void setQuantidade(double quantidade) { this.quantidade = quantidade; }
}