package com.pedromoura.laprobqi.repository.impl;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pedromoura.laprobqi.Produto;
import com.pedromoura.laprobqi.repository.ProdutoRepository;
import com.pedromoura.laprobqi.utils.DateConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação do repositório de produtos usando Cloud Firestore.
 * 
 * Características:
 * - Coleção: "produtos"
 * - Conversão de datas: String (model) ↔ Timestamp (Firestore)
 * - Campo quantidade (double) mapeia automaticamente para Number
 */
public class ProdutoRepositoryFirestore implements ProdutoRepository {
    
    private static final String TAG = "ProdutoRepositoryFirestore";
    private static final String COLLECTION_PRODUTOS = "produtos";
    
    private final FirebaseFirestore firestore;
    
    public ProdutoRepositoryFirestore() {
        this.firestore = FirebaseFirestore.getInstance();
    }
    
    /**
     * Adiciona um novo produto ao Firestore.
     * 
     * @param produto Produto a ser adicionado
     * @param listener Callback de resultado
     */
    @Override
    public void adicionarProduto(Produto produto, OnCompleteListener listener) {
        
        // Validações
        if (produto == null) {
            listener.onComplete(false, "Produto não pode ser nulo");
            return;
        }
        if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
            listener.onComplete(false, "Nome do produto não pode estar vazio");
            return;
        }
        if (produto.getTipo() == null || produto.getTipo().trim().isEmpty()) {
            listener.onComplete(false, "Tipo do produto não pode estar vazio");
            return;
        }
        if (produto.getQuantidade() < 0) {
            listener.onComplete(false, "Quantidade não pode ser negativa");
            return;
        }
        
        // Converter modelo para Map do Firestore
        Map<String, Object> produtoData = produtoToMap(produto);
        
        firestore.collection(COLLECTION_PRODUTOS)
            .add(produtoData)
            .addOnSuccessListener(documentReference -> {
                String id = documentReference.getId();
                Log.d(TAG, "Produto criado com sucesso: " + id);
                listener.onComplete(true, "Produto adicionado com sucesso");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao adicionar produto", e);
                listener.onComplete(false, "Erro ao adicionar produto: " + e.getMessage());
            });
    }
    
    /**
     * Lista todos os produtos do Firestore.
     * 
     * @param listener Callback de resultado
     */
    @Override
    public void listarProdutos(OnSuccessListener<List<Produto>> listener) {
        firestore.collection(COLLECTION_PRODUTOS)
            .orderBy("nome")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Produto> produtos = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Produto produto = documentSnapshotToProduto(document);
                    produtos.add(produto);
                }
                Log.d(TAG, "Total de produtos obtidos: " + produtos.size());
                listener.onSuccess(produtos);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao listar produtos", e);
                listener.onSuccess(new ArrayList<>()); // Retorna lista vazia em caso de erro
            });
    }
    
    /**
     * Atualiza um produto existente.
     * 
     * @param produto Produto com dados atualizados
     * @param listener Callback de resultado
     */
    @Override
    public void atualizarProduto(Produto produto, OnCompleteListener listener) {
        if (produto == null || produto.getId() <= 0) {
            listener.onComplete(false, "Produto inválido para atualização");
            return;
        }
        
        // Buscar o documento no Firestore usando query pelo campo id
        firestore.collection(COLLECTION_PRODUTOS)
            .whereEqualTo("id", produto.getId())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    listener.onComplete(false, "Produto não encontrado");
                    return;
                }
                
                // Pegar o primeiro documento encontrado
                String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                Map<String, Object> produtoData = produtoToMap(produto);
                
                firestore.collection(COLLECTION_PRODUTOS)
                    .document(documentId)
                    .set(produtoData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Produto atualizado com sucesso: " + produto.getId());
                        listener.onComplete(true, "Produto atualizado com sucesso");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erro ao atualizar produto", e);
                        listener.onComplete(false, "Erro ao atualizar produto: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao buscar produto para atualização", e);
                listener.onComplete(false, "Erro ao buscar produto: " + e.getMessage());
            });
    }
    
    /**
     * Remove um produto por ID.
     * 
     * @param id ID do produto (campo id do modelo, não document ID)
     * @param listener Callback de resultado
     */
    @Override
    public void removerProduto(int id, OnCompleteListener listener) {
        if (id <= 0) {
            listener.onComplete(false, "ID inválido");
            return;
        }
        
        // Buscar o documento no Firestore usando query pelo campo id
        firestore.collection(COLLECTION_PRODUTOS)
            .whereEqualTo("id", id)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    listener.onComplete(false, "Produto não encontrado");
                    return;
                }
                
                // Deletar o primeiro documento encontrado
                String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                
                firestore.collection(COLLECTION_PRODUTOS)
                    .document(documentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Produto removido com sucesso: " + id);
                        listener.onComplete(true, "Produto removido com sucesso");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erro ao remover produto", e);
                        listener.onComplete(false, "Erro ao remover produto: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao buscar produto para remoção", e);
                listener.onComplete(false, "Erro ao buscar produto: " + e.getMessage());
            });
    }
    
    /**
     * Converte um objeto Produto para Map do Firestore.
     * Realiza a conversão de datas String -> Timestamp.
     * 
     * IMPORTANTE: O campo 'id' NÃO é incluído no Map, pois o Firebase
     * usa o ID do documento (string gerado automaticamente) como identificador.
     * 
     * @param produto Objeto Produto
     * @return Map com dados para Firestore
     */
    private Map<String, Object> produtoToMap(Produto produto) {
        Map<String, Object> data = new HashMap<>();
        
        // Nota: Não incluímos o campo 'id', pois o Firestore gera seu próprio ID de documento
        data.put("nome", produto.getNome());
        data.put("tipo", produto.getTipo());
        data.put("quantidade", produto.getQuantidade()); // double -> Number (automático)
        data.put("unidade", produto.getUnidade());
        data.put("observacoes", produto.getObservacoes());
        
        // Converter validade: String -> Timestamp
        if (produto.getValidade() != null && !produto.getValidade().isEmpty()) {
            Timestamp timestamp = DateConverter.dateToTimestamp(produto.getValidade());
            if (timestamp != null) {
                data.put("validade", timestamp);
            } else {
                // Se conversão falhar, mantém como String
                data.put("validade", produto.getValidade());
            }
        } else {
            data.put("validade", null);
        }
        
        return data;
    }
    
    /**
     * Converte um DocumentSnapshot do Firestore para objeto Produto.
     * Realiza a conversão de datas Timestamp -> String.
     * 
     * @param document DocumentSnapshot do Firestore
     * @return Objeto Produto
     */
    private Produto documentSnapshotToProduto(DocumentSnapshot document) {
        // IMPORTANTE: O campo 'id' não é armazenado no Firestore.
        // O Firebase usa o ID do documento (string) como identificador único.
        // Para compatibilidade com SQLite, geramos um hashCode do ID do documento.
        String documentId = document.getId();
        int id = documentId.hashCode(); // Gera um int único baseado no ID do documento
        
        String nome = document.getString("nome");
        String tipo = document.getString("tipo");
        String unidade = document.getString("unidade");
        String observacoes = document.getString("observacoes");
        
        // Quantidade: Number -> double (automático)
        Double quantidade = document.getDouble("quantidade");
        if (quantidade == null) {
            quantidade = 0.0;
        }
        
        // Converter validade: Timestamp -> String
        String validade = null;
        Object validadeObj = document.get("validade");
        if (validadeObj instanceof Timestamp) {
            Timestamp validadeTimestamp = (Timestamp) validadeObj;
            validade = DateConverter.timestampToDate(validadeTimestamp);
        } else if (validadeObj instanceof String) {
            validade = (String) validadeObj;
        }
        
        // Criar e retornar o objeto Produto
        return new Produto(id, nome, tipo, validade, quantidade, unidade, observacoes);
    }
}
