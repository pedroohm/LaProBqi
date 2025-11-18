package com.pedromoura.laprobqi.repository.impl;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pedromoura.laprobqi.models.RegistroUso;
import com.pedromoura.laprobqi.repository.RegistroUsoRepository;
import com.pedromoura.laprobqi.utils.DateConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação do repositório de registros de uso usando Cloud Firestore.
 * 
 * Características:
 * - Coleção: "registros_uso"
 * - Denormalização: equipamentoNome, usuarioNome
 * - Conversão de datas: String (model) ↔ Timestamp (Firestore)
 * - Campos: usuarioId, equipamentoId, reservaId (pode ser null)
 */
public class RegistroUsoRepositoryFirestore implements RegistroUsoRepository {
    
    private static final String TAG = "RegistroUsoRepositoryFS";
    private static final String COLLECTION_REGISTROS_USO = "registros_uso";
    
    private final FirebaseFirestore firestore;
    
    public RegistroUsoRepositoryFirestore() {
        this.firestore = FirebaseFirestore.getInstance();
    }
    
    @Override
    public void salvarRegistroUso(RegistroUso registroUso, OnRegistroUsoListener listener) {
        if (registroUso == null) {
            listener.onFailure("Registro de uso não pode ser nulo");
            return;
        }
        
        Map<String, Object> registroData = registroUsoToMap(registroUso);
        
        if (registroUso.getId() != null && !registroUso.getId().isEmpty()) {
            firestore.collection(COLLECTION_REGISTROS_USO)
                .document(registroUso.getId())
                .set(registroData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Registro de uso salvo: " + registroUso.getId());
                    listener.onSuccess(registroUso);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao salvar registro de uso", e);
                    listener.onFailure("Erro: " + e.getMessage());
                });
        } else {
            firestore.collection(COLLECTION_REGISTROS_USO)
                .add(registroData)
                .addOnSuccessListener(documentReference -> {
                    registroUso.setId(documentReference.getId());
                    Log.d(TAG, "Registro de uso criado: " + registroUso.getId());
                    listener.onSuccess(registroUso);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao criar registro de uso", e);
                    listener.onFailure("Erro: " + e.getMessage());
                });
        }
    }
    
    @Override
    public void obterRegistroUso(String id, OnRegistroUsoListener listener) {
        firestore.collection(COLLECTION_REGISTROS_USO)
            .document(id)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    RegistroUso registroUso = documentSnapshotToRegistroUso(documentSnapshot);
                    listener.onSuccess(registroUso);
                } else {
                    listener.onFailure("Registro de uso não encontrado");
                }
            })
            .addOnFailureListener(e -> listener.onFailure("Erro: " + e.getMessage()));
    }
    
    @Override
    public void obterTodosRegistrosUso(OnRegistrosUsoListener listener) {
        firestore.collection(COLLECTION_REGISTROS_USO)
            .orderBy("dataInicio")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<RegistroUso> registrosUso = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    registrosUso.add(documentSnapshotToRegistroUso(document));
                }
                listener.onSuccess(registrosUso);
            })
            .addOnFailureListener(e -> listener.onFailure("Erro: " + e.getMessage()));
    }
    
    @Override
    public void atualizarRegistroUso(RegistroUso registroUso, OnBooleanListener listener) {
        if (registroUso == null || registroUso.getId() == null) {
            listener.onFailure("Registro de uso inválido");
            return;
        }
        
        Map<String, Object> registroData = registroUsoToMap(registroUso);
        
        firestore.collection(COLLECTION_REGISTROS_USO)
            .document(registroUso.getId())
            .set(registroData)
            .addOnSuccessListener(aVoid -> listener.onSuccess(true))
            .addOnFailureListener(e -> listener.onFailure("Erro: " + e.getMessage()));
    }
    
    @Override
    public void excluirRegistroUso(String id, OnBooleanListener listener) {
        firestore.collection(COLLECTION_REGISTROS_USO)
            .document(id)
            .delete()
            .addOnSuccessListener(aVoid -> listener.onSuccess(true))
            .addOnFailureListener(e -> listener.onFailure("Erro: " + e.getMessage()));
    }
    
    @Override
    public void obterRegistrosUsoPorUsuario(String usuarioId, OnRegistrosUsoListener listener) {
        firestore.collection(COLLECTION_REGISTROS_USO)
            .whereEqualTo("usuarioId", usuarioId)
            .orderBy("dataInicio")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<RegistroUso> registrosUso = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    registrosUso.add(documentSnapshotToRegistroUso(document));
                }
                listener.onSuccess(registrosUso);
            })
            .addOnFailureListener(e -> listener.onFailure("Erro: " + e.getMessage()));
    }
    
    @Override
    public void obterRegistrosUsoPorEquipamento(String equipamentoId, OnRegistrosUsoListener listener) {
        firestore.collection(COLLECTION_REGISTROS_USO)
            .whereEqualTo("equipamentoId", equipamentoId)
            .orderBy("dataInicio")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<RegistroUso> registrosUso = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    registrosUso.add(documentSnapshotToRegistroUso(document));
                }
                listener.onSuccess(registrosUso);
            })
            .addOnFailureListener(e -> listener.onFailure("Erro: " + e.getMessage()));
    }
    
    @Override
    public void obterRegistrosUsoEmAndamento(OnRegistrosUsoListener listener) {
        firestore.collection(COLLECTION_REGISTROS_USO)
            .whereEqualTo("status", "EM_ANDAMENTO")
            .orderBy("dataInicio")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<RegistroUso> registrosUso = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    registrosUso.add(documentSnapshotToRegistroUso(document));
                }
                listener.onSuccess(registrosUso);
            })
            .addOnFailureListener(e -> listener.onFailure("Erro: " + e.getMessage()));
    }
    
    @Override
    public void finalizarRegistroUso(String registroId, String observacoes, OnBooleanListener listener) {
        // Obter data e hora atuais
        String dataFim = DateConverter.timestampToDate(Timestamp.now());
        String horaFim = DateConverter.timestampToTime(Timestamp.now());
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "FINALIZADO");
        updates.put("dataFim", DateConverter.dateToTimestamp(dataFim));
        updates.put("horaFim", DateConverter.timeToTimestamp(horaFim));
        updates.put("observacoes", observacoes);
        
        firestore.collection(COLLECTION_REGISTROS_USO)
            .document(registroId)
            .update(updates)
            .addOnSuccessListener(aVoid -> listener.onSuccess(true))
            .addOnFailureListener(e -> listener.onFailure("Erro: " + e.getMessage()));
    }
    
    @Override
    public void obterRegistrosUsoPorPeriodo(String dataInicio, String dataFim, OnRegistrosUsoListener listener) {
        Timestamp dataInicioTimestamp = DateConverter.dateToTimestamp(dataInicio);
        Timestamp dataFimTimestamp = DateConverter.dateToTimestamp(dataFim);
        
        if (dataInicioTimestamp == null || dataFimTimestamp == null) {
            listener.onFailure("Datas inválidas");
            return;
        }
        
        firestore.collection(COLLECTION_REGISTROS_USO)
            .whereGreaterThanOrEqualTo("dataInicio", dataInicioTimestamp)
            .whereLessThanOrEqualTo("dataInicio", dataFimTimestamp)
            .orderBy("dataInicio")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<RegistroUso> registrosUso = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    registrosUso.add(documentSnapshotToRegistroUso(document));
                }
                listener.onSuccess(registrosUso);
            })
            .addOnFailureListener(e -> listener.onFailure("Erro: " + e.getMessage()));
    }
    
    @Override
    public void obterRegistrosUsoPorUsuarioEPeriodo(String usuarioId, String dataInicio, 
                                                     String dataFim, OnRegistrosUsoListener listener) {
        Timestamp dataInicioTimestamp = DateConverter.dateToTimestamp(dataInicio);
        Timestamp dataFimTimestamp = DateConverter.dateToTimestamp(dataFim);
        
        if (dataInicioTimestamp == null || dataFimTimestamp == null) {
            listener.onFailure("Datas inválidas");
            return;
        }
        
        firestore.collection(COLLECTION_REGISTROS_USO)
            .whereEqualTo("usuarioId", usuarioId)
            .whereGreaterThanOrEqualTo("dataInicio", dataInicioTimestamp)
            .whereLessThanOrEqualTo("dataInicio", dataFimTimestamp)
            .orderBy("dataInicio")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<RegistroUso> registrosUso = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    registrosUso.add(documentSnapshotToRegistroUso(document));
                }
                listener.onSuccess(registrosUso);
            })
            .addOnFailureListener(e -> listener.onFailure("Erro: " + e.getMessage()));
    }
    
    private Map<String, Object> registroUsoToMap(RegistroUso registroUso) {
        Map<String, Object> data = new HashMap<>();
        
        if (registroUso.getId() != null) data.put("id", registroUso.getId());
        data.put("equipamentoId", registroUso.getEquipamentoId());
        data.put("equipamentoNome", registroUso.getEquipamentoNome());
        data.put("usuarioId", registroUso.getUsuarioId());
        data.put("usuarioNome", registroUso.getUsuarioNome());
        data.put("reservaId", registroUso.getReservaId());
        data.put("status", registroUso.getStatus());
        data.put("observacoes", registroUso.getObservacoes());
        
        // Converter datas String -> Timestamp
        Timestamp dataInicio = DateConverter.dateToTimestamp(registroUso.getDataInicio());
        if (dataInicio != null) data.put("dataInicio", dataInicio);
        
        Timestamp horaInicio = DateConverter.timeToTimestamp(registroUso.getHoraInicio());
        if (horaInicio != null) data.put("horaInicio", horaInicio);
        
        if (registroUso.getDataFim() != null && !registroUso.getDataFim().isEmpty()) {
            Timestamp dataFim = DateConverter.dateToTimestamp(registroUso.getDataFim());
            if (dataFim != null) data.put("dataFim", dataFim);
        }
        
        if (registroUso.getHoraFim() != null && !registroUso.getHoraFim().isEmpty()) {
            Timestamp horaFim = DateConverter.timeToTimestamp(registroUso.getHoraFim());
            if (horaFim != null) data.put("horaFim", horaFim);
        }
        
        return data;
    }
    
    private RegistroUso documentSnapshotToRegistroUso(DocumentSnapshot document) {
        RegistroUso registroUso = new RegistroUso();
        
        registroUso.setId(document.getId());
        registroUso.setEquipamentoId(document.getString("equipamentoId"));
        registroUso.setEquipamentoNome(document.getString("equipamentoNome"));
        registroUso.setUsuarioId(document.getString("usuarioId"));
        registroUso.setUsuarioNome(document.getString("usuarioNome"));
        registroUso.setReservaId(document.getString("reservaId"));
        registroUso.setStatus(document.getString("status"));
        registroUso.setObservacoes(document.getString("observacoes"));
        
        // Converter Timestamp -> String
        Timestamp dataInicio = document.getTimestamp("dataInicio");
        if (dataInicio != null) {
            registroUso.setDataInicio(DateConverter.timestampToDate(dataInicio));
        }
        
        Timestamp horaInicio = document.getTimestamp("horaInicio");
        if (horaInicio != null) {
            registroUso.setHoraInicio(DateConverter.timestampToTime(horaInicio));
        }
        
        Timestamp dataFim = document.getTimestamp("dataFim");
        if (dataFim != null) {
            registroUso.setDataFim(DateConverter.timestampToDate(dataFim));
        }
        
        Timestamp horaFim = document.getTimestamp("horaFim");
        if (horaFim != null) {
            registroUso.setHoraFim(DateConverter.timestampToTime(horaFim));
        }
        
        return registroUso;
    }
}
