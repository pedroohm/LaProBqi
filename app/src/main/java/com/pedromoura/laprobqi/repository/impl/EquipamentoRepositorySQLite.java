package com.pedromoura.laprobqi.repository.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.pedromoura.laprobqi.models.Equipamento;
import com.pedromoura.laprobqi.repository.EquipamentoRepository;
import java.util.ArrayList;
import java.util.List;

public class EquipamentoRepositorySQLite implements EquipamentoRepository {
    
    private static final String DATABASE_NAME = "laprobqi.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_EQUIPAMENTOS = "equipamentos";
    
    private static final String COL_ID = "id";
    private static final String COL_NOME = "nome";
    private static final String COL_DESCRICAO = "descricao";
    private static final String COL_STATUS = "status";
    private static final String COL_DATA_CRIACAO = "data_criacao";
    
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;
    
    public EquipamentoRepositorySQLite(Context context) {
        dbHelper = new DatabaseHelper(context);
    }
    
    private SQLiteDatabase getDatabase() {
        if (database == null || !database.isOpen()) {
            database = dbHelper.getWritableDatabase();
        }
        return database;
    }
    
    @Override
    public void salvarEquipamento(Equipamento equipamento, OnEquipamentoListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_NOME, equipamento.getNome());
            values.put(COL_DESCRICAO, equipamento.getDescricao());
            values.put(COL_STATUS, equipamento.getStatus());
            values.put(COL_DATA_CRIACAO, equipamento.getDataCriacao());
            
            long id = db.insert(TABLE_EQUIPAMENTOS, null, values);
            if (id != -1) {
                equipamento.setId(String.valueOf(id));
                listener.onSuccess(equipamento);
            } else {
                listener.onFailure("Erro ao salvar equipamento");
            }
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void obterEquipamento(String id, OnEquipamentoListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            Cursor cursor = db.query(TABLE_EQUIPAMENTOS, null, COL_ID + " = ?", 
                                   new String[]{id}, null, null, null);
            
            if (cursor.moveToFirst()) {
                Equipamento equipamento = criarEquipamentoFromCursor(cursor);
                listener.onSuccess(equipamento);
            } else {
                listener.onFailure("Equipamento não encontrado");
            }
            cursor.close();
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void obterTodosEquipamentos(OnEquipamentosListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            Cursor cursor = db.query(TABLE_EQUIPAMENTOS, null, null, null, null, null, COL_NOME);
            
            List<Equipamento> equipamentos = new ArrayList<>();
            while (cursor.moveToNext()) {
                equipamentos.add(criarEquipamentoFromCursor(cursor));
            }
            cursor.close();
            listener.onSuccess(equipamentos);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void atualizarEquipamento(Equipamento equipamento, OnBooleanListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_NOME, equipamento.getNome());
            values.put(COL_DESCRICAO, equipamento.getDescricao());
            values.put(COL_STATUS, equipamento.getStatus());
            
            int rows = db.update(TABLE_EQUIPAMENTOS, values, COL_ID + " = ?", 
                               new String[]{equipamento.getId()});
            listener.onSuccess(rows > 0);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void excluirEquipamento(String id, OnBooleanListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            int rows = db.delete(TABLE_EQUIPAMENTOS, COL_ID + " = ?", new String[]{id});
            listener.onSuccess(rows > 0);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void obterEquipamentosDisponiveis(OnEquipamentosListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            Cursor cursor = db.query(TABLE_EQUIPAMENTOS, null, COL_STATUS + " = ?", 
                                   new String[]{"DISPONIVEL"}, null, null, COL_NOME);
            
            List<Equipamento> equipamentos = new ArrayList<>();
            while (cursor.moveToNext()) {
                equipamentos.add(criarEquipamentoFromCursor(cursor));
            }
            cursor.close();
            listener.onSuccess(equipamentos);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void buscarEquipamentosPorNome(String nome, OnEquipamentosListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            Cursor cursor = db.query(TABLE_EQUIPAMENTOS, null, COL_NOME + " LIKE ?", 
                                   new String[]{"%" + nome + "%"}, null, null, COL_NOME);
            
            List<Equipamento> equipamentos = new ArrayList<>();
            while (cursor.moveToNext()) {
                equipamentos.add(criarEquipamentoFromCursor(cursor));
            }
            cursor.close();
            listener.onSuccess(equipamentos);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void obterEquipamentosDisponiveisPorPeriodo(String data, String horaInicio, String horaFim, OnEquipamentosListener listener) {
        // Implementação simplificada - em produção seria necessário verificar conflitos com reservas
        obterEquipamentosDisponiveis(listener);
    }
    
    @Override
    public void atualizarStatusEquipamento(String equipamentoId, String novoStatus, OnBooleanListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_STATUS, novoStatus);
            
            int rows = db.update(TABLE_EQUIPAMENTOS, values, COL_ID + " = ?", 
                               new String[]{equipamentoId});
            listener.onSuccess(rows > 0);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    private Equipamento criarEquipamentoFromCursor(Cursor cursor) {
        Equipamento equipamento = new Equipamento();
        equipamento.setId(cursor.getString(cursor.getColumnIndexOrThrow(COL_ID)));
        equipamento.setNome(cursor.getString(cursor.getColumnIndexOrThrow(COL_NOME)));
        equipamento.setDescricao(cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRICAO)));
        equipamento.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)));
        equipamento.setDataCriacao(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATA_CRIACAO)));
        return equipamento;
    }
    
    private static class DatabaseHelper extends SQLiteOpenHelper {
        
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        
        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTable = "CREATE TABLE " + TABLE_EQUIPAMENTOS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NOME + " TEXT NOT NULL, " +
                    COL_DESCRICAO + " TEXT, " +
                    COL_STATUS + " TEXT NOT NULL DEFAULT 'DISPONIVEL', " +
                    COL_DATA_CRIACAO + " TEXT NOT NULL" +
                    ")";
            db.execSQL(createTable);
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EQUIPAMENTOS);
            onCreate(db);
        }
    }
}
