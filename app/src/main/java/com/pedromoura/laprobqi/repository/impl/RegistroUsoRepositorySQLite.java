package com.pedromoura.laprobqi.repository.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.pedromoura.laprobqi.models.RegistroUso;
import com.pedromoura.laprobqi.repository.RegistroUsoRepository;
import java.util.ArrayList;
import java.util.List;

public class RegistroUsoRepositorySQLite implements RegistroUsoRepository {
    
    private static final String DATABASE_NAME = "laprobqi.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_REGISTROS_USO = "registros_uso";
    
    private static final String COL_ID = "id";
    private static final String COL_EQUIPAMENTO_ID = "equipamento_id";
    private static final String COL_EQUIPAMENTO_NOME = "equipamento_nome";
    private static final String COL_USUARIO_ID = "usuario_id";
    private static final String COL_USUARIO_NOME = "usuario_nome";
    private static final String COL_RESERVA_ID = "reserva_id";
    private static final String COL_DATA_INICIO = "data_inicio";
    private static final String COL_HORA_INICIO = "hora_inicio";
    private static final String COL_DATA_FIM = "data_fim";
    private static final String COL_HORA_FIM = "hora_fim";
    private static final String COL_STATUS = "status";
    private static final String COL_OBSERVACOES = "observacoes";
    
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;
    
    public RegistroUsoRepositorySQLite(Context context) {
        dbHelper = new DatabaseHelper(context);
    }
    
    private SQLiteDatabase getDatabase() {
        if (database == null || !database.isOpen()) {
            database = dbHelper.getWritableDatabase();
        }
        return database;
    }
    
    @Override
    public void salvarRegistroUso(RegistroUso registroUso, OnRegistroUsoListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_EQUIPAMENTO_ID, registroUso.getEquipamentoId());
            values.put(COL_EQUIPAMENTO_NOME, registroUso.getEquipamentoNome());
            values.put(COL_USUARIO_ID, registroUso.getUsuarioId());
            values.put(COL_USUARIO_NOME, registroUso.getUsuarioNome());
            values.put(COL_RESERVA_ID, registroUso.getReservaId());
            values.put(COL_DATA_INICIO, registroUso.getDataInicio());
            values.put(COL_HORA_INICIO, registroUso.getHoraInicio());
            values.put(COL_DATA_FIM, registroUso.getDataFim());
            values.put(COL_HORA_FIM, registroUso.getHoraFim());
            values.put(COL_STATUS, registroUso.getStatus());
            values.put(COL_OBSERVACOES, registroUso.getObservacoes());
            
            long id = db.insert(TABLE_REGISTROS_USO, null, values);
            if (id != -1) {
                registroUso.setId(String.valueOf(id));
                listener.onSuccess(registroUso);
            } else {
                listener.onFailure("Erro ao salvar registro de uso");
            }
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void obterRegistroUso(String id, OnRegistroUsoListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            Cursor cursor = db.query(TABLE_REGISTROS_USO, null, COL_ID + " = ?", 
                                   new String[]{id}, null, null, null);
            
            if (cursor.moveToFirst()) {
                RegistroUso registroUso = criarRegistroUsoFromCursor(cursor);
                listener.onSuccess(registroUso);
            } else {
                listener.onFailure("Registro de uso não encontrado");
            }
            cursor.close();
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void obterTodosRegistrosUso(OnRegistrosUsoListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            Cursor cursor = db.query(TABLE_REGISTROS_USO, null, null, null, null, null, 
                                   COL_DATA_INICIO + " DESC, " + COL_HORA_INICIO);
            
            List<RegistroUso> registrosUso = new ArrayList<>();
            while (cursor.moveToNext()) {
                registrosUso.add(criarRegistroUsoFromCursor(cursor));
            }
            cursor.close();
            listener.onSuccess(registrosUso);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void atualizarRegistroUso(RegistroUso registroUso, OnBooleanListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_EQUIPAMENTO_ID, registroUso.getEquipamentoId());
            values.put(COL_EQUIPAMENTO_NOME, registroUso.getEquipamentoNome());
            values.put(COL_USUARIO_ID, registroUso.getUsuarioId());
            values.put(COL_USUARIO_NOME, registroUso.getUsuarioNome());
            values.put(COL_RESERVA_ID, registroUso.getReservaId());
            values.put(COL_DATA_INICIO, registroUso.getDataInicio());
            values.put(COL_HORA_INICIO, registroUso.getHoraInicio());
            values.put(COL_DATA_FIM, registroUso.getDataFim());
            values.put(COL_HORA_FIM, registroUso.getHoraFim());
            values.put(COL_STATUS, registroUso.getStatus());
            values.put(COL_OBSERVACOES, registroUso.getObservacoes());
            
            int rows = db.update(TABLE_REGISTROS_USO, values, COL_ID + " = ?", 
                               new String[]{registroUso.getId()});
            listener.onSuccess(rows > 0);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void excluirRegistroUso(String id, OnBooleanListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            int rows = db.delete(TABLE_REGISTROS_USO, COL_ID + " = ?", new String[]{id});
            listener.onSuccess(rows > 0);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void obterRegistrosUsoPorUsuario(String usuarioId, OnRegistrosUsoListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            Cursor cursor = db.query(TABLE_REGISTROS_USO, null, COL_USUARIO_ID + " = ?", 
                                   new String[]{usuarioId}, null, null, 
                                   COL_DATA_INICIO + " DESC, " + COL_HORA_INICIO);
            
            List<RegistroUso> registrosUso = new ArrayList<>();
            while (cursor.moveToNext()) {
                registrosUso.add(criarRegistroUsoFromCursor(cursor));
            }
            cursor.close();
            listener.onSuccess(registrosUso);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void obterRegistrosUsoPorEquipamento(String equipamentoId, OnRegistrosUsoListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            Cursor cursor = db.query(TABLE_REGISTROS_USO, null, COL_EQUIPAMENTO_ID + " = ?", 
                                   new String[]{equipamentoId}, null, null, 
                                   COL_DATA_INICIO + " DESC, " + COL_HORA_INICIO);
            
            List<RegistroUso> registrosUso = new ArrayList<>();
            while (cursor.moveToNext()) {
                registrosUso.add(criarRegistroUsoFromCursor(cursor));
            }
            cursor.close();
            listener.onSuccess(registrosUso);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void obterRegistrosUsoEmAndamento(OnRegistrosUsoListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            Cursor cursor = db.query(TABLE_REGISTROS_USO, null, COL_STATUS + " = ?", 
                                   new String[]{"EM_ANDAMENTO"}, null, null, 
                                   COL_DATA_INICIO + " DESC, " + COL_HORA_INICIO);
            
            List<RegistroUso> registrosUso = new ArrayList<>();
            while (cursor.moveToNext()) {
                registrosUso.add(criarRegistroUsoFromCursor(cursor));
            }
            cursor.close();
            listener.onSuccess(registrosUso);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void finalizarRegistroUso(String registroId, String observacoes, OnBooleanListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_STATUS, "FINALIZADO");
            values.put(COL_DATA_FIM, String.valueOf(System.currentTimeMillis()));
            values.put(COL_HORA_FIM, String.valueOf(System.currentTimeMillis()));
            values.put(COL_OBSERVACOES, observacoes);
            
            int rows = db.update(TABLE_REGISTROS_USO, values, COL_ID + " = ?", 
                               new String[]{registroId});
            listener.onSuccess(rows > 0);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void obterRegistrosUsoPorPeriodo(String dataInicio, String dataFim, OnRegistrosUsoListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            Cursor cursor = db.query(TABLE_REGISTROS_USO, null, 
                                   COL_DATA_INICIO + " BETWEEN ? AND ?", 
                                   new String[]{dataInicio, dataFim}, null, null, 
                                   COL_DATA_INICIO + " DESC, " + COL_HORA_INICIO);
            
            List<RegistroUso> registrosUso = new ArrayList<>();
            while (cursor.moveToNext()) {
                registrosUso.add(criarRegistroUsoFromCursor(cursor));
            }
            cursor.close();
            listener.onSuccess(registrosUso);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void obterRegistrosUsoPorUsuarioEPeriodo(String usuarioId, String dataInicio, String dataFim, OnRegistrosUsoListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            Cursor cursor = db.query(TABLE_REGISTROS_USO, null, 
                                   COL_USUARIO_ID + " = ? AND " + COL_DATA_INICIO + " BETWEEN ? AND ?", 
                                   new String[]{usuarioId, dataInicio, dataFim}, null, null, 
                                   COL_DATA_INICIO + " DESC, " + COL_HORA_INICIO);
            
            List<RegistroUso> registrosUso = new ArrayList<>();
            while (cursor.moveToNext()) {
                registrosUso.add(criarRegistroUsoFromCursor(cursor));
            }
            cursor.close();
            listener.onSuccess(registrosUso);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    private RegistroUso criarRegistroUsoFromCursor(Cursor cursor) {
        RegistroUso registroUso = new RegistroUso();
        registroUso.setId(cursor.getString(cursor.getColumnIndexOrThrow(COL_ID)));
        registroUso.setEquipamentoId(cursor.getString(cursor.getColumnIndexOrThrow(COL_EQUIPAMENTO_ID)));
        registroUso.setEquipamentoNome(cursor.getString(cursor.getColumnIndexOrThrow(COL_EQUIPAMENTO_NOME)));
        registroUso.setUsuarioId(cursor.getString(cursor.getColumnIndexOrThrow(COL_USUARIO_ID)));
        registroUso.setUsuarioNome(cursor.getString(cursor.getColumnIndexOrThrow(COL_USUARIO_NOME)));
        registroUso.setReservaId(cursor.getString(cursor.getColumnIndexOrThrow(COL_RESERVA_ID)));
        registroUso.setDataInicio(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATA_INICIO)));
        registroUso.setHoraInicio(cursor.getString(cursor.getColumnIndexOrThrow(COL_HORA_INICIO)));
        registroUso.setDataFim(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATA_FIM)));
        registroUso.setHoraFim(cursor.getString(cursor.getColumnIndexOrThrow(COL_HORA_FIM)));
        registroUso.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)));
        registroUso.setObservacoes(cursor.getString(cursor.getColumnIndexOrThrow(COL_OBSERVACOES)));
        return registroUso;
    }
    
    private static class DatabaseHelper extends SQLiteOpenHelper {
        
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        
        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTable = "CREATE TABLE " + TABLE_REGISTROS_USO + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_EQUIPAMENTO_ID + " TEXT NOT NULL, " +
                    COL_EQUIPAMENTO_NOME + " TEXT NOT NULL, " +
                    COL_USUARIO_ID + " TEXT NOT NULL, " +
                    COL_USUARIO_NOME + " TEXT NOT NULL, " +
                    COL_RESERVA_ID + " TEXT, " +
                    COL_DATA_INICIO + " TEXT NOT NULL, " +
                    COL_HORA_INICIO + " TEXT NOT NULL, " +
                    COL_DATA_FIM + " TEXT, " +
                    COL_HORA_FIM + " TEXT, " +
                    COL_STATUS + " TEXT NOT NULL DEFAULT 'EM_ANDAMENTO', " +
                    COL_OBSERVACOES + " TEXT" +
                    ")";
            db.execSQL(createTable);
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_REGISTROS_USO);
            onCreate(db);
        }
    }
}
