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
     * IMPORTANTE: O ID do produto no app é gerado via hashCode() do documentId do Firestore.
     * Para atualizar, precisamos buscar todos os produtos e encontrar o que tem o mesmo ID.
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
        
        // Buscar TODOS os produtos para encontrar o document ID correto
        // (porque o ID do produto é o hashCode do documentId)
        firestore.collection(COLLECTION_PRODUTOS)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                String documentIdParaAtualizar = null;
                
                // Procurar o documento cujo ID.hashCode() == produto.getId()
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    int docIdHash = doc.getId().hashCode();
                    if (docIdHash == produto.getId()) {
                        documentIdParaAtualizar = doc.getId();
                        break;
                    }
                }
                
                if (documentIdParaAtualizar == null) {
                    Log.e(TAG, "Documento não encontrado para produto ID: " + produto.getId());
                    listener.onComplete(false, "Produto não encontrado no Firebase");
                    return;
                }
                
                // Atualizar o documento encontrado (usar variável final para lambda)
                final String docId = documentIdParaAtualizar;
                Map<String, Object> produtoData = produtoToMap(produto);
                
                firestore.collection(COLLECTION_PRODUTOS)
                    .document(docId)
                    .set(produtoData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Produto atualizado com sucesso: ID=" + produto.getId() + 
                              ", DocID=" + docId);
                        listener.onComplete(true, "Produto atualizado com sucesso");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erro ao atualizar produto", e);
                        listener.onComplete(false, "Erro ao atualizar produto: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao buscar produtos", e);
                listener.onComplete(false, "Erro ao buscar produto: " + e.getMessage());
            });
    }
    
    /**
     * Remove um produto por ID.
     * 
     * IMPORTANTE: O ID do produto no app é gerado via hashCode() do documentId do Firestore.
     * Para remover, precisamos buscar todos os produtos e encontrar o que tem o mesmo ID.
     * 
     * @param id ID do produto (gerado via hashCode do document ID)
     * @param listener Callback de resultado
     */
    @Override
    public void removerProduto(int id, OnCompleteListener listener) {
        if (id <= 0) {
            listener.onComplete(false, "ID inválido");
            return;
        }
        
        // Buscar TODOS os produtos para encontrar o document ID correto
        firestore.collection(COLLECTION_PRODUTOS)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                String documentIdParaRemover = null;
                
                // Procurar o documento cujo ID.hashCode() == id
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    int docIdHash = doc.getId().hashCode();
                    if (docIdHash == id) {
                        documentIdParaRemover = doc.getId();
                        break;
                    }
                }
                
                if (documentIdParaRemover == null) {
                    Log.e(TAG, "Documento não encontrado para produto ID: " + id);
                    listener.onComplete(false, "Produto não encontrado");
                    return;
                }
                
                // Deletar o documento encontrado (usar variável final para lambda)
                final String docId = documentIdParaRemover;
                
                firestore.collection(COLLECTION_PRODUTOS)
                    .document(docId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Produto removido com sucesso: ID=" + id + 
                              ", DocID=" + docId);
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
        
        // Campo tipo agora armazena a categoria completa (ex: "Reagentes Controlados")
        data.put("tipo", produto.getCategoria() != null ? produto.getCategoria() : "Reagentes não classificados/Indefinidos");
        
        // Novos campos de categorização
        data.put("categoria", produto.getCategoria());
        data.put("codigo", produto.getCodigo());
        data.put("cor", produto.getCor());
        data.put("hexColor", produto.getHexColor());
        
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
        
        // Ler campos de categorização
        String codigo = document.getString("codigo");
        String categoria = document.getString("categoria");
        String cor = document.getString("cor");
        String hexColor = document.getString("hexColor");
        
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
        
        // Criar produto e aplicar categorização
        Produto produto = new Produto(id, nome, tipo, validade, quantidade, unidade, observacoes);
        
        // Aplicar categoria se existir, caso contrário definir como Indefinido
        if (codigo != null && !codigo.isEmpty()) {
            produto.setCategoria(codigo);
        } else {
            produto.setCategoria("IND"); // Indefinido por padrão
        }
        
        return produto;
    }
}
