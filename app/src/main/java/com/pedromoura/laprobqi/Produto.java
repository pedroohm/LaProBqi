package com.pedromoura.laprobqi;

public class Produto {
    private int id;
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

    // Construtor sem ID (para inserção)
    public Produto(String nome, String tipo, String validade, double quantidade, String unidade, String observacoes) {
        this(-1, nome, tipo, validade, quantidade, unidade, observacoes);
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
}