package com.pedromoura.laprobqi.repository.impl;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pedromoura.laprobqi.models.SolicitacaoCoordenador;
import com.pedromoura.laprobqi.repository.SolicitacaoCoordenadorRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SolicitacaoCoordenadorRepositoryFirestore implements SolicitacaoCoordenadorRepository {
    
    private static final String TAG = "SolicitacaoCoordenadorRepo";
    private static final String COLLECTION = "solicitacoes_coordenador";
    
    private final FirebaseFirestore firestore;
    
    public SolicitacaoCoordenadorRepositoryFirestore() {
        this.firestore = FirebaseFirestore.getInstance();
    }
    
    @Override
    public void criarSolicitacao(SolicitacaoCoordenador solicitacao, OnSolicitacaoListener listener) {
        Map<String, Object> data = new HashMap<>();
        data.put("nome", solicitacao.getNome());
        data.put("email", solicitacao.getEmail());
        data.put("status", solicitacao.getStatus());
        data.put("dataSolicitacao", solicitacao.getDataSolicitacao());
        
        firestore.collection(COLLECTION)
            .add(data)
            .addOnSuccessListener(documentReference -> {
                solicitacao.setId(documentReference.getId());
                Log.d(TAG, "Solicitação criada com sucesso: " + documentReference.getId());
                listener.onSuccess(solicitacao);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao criar solicitação", e);
                listener.onFailure("Erro ao criar solicitação: " + e.getMessage());
            });
    }
    
    @Override
    public void listarSolicitacoesPendentes(OnListSolicitacoesListener listener) {
        firestore.collection(COLLECTION)
            .whereEqualTo("status", "PENDENTE")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<SolicitacaoCoordenador> solicitacoes = new ArrayList<>();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    SolicitacaoCoordenador solicitacao = doc.toObject(SolicitacaoCoordenador.class);
                    solicitacao.setId(doc.getId());
                    solicitacoes.add(solicitacao);
                }
                listener.onSuccess(solicitacoes);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao listar solicitações pendentes", e);
                listener.onFailure("Erro ao listar solicitações: " + e.getMessage());
            });
    }
    
    @Override
    public void aprovarSolicitacao(String solicitacaoId, String coordenadorId, 
                                   String coordenadorNome, OnCompleteListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "APROVADA");
        updates.put("coordenadorAprovadorId", coordenadorId);
        updates.put("coordenadorAprovadorNome", coordenadorNome);
        updates.put("dataAprovacao", String.valueOf(System.currentTimeMillis()));
        
        firestore.collection(COLLECTION)
            .document(solicitacaoId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Solicitação aprovada com sucesso");
                listener.onComplete(true, "Solicitação aprovada com sucesso");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao aprovar solicitação", e);
                listener.onComplete(false, "Erro ao aprovar solicitação: " + e.getMessage());
            });
    }
    
    @Override
    public void rejeitarSolicitacao(String solicitacaoId, String motivo, OnCompleteListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "REJEITADA");
        updates.put("motivoRejeicao", motivo);
        updates.put("dataAprovacao", String.valueOf(System.currentTimeMillis()));
        
        firestore.collection(COLLECTION)
            .document(solicitacaoId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Solicitação rejeitada com sucesso");
                listener.onComplete(true, "Solicitação rejeitada");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao rejeitar solicitação", e);
                listener.onComplete(false, "Erro ao rejeitar solicitação: " + e.getMessage());
            });
    }
    
    @Override
    public void verificarEmailJaSolicitado(String email, OnBooleanListener listener) {
        firestore.collection(COLLECTION)
            .whereEqualTo("email", email)
            .whereEqualTo("status", "PENDENTE")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                boolean existe = !querySnapshot.isEmpty();
                listener.onSuccess(existe);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao verificar email", e);
                listener.onFailure("Erro ao verificar email: " + e.getMessage());
            });
    }
    
    @Override
    public void verificarEmailAprovado(String email, OnBooleanListener listener) {
        firestore.collection(COLLECTION)
            .whereEqualTo("email", email)
            .whereEqualTo("status", "APROVADA")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                boolean existe = !querySnapshot.isEmpty();
                listener.onSuccess(existe);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao verificar aprovação", e);
                listener.onFailure("Erro ao verificar aprovação: " + e.getMessage());
            });
    }
}
