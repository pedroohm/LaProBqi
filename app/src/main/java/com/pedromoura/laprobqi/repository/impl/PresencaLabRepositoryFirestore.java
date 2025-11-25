package com.pedromoura.laprobqi.repository.impl;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pedromoura.laprobqi.models.PresencaLab;
import com.pedromoura.laprobqi.repository.PresencaLabRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação Firebase do repositório de presença no laboratório.
 */
public class PresencaLabRepositoryFirestore implements PresencaLabRepository {
    
    private static final String TAG = "PresencaLabRepository";
    private static final String COLLECTION = "presencas_lab";
    
    private final FirebaseFirestore firestore;
    
    public PresencaLabRepositoryFirestore() {
        this.firestore = FirebaseFirestore.getInstance();
    }
    
    @Override
    public void registrarEntrada(PresencaLab presenca, OnCompleteListener listener) {
        Map<String, Object> data = presencaToMap(presenca);
        
        firestore.collection(COLLECTION)
            .add(data)
            .addOnSuccessListener(documentReference -> {
                String id = documentReference.getId();
                Log.d(TAG, "Entrada registrada com sucesso: " + id);
                listener.onComplete(true, "Entrada registrada com sucesso!");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao registrar entrada", e);
                listener.onComplete(false, "Erro ao registrar entrada: " + e.getMessage());
            });
    }
    
    @Override
    public void registrarSaida(String presencaId, String dataSaida, String horaSaida, OnCompleteListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("dataSaida", dataSaida);
        updates.put("horaSaida", horaSaida);
        updates.put("status", "SAIU");
        
        firestore.collection(COLLECTION)
            .document(presencaId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Saída registrada com sucesso: " + presencaId);
                listener.onComplete(true, "Saída registrada com sucesso!");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao registrar saída", e);
                listener.onComplete(false, "Erro ao registrar saída: " + e.getMessage());
            });
    }
    
    @Override
    public void buscarPresencaAtiva(String usuarioId, OnPresencaListener listener) {
        firestore.collection(COLLECTION)
            .whereEqualTo("usuarioId", usuarioId)
            .whereEqualTo("status", "PRESENTE")
            .orderBy("dataEntrada", Query.Direction.DESCENDING)
            .orderBy("horaEntrada", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                    PresencaLab presenca = documentToPresenca(document);
                    listener.onSuccess(presenca);
                } else {
                    listener.onNotFound();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao buscar presença ativa", e);
                listener.onFailure("Erro ao buscar presença: " + e.getMessage());
            });
    }
    
    @Override
    public void listarPresencasPorPeriodo(String dataInicio, String dataFim, OnListPresencasListener listener) {
        firestore.collection(COLLECTION)
            .whereGreaterThanOrEqualTo("dataEntrada", dataInicio)
            .whereLessThanOrEqualTo("dataEntrada", dataFim)
            .orderBy("dataEntrada", Query.Direction.DESCENDING)
            .orderBy("horaEntrada", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<PresencaLab> presencas = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    presencas.add(documentToPresenca(document));
                }
                Log.d(TAG, "Presenças obtidas: " + presencas.size());
                listener.onSuccess(presencas);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao listar presenças por período", e);
                listener.onFailure("Erro ao listar presenças: " + e.getMessage());
            });
    }
    
    @Override
    public void listarTodasPresencas(OnListPresencasListener listener) {
        firestore.collection(COLLECTION)
            .orderBy("dataEntrada", Query.Direction.DESCENDING)
            .orderBy("horaEntrada", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<PresencaLab> presencas = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    presencas.add(documentToPresenca(document));
                }
                Log.d(TAG, "Total de presenças: " + presencas.size());
                listener.onSuccess(presencas);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao listar todas as presenças", e);
                listener.onFailure("Erro ao listar presenças: " + e.getMessage());
            });
    }
    
    // Métodos auxiliares
    private Map<String, Object> presencaToMap(PresencaLab presenca) {
        Map<String, Object> data = new HashMap<>();
        data.put("usuarioId", presenca.getUsuarioId());
        data.put("usuarioNome", presenca.getUsuarioNome());
        data.put("usuarioEmail", presenca.getUsuarioEmail());
        data.put("dataEntrada", presenca.getDataEntrada());
        data.put("horaEntrada", presenca.getHoraEntrada());
        data.put("dataSaida", presenca.getDataSaida());
        data.put("horaSaida", presenca.getHoraSaida());
        data.put("status", presenca.getStatus());
        return data;
    }
    
    private PresencaLab documentToPresenca(DocumentSnapshot document) {
        PresencaLab presenca = new PresencaLab();
        presenca.setId(document.getId());
        presenca.setUsuarioId(document.getString("usuarioId"));
        presenca.setUsuarioNome(document.getString("usuarioNome"));
        presenca.setUsuarioEmail(document.getString("usuarioEmail"));
        presenca.setDataEntrada(document.getString("dataEntrada"));
        presenca.setHoraEntrada(document.getString("horaEntrada"));
        presenca.setDataSaida(document.getString("dataSaida"));
        presenca.setHoraSaida(document.getString("horaSaida"));
        presenca.setStatus(document.getString("status"));
        return presenca;
    }
}
