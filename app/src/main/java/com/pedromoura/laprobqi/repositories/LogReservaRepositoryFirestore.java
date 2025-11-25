package com.pedromoura.laprobqi.repositories;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pedromoura.laprobqi.models.LogReserva;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogReservaRepositoryFirestore implements LogReservaRepository {
    
    private final FirebaseFirestore db;
    private final CollectionReference logsCollection;

    public LogReservaRepositoryFirestore() {
        this.db = FirebaseFirestore.getInstance();
        this.logsCollection = db.collection("logs_reservas");
    }

    @Override
    public void salvarLog(LogReserva log, OnLogSavedListener listener) {
        Map<String, Object> data = new HashMap<>();
        data.put("equipamentoId", log.getEquipamentoId());
        data.put("equipamentoNome", log.getEquipamentoNome());
        data.put("usuarioId", log.getUsuarioId());
        data.put("usuarioNome", log.getUsuarioNome());
        data.put("usuarioEmail", log.getUsuarioEmail());
        data.put("dataHoraInicio", log.getDataHoraInicio());
        data.put("dataHoraFim", log.getDataHoraFim());
        data.put("dataHoraReserva", log.getDataHoraReserva());
        data.put("status", log.getStatus());
        data.put("observacao", log.getObservacao());
        data.put("dataHoraCancelamento", log.getDataHoraCancelamento());
        data.put("motivoCancelamento", log.getMotivoCancelamento());

        if (log.getId() != null && !log.getId().isEmpty()) {
            // Atualizar log existente
            logsCollection.document(log.getId())
                    .set(data)
                    .addOnSuccessListener(aVoid -> listener.onSuccess(log.getId()))
                    .addOnFailureListener(e -> listener.onError("Erro ao salvar log: " + e.getMessage()));
        } else {
            // Criar novo log
            logsCollection.add(data)
                    .addOnSuccessListener(documentReference -> {
                        String logId = documentReference.getId();
                        log.setId(logId);
                        listener.onSuccess(logId);
                    })
                    .addOnFailureListener(e -> listener.onError("Erro ao criar log: " + e.getMessage()));
        }
    }

    @Override
    public void buscarTodosLogs(OnLogsLoadedListener listener) {
        logsCollection.orderBy("dataHoraReserva", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<LogReserva> logs = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        LogReserva log = documentToLogReserva(doc);
                        if (log != null) {
                            logs.add(log);
                        }
                    }
                    listener.onSuccess(logs);
                })
                .addOnFailureListener(e -> listener.onError("Erro ao buscar logs: " + e.getMessage()));
    }

    @Override
    public void buscarLogsPorEquipamento(String equipamentoId, OnLogsLoadedListener listener) {
        logsCollection.whereEqualTo("equipamentoId", equipamentoId)
                .orderBy("dataHoraReserva", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<LogReserva> logs = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        LogReserva log = documentToLogReserva(doc);
                        if (log != null) {
                            logs.add(log);
                        }
                    }
                    listener.onSuccess(logs);
                })
                .addOnFailureListener(e -> listener.onError("Erro ao buscar logs: " + e.getMessage()));
    }

    @Override
    public void buscarLogsPorUsuario(String usuarioId, OnLogsLoadedListener listener) {
        logsCollection.whereEqualTo("usuarioId", usuarioId)
                .orderBy("dataHoraReserva", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<LogReserva> logs = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        LogReserva log = documentToLogReserva(doc);
                        if (log != null) {
                            logs.add(log);
                        }
                    }
                    listener.onSuccess(logs);
                })
                .addOnFailureListener(e -> listener.onError("Erro ao buscar logs: " + e.getMessage()));
    }

    @Override
    public void buscarLogsPorPeriodo(Date dataInicio, Date dataFim, OnLogsLoadedListener listener) {
        logsCollection.whereGreaterThanOrEqualTo("dataHoraInicio", dataInicio)
                .whereLessThanOrEqualTo("dataHoraInicio", dataFim)
                .orderBy("dataHoraInicio", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<LogReserva> logs = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        LogReserva log = documentToLogReserva(doc);
                        if (log != null) {
                            logs.add(log);
                        }
                    }
                    listener.onSuccess(logs);
                })
                .addOnFailureListener(e -> listener.onError("Erro ao buscar logs: " + e.getMessage()));
    }

    @Override
    public void buscarLogsPorStatus(String status, OnLogsLoadedListener listener) {
        logsCollection.whereEqualTo("status", status)
                .orderBy("dataHoraReserva", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<LogReserva> logs = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        LogReserva log = documentToLogReserva(doc);
                        if (log != null) {
                            logs.add(log);
                        }
                    }
                    listener.onSuccess(logs);
                })
                .addOnFailureListener(e -> listener.onError("Erro ao buscar logs: " + e.getMessage()));
    }

    @Override
    public void atualizarStatus(String logId, String novoStatus, OnLogSavedListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", novoStatus);

        logsCollection.document(logId)
                .update(updates)
                .addOnSuccessListener(aVoid -> listener.onSuccess(logId))
                .addOnFailureListener(e -> listener.onError("Erro ao atualizar status: " + e.getMessage()));
    }

    @Override
    public void cancelarReserva(String logId, String motivo, OnLogSavedListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "CANCELADA");
        updates.put("motivoCancelamento", motivo);
        updates.put("dataHoraCancelamento", new Date());

        logsCollection.document(logId)
                .update(updates)
                .addOnSuccessListener(aVoid -> listener.onSuccess(logId))
                .addOnFailureListener(e -> listener.onError("Erro ao cancelar reserva: " + e.getMessage()));
    }

    private LogReserva documentToLogReserva(DocumentSnapshot doc) {
        try {
            LogReserva log = new LogReserva();
            log.setId(doc.getId());
            log.setEquipamentoId(doc.getString("equipamentoId"));
            log.setEquipamentoNome(doc.getString("equipamentoNome"));
            log.setUsuarioId(doc.getString("usuarioId"));
            log.setUsuarioNome(doc.getString("usuarioNome"));
            log.setUsuarioEmail(doc.getString("usuarioEmail"));
            log.setDataHoraInicio(doc.getDate("dataHoraInicio"));
            log.setDataHoraFim(doc.getDate("dataHoraFim"));
            log.setDataHoraReserva(doc.getDate("dataHoraReserva"));
            log.setStatus(doc.getString("status"));
            log.setObservacao(doc.getString("observacao"));
            log.setDataHoraCancelamento(doc.getDate("dataHoraCancelamento"));
            log.setMotivoCancelamento(doc.getString("motivoCancelamento"));
            return log;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
