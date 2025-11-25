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
    
    // Novos campos para categorização
    private String categoria;
    private String codigo;
    private String cor;
    private String hexColor;

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
    public String getCategoria() { return categoria; }
    public String getCodigo() { return codigo; }
    public String getCor() { return cor; }
    public String getHexColor() { return hexColor; }

    public void setId(int id) { this.id = id; }
    public void setQuantidade(double quantidade) { this.quantidade = quantidade; }
    
    /**
     * Define a categoria do produto e automaticamente atualiza código, cor e hexColor
     */
    public void setCategoria(String codigo) {
        this.codigo = codigo;
        this.categoria = getCategoriaFromCodigo(codigo);
        this.cor = getCorFromCodigo(codigo);
        this.hexColor = getHexFromCodigo(codigo);
    }
    
    /**
     * Mapeia código para nome da categoria
     */
    private String getCategoriaFromCodigo(String codigo) {
        switch (codigo) {
            case "COM": return "Reagentes Controlados";
            case "PAA": return "Proteínas e Aminoácidos";
            case "RGF": return "Reagentes Geladeira/Freezer";
            case "COR": return "Corantes";
            case "SAIS": return "Sais";
            case "ENZ": return "Enzimas";
            case "MDC": return "Meios de Cultura";
            case "ACU": return "Açúcares";
            case "FFT": return "Fosfatos";
            case "BASE": return "Bases";
            case "IND": 
            default: return "Reagentes não classificados/Indefinidos";
        }
    }
    
    /**
     * Mapeia código para cor
     */
    private String getCorFromCodigo(String codigo) {
        switch (codigo) {
            case "COM": return "Azul";
            case "PAA": return "Verde escuro";
            case "RGF": return "Azul escuro";
            case "COR": return "Vinho";
            case "SAIS": return "Laranja";
            case "ENZ": return "Azul Claro";
            case "MDC": return "Roxo";
            case "ACU": return "Rosa";
            case "FFT": return "Lilás";
            case "BASE": return "Amarelo";
            case "IND": 
            default: return "Cinza";
        }
    }
    
    /**
     * Mapeia código para código hexadecimal da cor
     */
    private String getHexFromCodigo(String codigo) {
        switch (codigo) {
            case "COM": return "#2196F3";       // Azul
            case "PAA": return "#1B5E20";       // Verde escuro
            case "RGF": return "#0D47A1";       // Azul escuro
            case "COR": return "#880E4F";       // Vinho
            case "SAIS": return "#FF6F00";      // Laranja
            case "ENZ": return "#81D4FA";       // Azul Claro
            case "MDC": return "#7B1FA2";       // Roxo
            case "ACU": return "#F48FB1";       // Rosa
            case "FFT": return "#BA68C8";       // Lilás
            case "BASE": return "#FFEB3B";      // Amarelo
            case "IND": 
            default: return "#9E9E9E";          // Cinza
        }
    }
    
    /**
     * Retorna todas as categorias disponíveis
     */
    public static String[] getCategorias() {
        return new String[]{
            "Reagentes Controlados",
            "Proteínas e Aminoácidos",
            "Reagentes Geladeira/Freezer",
            "Corantes",
            "Sais",
            "Enzimas",
            "Meios de Cultura",
            "Açúcares",
            "Fosfatos",
            "Bases",
            "Reagentes não classificados/Indefinidos"
        };
    }
    
    /**
     * Retorna todos os códigos disponíveis
     */
    public static String[] getCodigos() {
        return new String[]{"COM", "PAA", "RGF", "COR", "SAIS", "ENZ", "MDC", "ACU", "FFT", "BASE", "IND"};
    }
}