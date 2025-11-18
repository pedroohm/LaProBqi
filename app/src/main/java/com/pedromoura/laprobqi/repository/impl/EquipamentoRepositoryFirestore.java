package com.pedromoura.laprobqi.repository.impl;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pedromoura.laprobqi.models.Equipamento;
import com.pedromoura.laprobqi.repository.EquipamentoRepository;
import com.pedromoura.laprobqi.utils.DateConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação do repositório de equipamentos usando Cloud Firestore.
 * 
 * Características:
 * - Coleção: "equipamentos"
 * - Conversão de datas: String (model) ↔ Timestamp (Firestore)
 * - Mantém compatibilidade com Activities (modelo não muda)
 */
public class EquipamentoRepositoryFirestore implements EquipamentoRepository {
    
    private static final String TAG = "EquipRepositoryFirestore";
    private static final String COLLECTION_EQUIPAMENTOS = "equipamentos";
    
    private final FirebaseFirestore firestore;
    
    public EquipamentoRepositoryFirestore() {

        this.firestore = FirebaseFirestore.getInstance();
    }
    
    /**
     * Salva um novo equipamento no Firestore.
     * 
     * @param equipamento Equipamento a ser salvo
     * @param listener Callback de resultado
     */
    @Override
    public void salvarEquipamento(Equipamento equipamento, OnEquipamentoListener listener) {
        
        // Validações
        if (equipamento == null) {
            listener.onFailure("Equipamento não pode ser nulo");
            return;
        }
        if (equipamento.getNome() == null || equipamento.getNome().trim().isEmpty()) {
            listener.onFailure("Nome do equipamento não pode estar vazio");
            return;
        }
        
        // Converter modelo para Map do Firestore
        Map<String, Object> equipamentoData = equipamentoToMap(equipamento);
        
        // Se tem ID, usa ele; senão, gera um novo
        if (equipamento.getId() != null && !equipamento.getId().isEmpty()) {
            firestore.collection(COLLECTION_EQUIPAMENTOS)
                .document(equipamento.getId())
                .set(equipamentoData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Equipamento salvo com sucesso: " + equipamento.getId());
                    listener.onSuccess(equipamento);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao salvar equipamento", e);
                    listener.onFailure("Erro ao salvar equipamento: " + e.getMessage());
                });
        } else {
            firestore.collection(COLLECTION_EQUIPAMENTOS)
                .add(equipamentoData)
                .addOnSuccessListener(documentReference -> {
                    String id = documentReference.getId();
                    equipamento.setId(id);
                    Log.d(TAG, "Equipamento criado com sucesso: " + id);
                    listener.onSuccess(equipamento);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao criar equipamento", e);
                    listener.onFailure("Erro ao criar equipamento: " + e.getMessage());
                });
        }
    }
    
    /**
     * Obtém um equipamento por ID.
     * 
     * @param id ID do equipamento
     * @param listener Callback de resultado
     */
    @Override
    public void obterEquipamento(String id, OnEquipamentoListener listener) {
        if (id == null || id.trim().isEmpty()) {
            listener.onFailure("ID do equipamento não pode estar vazio");
            return;
        }
        
        firestore.collection(COLLECTION_EQUIPAMENTOS)
            .document(id)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Equipamento equipamento = documentSnapshotToEquipamento(documentSnapshot);
                    Log.d(TAG, "Equipamento obtido: " + id);
                    listener.onSuccess(equipamento);
                } else {
                    Log.d(TAG, "Equipamento não encontrado: " + id);
                    listener.onFailure("Equipamento não encontrado");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao obter equipamento", e);
                listener.onFailure("Erro ao obter equipamento: " + e.getMessage());
            });
    }
    
    /**
     * Obtém todos os equipamentos.
     * 
     * @param listener Callback de resultado
     */
    @Override
    public void obterTodosEquipamentos(OnEquipamentosListener listener) {
        firestore.collection(COLLECTION_EQUIPAMENTOS)
            .orderBy("nome")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Equipamento> equipamentos = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Equipamento equipamento = documentSnapshotToEquipamento(document);
                    equipamentos.add(equipamento);
                }
                Log.d(TAG, "Total de equipamentos obtidos: " + equipamentos.size());
                listener.onSuccess(equipamentos);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao obter equipamentos", e);
                listener.onFailure("Erro ao obter equipamentos: " + e.getMessage());
            });
    }
    
    /**
     * Atualiza um equipamento existente.
     * 
     * @param equipamento Equipamento com dados atualizados
     * @param listener Callback de resultado
     */
    @Override
    public void atualizarEquipamento(Equipamento equipamento, OnBooleanListener listener) {
        if (equipamento == null || equipamento.getId() == null || equipamento.getId().isEmpty()) {
            listener.onFailure("Equipamento inválido para atualização");
            return;
        }
        
        Map<String, Object> equipamentoData = equipamentoToMap(equipamento);
        
        firestore.collection(COLLECTION_EQUIPAMENTOS)
            .document(equipamento.getId())
            .set(equipamentoData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Equipamento atualizado com sucesso: " + equipamento.getId());
                listener.onSuccess(true);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao atualizar equipamento", e);
                listener.onFailure("Erro ao atualizar equipamento: " + e.getMessage());
            });
    }
    
    /**
     * Exclui um equipamento por ID.
     * 
     * @param id ID do equipamento
     * @param listener Callback de resultado
     */
    @Override
    public void excluirEquipamento(String id, OnBooleanListener listener) {
        if (id == null || id.trim().isEmpty()) {
            listener.onFailure("ID do equipamento não pode estar vazio");
            return;
        }
        
        firestore.collection(COLLECTION_EQUIPAMENTOS)
            .document(id)
            .delete()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Equipamento excluído com sucesso: " + id);
                listener.onSuccess(true);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao excluir equipamento", e);
                listener.onFailure("Erro ao excluir equipamento: " + e.getMessage());
            });
    }
    
    /**
     * Obtém equipamentos disponíveis.
     * 
     * @param listener Callback de resultado
     */
    @Override
    public void obterEquipamentosDisponiveis(OnEquipamentosListener listener) {
        firestore.collection(COLLECTION_EQUIPAMENTOS)
            .whereEqualTo("status", "DISPONIVEL")
            .orderBy("nome")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Equipamento> equipamentos = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Equipamento equipamento = documentSnapshotToEquipamento(document);
                    equipamentos.add(equipamento);
                }
                Log.d(TAG, "Equipamentos disponíveis: " + equipamentos.size());
                listener.onSuccess(equipamentos);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao obter equipamentos disponíveis", e);
                listener.onFailure("Erro ao obter equipamentos disponíveis: " + e.getMessage());
            });
    }
    
    /**
     * Busca equipamentos por nome.
     * 
     * @param nome Nome ou parte do nome do equipamento
     * @param listener Callback de resultado
     */
    @Override
    public void buscarEquipamentosPorNome(String nome, OnEquipamentosListener listener) {
        if (nome == null || nome.trim().isEmpty()) {
            obterTodosEquipamentos(listener);
            return;
        }
        
        String nomeUpper = nome.trim().toUpperCase();
        
        firestore.collection(COLLECTION_EQUIPAMENTOS)
            .orderBy("nome")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Equipamento> equipamentos = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Equipamento equipamento = documentSnapshotToEquipamento(document);
                    if (equipamento.getNome() != null && 
                        equipamento.getNome().toUpperCase().contains(nomeUpper)) {
                        equipamentos.add(equipamento);
                    }
                }
                Log.d(TAG, "Equipamentos encontrados com nome '" + nome + "': " + equipamentos.size());
                listener.onSuccess(equipamentos);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao buscar equipamentos por nome", e);
                listener.onFailure("Erro ao buscar equipamentos: " + e.getMessage());
            });
    }
    
    /**
     * Obtém equipamentos disponíveis em um período específico.
     * Nota: Esta é uma implementação simplificada. Para uma verificação completa,
     * seria necessário consultar as reservas e registros de uso.
     * 
     * @param data Data no formato "dd/MM/yyyy"
     * @param horaInicio Hora de início no formato "HH:mm:ss"
     * @param horaFim Hora de fim no formato "HH:mm:ss"
     * @param listener Callback de resultado
     */
    @Override
    public void obterEquipamentosDisponiveisPorPeriodo(String data, String horaInicio, 
                                                        String horaFim, OnEquipamentosListener listener) {
        // Por simplicidade, retorna equipamentos com status DISPONIVEL
        // Uma implementação completa deveria verificar reservas e registros de uso
        obterEquipamentosDisponiveis(listener);
    }
    
    /**
     * Atualiza o status de um equipamento.
     * 
     * @param equipamentoId ID do equipamento
     * @param novoStatus Novo status do equipamento
     * @param listener Callback de resultado
     */
    @Override
    public void atualizarStatusEquipamento(String equipamentoId, String novoStatus, 
                                           OnBooleanListener listener) {
        if (equipamentoId == null || equipamentoId.trim().isEmpty()) {
            listener.onFailure("ID do equipamento não pode estar vazio");
            return;
        }
        if (novoStatus == null || novoStatus.trim().isEmpty()) {
            listener.onFailure("Status não pode estar vazio");
            return;
        }
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", novoStatus);
        
        firestore.collection(COLLECTION_EQUIPAMENTOS)
            .document(equipamentoId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Status do equipamento atualizado: " + equipamentoId + " -> " + novoStatus);
                listener.onSuccess(true);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao atualizar status do equipamento", e);
                listener.onFailure("Erro ao atualizar status: " + e.getMessage());
            });
    }
    
    /**
     * Converte um objeto Equipamento para Map do Firestore.
     * Realiza a conversão de datas String -> Timestamp.
     * 
     * @param equipamento Objeto Equipamento
     * @return Map com dados para Firestore
     */
    private Map<String, Object> equipamentoToMap(Equipamento equipamento) {
        Map<String, Object> data = new HashMap<>();
        
        if (equipamento.getId() != null) {
            data.put("id", equipamento.getId());
        }
        data.put("nome", equipamento.getNome());
        data.put("descricao", equipamento.getDescricao());
        data.put("status", equipamento.getStatus());
        
        // Converter dataCriacao: String -> Timestamp
        if (equipamento.getDataCriacao() != null && !equipamento.getDataCriacao().isEmpty()) {
            Timestamp timestamp = DateConverter.dateToTimestamp(equipamento.getDataCriacao());
            if (timestamp != null) {
                data.put("dataCriacao", timestamp);
            } else {
                // Se a conversão falhar, usa timestamp atual
                data.put("dataCriacao", Timestamp.now());
            }
        } else {
            data.put("dataCriacao", Timestamp.now());
        }
        
        return data;
    }
    
    /**
     * Converte um DocumentSnapshot do Firestore para objeto Equipamento.
     * Realiza a conversão de datas Timestamp -> String.
     * 
     * @param document DocumentSnapshot do Firestore
     * @return Objeto Equipamento
     */
    private Equipamento documentSnapshotToEquipamento(DocumentSnapshot document) {
        Equipamento equipamento = new Equipamento();
        
        equipamento.setId(document.getId());
        equipamento.setNome(document.getString("nome"));
        equipamento.setDescricao(document.getString("descricao"));
        equipamento.setStatus(document.getString("status"));
        
        // Converter dataCriacao: Timestamp -> String
        Timestamp dataCriacaoTimestamp = document.getTimestamp("dataCriacao");
        if (dataCriacaoTimestamp != null) {
            String dataCriacaoString = DateConverter.timestampToDate(dataCriacaoTimestamp);
            equipamento.setDataCriacao(dataCriacaoString);
        }
        
        return equipamento;
    }
}
