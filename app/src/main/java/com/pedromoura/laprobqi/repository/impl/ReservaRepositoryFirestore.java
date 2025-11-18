package com.pedromoura.laprobqi.repository.impl;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pedromoura.laprobqi.models.Reserva;
import com.pedromoura.laprobqi.repository.ReservaRepository;
import com.pedromoura.laprobqi.utils.DateConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação do repositório de reservas usando Cloud Firestore.
 * 
 * Características:
 * - Coleção: "reservas"
 * - Denormalização: equipamentoNome, usuarioNome
 * - Conversão de datas: String (model) ↔ Timestamp (Firestore)
 */
public class ReservaRepositoryFirestore implements ReservaRepository {
    
    private static final String TAG = "ReservaRepositoryFirestore";
    private static final String COLLECTION_RESERVAS = "reservas";
    
    private final FirebaseFirestore firestore;
    
    public ReservaRepositoryFirestore() {
        this.firestore = FirebaseFirestore.getInstance();
    }
    
    @Override
    public void salvarReserva(Reserva reserva, OnReservaListener listener) {
        if (reserva == null) {
            listener.onFailure("Reserva não pode ser nula");
            return;
        }
        
        Map<String, Object> reservaData = reservaToMap(reserva);
        
        if (reserva.getId() != null && !reserva.getId().isEmpty()) {
            firestore.collection(COLLECTION_RESERVAS)
                .document(reserva.getId())
                .set(reservaData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Reserva salva: " + reserva.getId());
                    listener.onSuccess(reserva);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao salvar reserva", e);
                    listener.onFailure("Erro: " + e.getMessage());
                });
        } else {
            firestore.collection(COLLECTION_RESERVAS)
                .add(reservaData)
                .addOnSuccessListener(documentReference -> {
                    reserva.setId(documentReference.getId());
                    Log.d(TAG, "Reserva criada: " + reserva.getId());
                    listener.onSuccess(reserva);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao criar reserva", e);
                    listener.onFailure("Erro: " + e.getMessage());
                });
        }
    }
    
    @Override
    public void obterReserva(String id, OnReservaListener listener) {
        firestore.collection(COLLECTION_RESERVAS)
            .document(id)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Reserva reserva = documentSnapshotToReserva(documentSnapshot);
                    listener.onSuccess(reserva);
                } else {
                    listener.onFailure("Reserva não encontrada");
                }
            })
            .addOnFailureListener(e -> listener.onFailure("Erro: " + e.getMessage()));
    }
    
    @Override
    public void obterTodasReservas(OnReservasListener listener) {
        firestore.collection(COLLECTION_RESERVAS)
            .orderBy("dataReserva")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Reserva> reservas = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    reservas.add(documentSnapshotToReserva(document));
                }
                listener.onSuccess(reservas);
            })
            .addOnFailureListener(e -> listener.onFailure("Erro: " + e.getMessage()));
    }
    
    @Override
    public void atualizarReserva(Reserva reserva, OnBooleanListener listener) {
        if (reserva == null || reserva.getId() == null) {
            listener.onFailure("Reserva inválida");
            return;
        }
        
        Map<String, Object> reservaData = reservaToMap(reserva);
        
        firestore.collection(COLLECTION_RESERVAS)
            .document(reserva.getId())
            .set(reservaData)
            .addOnSuccessListener(aVoid -> listener.onSuccess(true))
            .addOnFailureListener(e -> listener.onFailure("Erro: " + e.getMessage()));
    }
    
    @Override
    public void excluirReserva(String id, OnBooleanListener listener) {
        firestore.collection(COLLECTION_RESERVAS)
            .document(id)
            .delete()
            .addOnSuccessListener(aVoid -> listener.onSuccess(true))
            .addOnFailureListener(e -> listener.onFailure("Erro: " + e.getMessage()));
    }
    
    @Override
    public void obterReservasPorUsuario(String usuarioId, OnReservasListener listener) {
        firestore.collection(COLLECTION_RESERVAS)
            .whereEqualTo("usuarioId", usuarioId)
            .orderBy("dataReserva")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Reserva> reservas = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    reservas.add(documentSnapshotToReserva(document));
                }
                listener.onSuccess(reservas);
            })
            .addOnFailureListener(e -> listener.onFailure("Erro: " + e.getMessage()));
    }
    
    @Override
    public void obterReservasPorEquipamento(String equipamentoId, OnReservasListener listener) {
        firestore.collection(COLLECTION_RESERVAS)
            .whereEqualTo("equipamentoId", equipamentoId)
            .orderBy("dataReserva")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Reserva> reservas = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    reservas.add(documentSnapshotToReserva(document));
                }
                listener.onSuccess(reservas);
            })
            .addOnFailureListener(e -> listener.onFailure("Erro: " + e.getMessage()));
    }
    
    @Override
    public void obterReservasAtivas(OnReservasListener listener) {
        firestore.collection(COLLECTION_RESERVAS)
            .whereEqualTo("status", "ATIVA")
            .orderBy("dataReserva")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Reserva> reservas = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    reservas.add(documentSnapshotToReserva(document));
                }
                listener.onSuccess(reservas);
            })
            .addOnFailureListener(e -> listener.onFailure("Erro: " + e.getMessage()));
    }
    
    @Override
    public void cancelarReserva(String reservaId, OnBooleanListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "CANCELADA");
        
        firestore.collection(COLLECTION_RESERVAS)
            .document(reservaId)
            .update(updates)
            .addOnSuccessListener(aVoid -> listener.onSuccess(true))
            .addOnFailureListener(e -> listener.onFailure("Erro: " + e.getMessage()));
    }
    
    @Override
    public void verificarConflitoReserva(String equipamentoId, String data, String horaInicio, 
                                         String horaFim, OnBooleanListener listener) {
        // Converte data e horas para Timestamps combinados
        Timestamp horaInicioTimestamp = DateConverter.dateTimeToTimestamp(data, horaInicio);
        Timestamp horaFimTimestamp = DateConverter.dateTimeToTimestamp(data, horaFim);
        Timestamp dataTimestamp = DateConverter.dateToTimestamp(data);
        
        Log.d(TAG, "=== VERIFICANDO CONFLITO ===");
        Log.d(TAG, "EquipamentoId: " + equipamentoId);
        Log.d(TAG, "Data: " + data + " -> " + dataTimestamp);
        Log.d(TAG, "Hora Início: " + horaInicio + " -> " + horaInicioTimestamp);
        Log.d(TAG, "Hora Fim: " + horaFim + " -> " + horaFimTimestamp);
        
        if (dataTimestamp == null || horaInicioTimestamp == null || horaFimTimestamp == null) {
            listener.onFailure("Datas/horas inválidas");
            return;
        }
        
        // Buscar todas as reservas ativas do equipamento na mesma data
        firestore.collection(COLLECTION_RESERVAS)
            .whereEqualTo("equipamentoId", equipamentoId)
            .whereEqualTo("dataReserva", dataTimestamp)
            .whereEqualTo("status", "ATIVA")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "Reservas encontradas: " + queryDocumentSnapshots.size());
                boolean temConflito = false;
                
                // Verifica se há sobreposição de horários
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Timestamp reservaInicio = document.getTimestamp("horaInicio");
                    Timestamp reservaFim = document.getTimestamp("horaFim");
                    
                    Log.d(TAG, "Comparando com reserva: " + document.getId());
                    Log.d(TAG, "  Reserva início: " + reservaInicio);
                    Log.d(TAG, "  Reserva fim: " + reservaFim);
                    
                    if (reservaInicio != null && reservaFim != null) {
                        // Verifica sobreposição:
                        // Nova reserva começa antes da existente terminar E
                        // Nova reserva termina depois da existente começar
                        boolean sobrepoe = horaInicioTimestamp.toDate().before(reservaFim.toDate()) &&
                                          horaFimTimestamp.toDate().after(reservaInicio.toDate());
                        
                        Log.d(TAG, "  Sobrepõe? " + sobrepoe);
                        
                        if (sobrepoe) {
                            temConflito = true;
                            break;
                        }
                    }
                }
                
                Log.d(TAG, "Resultado final - Tem conflito? " + temConflito);
                listener.onSuccess(temConflito);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao verificar conflito", e);
                listener.onFailure("Erro: " + e.getMessage());
            });
    }
    
    @Override
    public void obterReservasPorPeriodo(String dataInicio, String dataFim, OnReservasListener listener) {
        Timestamp dataInicioTimestamp = DateConverter.dateToTimestamp(dataInicio);
        Timestamp dataFimTimestamp = DateConverter.dateToTimestamp(dataFim);
        
        if (dataInicioTimestamp == null || dataFimTimestamp == null) {
            listener.onFailure("Datas inválidas");
            return;
        }
        
        firestore.collection(COLLECTION_RESERVAS)
            .whereGreaterThanOrEqualTo("dataReserva", dataInicioTimestamp)
            .whereLessThanOrEqualTo("dataReserva", dataFimTimestamp)
            .orderBy("dataReserva")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Reserva> reservas = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    reservas.add(documentSnapshotToReserva(document));
                }
                listener.onSuccess(reservas);
            })
            .addOnFailureListener(e -> listener.onFailure("Erro: " + e.getMessage()));
    }
    
    private Map<String, Object> reservaToMap(Reserva reserva) {
        Map<String, Object> data = new HashMap<>();
        
        if (reserva.getId() != null) data.put("id", reserva.getId());
        data.put("equipamentoId", reserva.getEquipamentoId());
        data.put("equipamentoNome", reserva.getEquipamentoNome());
        data.put("usuarioId", reserva.getUsuarioId());
        data.put("usuarioNome", reserva.getUsuarioNome());
        data.put("status", reserva.getStatus());
        
        // Converter datas String -> Timestamp
        Timestamp dataReserva = DateConverter.dateToTimestamp(reserva.getDataReserva());
        if (dataReserva != null) data.put("dataReserva", dataReserva);
        
        // CORREÇÃO: Usar dateTimeToTimestamp para combinar data + hora corretamente
        Timestamp horaInicio = DateConverter.dateTimeToTimestamp(reserva.getDataReserva(), reserva.getHoraInicio());
        if (horaInicio != null) data.put("horaInicio", horaInicio);
        
        Timestamp horaFim = DateConverter.dateTimeToTimestamp(reserva.getDataReserva(), reserva.getHoraFim());
        if (horaFim != null) data.put("horaFim", horaFim);
        
        Timestamp dataCriacao = DateConverter.dateToTimestamp(reserva.getDataCriacao());
        if (dataCriacao != null) {
            data.put("dataCriacao", dataCriacao);
        } else {
            data.put("dataCriacao", Timestamp.now());
        }
        
        return data;
    }
    
    private Reserva documentSnapshotToReserva(DocumentSnapshot document) {
        Reserva reserva = new Reserva();
        
        reserva.setId(document.getId());
        reserva.setEquipamentoId(document.getString("equipamentoId"));
        reserva.setEquipamentoNome(document.getString("equipamentoNome"));
        reserva.setUsuarioId(document.getString("usuarioId"));
        reserva.setUsuarioNome(document.getString("usuarioNome"));
        reserva.setStatus(document.getString("status"));
        
        // Converter Timestamp -> String
        Timestamp dataReserva = document.getTimestamp("dataReserva");
        if (dataReserva != null) {
            reserva.setDataReserva(DateConverter.timestampToDate(dataReserva));
        }
        
        Timestamp horaInicio = document.getTimestamp("horaInicio");
        if (horaInicio != null) {
            reserva.setHoraInicio(DateConverter.timestampToTime(horaInicio));
        }
        
        Timestamp horaFim = document.getTimestamp("horaFim");
        if (horaFim != null) {
            reserva.setHoraFim(DateConverter.timestampToTime(horaFim));
        }
        
        Timestamp dataCriacao = document.getTimestamp("dataCriacao");
        if (dataCriacao != null) {
            reserva.setDataCriacao(DateConverter.timestampToDate(dataCriacao));
        }
        
        return reserva;
    }
}
