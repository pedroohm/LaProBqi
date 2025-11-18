package com.pedromoura.laprobqi.enums;

/**
 * Enum para os possíveis status de uma Reserva.
 */
public enum StatusReserva {
    PENDENTE("Pendente"),
    CONFIRMADA("Confirmada"),
    CANCELADA("Cancelada"),
    CONCLUIDA("Concluída");
    
    private final String descricao;
    
    StatusReserva(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Converte uma String para o enum correspondente.
     * 
     * @param status String do status
     * @return StatusReserva correspondente ou null se não encontrado
     */
    public static StatusReserva fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        
        for (StatusReserva s : StatusReserva.values()) {
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
