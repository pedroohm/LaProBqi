package com.pedromoura.laprobqi.enums;

/**
 * Enum para os possíveis status de um Registro de Uso.
 */
public enum StatusRegistroUso {
    EM_ANDAMENTO("Em Andamento"),
    FINALIZADO("Finalizado"),
    CANCELADO("Cancelado");
    
    private final String descricao;
    
    StatusRegistroUso(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Converte uma String para o enum correspondente.
     * 
     * @param status String do status
     * @return StatusRegistroUso correspondente ou null se não encontrado
     */
    public static StatusRegistroUso fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        
        for (StatusRegistroUso s : StatusRegistroUso.values()) {
            if (s.getDescricao().equalsIgnoreCase(status.trim()) || 
                s.name().equalsIgnoreCase(status.trim())) {
                return s;
            }
        }
        return null;
    }
    
    /**
     * Valida se uma String representa um status válido.
     * 
     * @param status String a ser validada
     * @return true se for um status válido, false caso contrário
     */
    public static boolean isValid(String status) {
        return fromString(status) != null;
    }
}
