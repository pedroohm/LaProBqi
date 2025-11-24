package com.pedromoura.laprobqi.repository;

import com.pedromoura.laprobqi.model.ConfiguracaoDocumentos;

/**
 * Interface para gerenciar configurações de documentos (POPs e Manuais).
 */
public interface DocumentoRepository {
    
    /**
     * Obtém as configurações atuais dos documentos.
     */
    void obterConfiguracao(OnSuccessListener<ConfiguracaoDocumentos> listener);
    
    /**
     * Salva as configurações dos documentos (apenas coordenador).
     */
    void salvarConfiguracao(ConfiguracaoDocumentos config, OnSuccessListener<Void> listener);
    
    // Callbacks
    interface OnSuccessListener<T> {
        void onSuccess(T result);
    }
}
