package com.pedromoura.laprobqi.repository.impl;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.pedromoura.laprobqi.model.ConfiguracaoDocumentos;
import com.pedromoura.laprobqi.repository.DocumentoRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementação Firestore do repositório de configurações de documentos.
 */
public class DocumentoRepositoryFirestore implements DocumentoRepository {
    
    private static final String TAG = "DocumentoRepoFirestore";
    private static final String COLLECTION_CONFIG = "configuracoes";
    private static final String DOC_DOCUMENTOS = "documentos";
    
    private final FirebaseFirestore firestore;
    
    public DocumentoRepositoryFirestore() {
        this.firestore = FirebaseFirestore.getInstance();
    }
    
    /**
     * Obtém as configurações atuais dos documentos do Firestore.
     */
    @Override
    public void obterConfiguracao(OnSuccessListener<ConfiguracaoDocumentos> listener) {
        firestore.collection(COLLECTION_CONFIG)
                .document(DOC_DOCUMENTOS)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ConfiguracaoDocumentos config = documentSnapshot.toObject(ConfiguracaoDocumentos.class);
                        if (config != null) {
                            Log.d(TAG, "Configuração obtida: " + config.getUrlPastaGoogleDrive());
                            listener.onSuccess(config);
                        } else {
                            // Se não conseguir converter, retorna configuração vazia
                            listener.onSuccess(criarConfiguracaoPadrao());
                        }
                    } else {
                        // Se documento não existe, retorna configuração padrão
                        Log.d(TAG, "Documento de configuração não existe, criando padrão");
                        ConfiguracaoDocumentos configPadrao = criarConfiguracaoPadrao();
                        
                        // Salva a configuração padrão no Firestore
                        salvarConfiguracao(configPadrao, aVoid -> {
                            listener.onSuccess(configPadrao);
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao obter configuração", e);
                    listener.onSuccess(criarConfiguracaoPadrao());
                });
    }
    
    /**
     * Atualiza as configurações dos documentos no Firestore.
     */
    @Override
    public void salvarConfiguracao(ConfiguracaoDocumentos config, OnSuccessListener<Void> listener) {
        if (config == null) {
            Log.e(TAG, "Configuração inválida");
            return;
        }
        
        // Validar URL
        if (config.getUrlPastaGoogleDrive() == null || config.getUrlPastaGoogleDrive().trim().isEmpty()) {
            Log.e(TAG, "URL da pasta é obrigatória");
            return;
        }
        
        if (!config.getUrlPastaGoogleDrive().contains("drive.google.com")) {
            Log.e(TAG, "URL inválida. Use um link do Google Drive");
            return;
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("urlPastaGoogleDrive", config.getUrlPastaGoogleDrive().trim());
        data.put("ultimaAtualizacao", config.getUltimaAtualizacao());
        data.put("atualizadoPor", config.getAtualizadoPor());
        data.put("instrucoes", config.getInstrucoes());
        
        firestore.collection(COLLECTION_CONFIG)
                .document(DOC_DOCUMENTOS)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Configuração atualizada com sucesso");
                    listener.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao atualizar configuração", e);
                });
    }
    
    /**
     * Cria uma configuração padrão quando não existe no Firestore.
     */
    private ConfiguracaoDocumentos criarConfiguracaoPadrao() {
        return new ConfiguracaoDocumentos(
                "", // URL vazia - coordenador deve configurar
                "Aguardando configuração pelo coordenador",
                "Não configurado",
                "Sistema"
        );
    }
}
