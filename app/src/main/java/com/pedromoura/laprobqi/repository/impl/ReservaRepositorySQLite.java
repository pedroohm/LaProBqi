package com.pedromoura.laprobqi.repository.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.pedromoura.laprobqi.database.DatabaseHelper;
import com.pedromoura.laprobqi.models.Reserva;
import com.pedromoura.laprobqi.repository.ReservaRepository;
import java.util.ArrayList;
import java.util.List;

public class ReservaRepositorySQLite implements ReservaRepository {
    
    private static final String TABLE_RESERVAS = "reservas";
    
    private static final String COL_ID = "id";
    private static final String COL_EQUIPAMENTO_ID = "equipamento_id";
    private static final String COL_EQUIPAMENTO_NOME = "equipamento_nome";
    private static final String COL_USUARIO_ID = "usuario_id";
    private static final String COL_USUARIO_NOME = "usuario_nome";
    private static final String COL_DATA_RESERVA = "data_reserva";
    private static final String COL_HORA_INICIO = "hora_inicio";
    private static final String COL_HORA_FIM = "hora_fim";
    private static final String COL_STATUS = "status";
    private static final String COL_DATA_CRIACAO = "data_criacao";
    
    private DatabaseHelper dbHelper;
    
    public ReservaRepositorySQLite(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }
    
    private SQLiteDatabase getDatabase() {
        return dbHelper.getWritableDatabase();
    }
    
    @Override
    public void salvarReserva(Reserva reserva, OnReservaListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_EQUIPAMENTO_ID, reserva.getEquipamentoId());
            values.put(COL_EQUIPAMENTO_NOME, reserva.getEquipamentoNome());
            values.put(COL_USUARIO_ID, reserva.getUsuarioId());
            values.put(COL_USUARIO_NOME, reserva.getUsuarioNome());
            values.put(COL_DATA_RESERVA, reserva.getDataReserva());
            values.put(COL_HORA_INICIO, reserva.getHoraInicio());
            values.put(COL_HORA_FIM, reserva.getHoraFim());
            values.put(COL_STATUS, reserva.getStatus());
            values.put(COL_DATA_CRIACAO, reserva.getDataCriacao());
            
            long id = db.insert(TABLE_RESERVAS, null, values);
            if (id != -1) {
                reserva.setId(String.valueOf(id));
                listener.onSuccess(reserva);
            } else {
                listener.onFailure("Erro ao salvar reserva");
            }
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void obterReserva(String id, OnReservaListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            Cursor cursor = db.query(TABLE_RESERVAS, null, COL_ID + " = ?", 
                                   new String[]{id}, null, null, null);
            
            if (cursor.moveToFirst()) {
                Reserva reserva = criarReservaFromCursor(cursor);
                listener.onSuccess(reserva);
            } else {
                listener.onFailure("Reserva não encontrada");
            }
            cursor.close();
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void obterTodasReservas(OnReservasListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            Cursor cursor = db.query(TABLE_RESERVAS, null, null, null, null, null, 
                                   COL_DATA_RESERVA + " DESC, " + COL_HORA_INICIO);
            
            List<Reserva> reservas = new ArrayList<>();
            while (cursor.moveToNext()) {
                reservas.add(criarReservaFromCursor(cursor));
            }
            cursor.close();
            listener.onSuccess(reservas);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void atualizarReserva(Reserva reserva, OnBooleanListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_EQUIPAMENTO_ID, reserva.getEquipamentoId());
            values.put(COL_EQUIPAMENTO_NOME, reserva.getEquipamentoNome());
            values.put(COL_USUARIO_ID, reserva.getUsuarioId());
            values.put(COL_USUARIO_NOME, reserva.getUsuarioNome());
            values.put(COL_DATA_RESERVA, reserva.getDataReserva());
            values.put(COL_HORA_INICIO, reserva.getHoraInicio());
            values.put(COL_HORA_FIM, reserva.getHoraFim());
            values.put(COL_STATUS, reserva.getStatus());
            
            int rows = db.update(TABLE_RESERVAS, values, COL_ID + " = ?", 
                               new String[]{reserva.getId()});
            listener.onSuccess(rows > 0);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void excluirReserva(String id, OnBooleanListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            int rows = db.delete(TABLE_RESERVAS, COL_ID + " = ?", new String[]{id});
            listener.onSuccess(rows > 0);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void obterReservasPorUsuario(String usuarioId, OnReservasListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            Cursor cursor = db.query(TABLE_RESERVAS, null, COL_USUARIO_ID + " = ?", 
                                   new String[]{usuarioId}, null, null, 
                                   COL_DATA_RESERVA + " DESC, " + COL_HORA_INICIO);
            
            List<Reserva> reservas = new ArrayList<>();
            while (cursor.moveToNext()) {
                reservas.add(criarReservaFromCursor(cursor));
            }
            cursor.close();
            listener.onSuccess(reservas);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void obterReservasPorEquipamento(String equipamentoId, OnReservasListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            Cursor cursor = db.query(TABLE_RESERVAS, null, COL_EQUIPAMENTO_ID + " = ?", 
                                   new String[]{equipamentoId}, null, null, 
                                   COL_DATA_RESERVA + " DESC, " + COL_HORA_INICIO);
            
            List<Reserva> reservas = new ArrayList<>();
            while (cursor.moveToNext()) {
                reservas.add(criarReservaFromCursor(cursor));
            }
            cursor.close();
            listener.onSuccess(reservas);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void obterReservasAtivas(OnReservasListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            Cursor cursor = db.query(TABLE_RESERVAS, null, COL_STATUS + " = ?", 
                                   new String[]{"ATIVA"}, null, null, 
                                   COL_DATA_RESERVA + " DESC, " + COL_HORA_INICIO);
            
            List<Reserva> reservas = new ArrayList<>();
            while (cursor.moveToNext()) {
                reservas.add(criarReservaFromCursor(cursor));
            }
            cursor.close();
            listener.onSuccess(reservas);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void cancelarReserva(String reservaId, OnBooleanListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_STATUS, "CANCELADA");
            
            int rows = db.update(TABLE_RESERVAS, values, COL_ID + " = ?", 
                               new String[]{reservaId});
            listener.onSuccess(rows > 0);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void verificarConflitoReserva(String equipamentoId, String data, String horaInicio, String horaFim, OnBooleanListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            String sql = "SELECT COUNT(*) FROM " + TABLE_RESERVAS + 
                        " WHERE " + COL_EQUIPAMENTO_ID + " = ? AND " + COL_DATA_RESERVA + " = ? AND " + COL_STATUS + " = 'ATIVA' AND " +
                        "((? >= " + COL_HORA_INICIO + " AND ? < " + COL_HORA_FIM + ") OR " +
                        "(? > " + COL_HORA_INICIO + " AND ? <= " + COL_HORA_FIM + ") OR " +
                        "(? <= " + COL_HORA_INICIO + " AND ? >= " + COL_HORA_FIM + "))";
            
            Cursor cursor = db.rawQuery(sql, new String[]{equipamentoId, data, horaInicio, horaInicio, 
                                                         horaFim, horaFim, horaInicio, horaFim});
            
            boolean temConflito = false;
            if (cursor.moveToFirst()) {
                temConflito = cursor.getInt(0) > 0;
            }
            cursor.close();
            listener.onSuccess(temConflito);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    @Override
    public void obterReservasPorPeriodo(String dataInicio, String dataFim, OnReservasListener listener) {
        try {
            SQLiteDatabase db = getDatabase();
            Cursor cursor = db.query(TABLE_RESERVAS, null, 
                                   COL_DATA_RESERVA + " BETWEEN ? AND ?", 
                                   new String[]{dataInicio, dataFim}, null, null, 
                                   COL_DATA_RESERVA + " DESC, " + COL_HORA_INICIO);
            
            List<Reserva> reservas = new ArrayList<>();
            while (cursor.moveToNext()) {
                reservas.add(criarReservaFromCursor(cursor));
            }
            cursor.close();
            listener.onSuccess(reservas);
        } catch (Exception e) {
            listener.onFailure("Erro: " + e.getMessage());
        }
    }
    
    private Reserva criarReservaFromCursor(Cursor cursor) {
        Reserva reserva = new Reserva();
        reserva.setId(cursor.getString(cursor.getColumnIndexOrThrow(COL_ID)));
        reserva.setEquipamentoId(cursor.getString(cursor.getColumnIndexOrThrow(COL_EQUIPAMENTO_ID)));
        reserva.setEquipamentoNome(cursor.getString(cursor.getColumnIndexOrThrow(COL_EQUIPAMENTO_NOME)));
        reserva.setUsuarioId(cursor.getString(cursor.getColumnIndexOrThrow(COL_USUARIO_ID)));
        reserva.setUsuarioNome(cursor.getString(cursor.getColumnIndexOrThrow(COL_USUARIO_NOME)));
        reserva.setDataReserva(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATA_RESERVA)));
        reserva.setHoraInicio(cursor.getString(cursor.getColumnIndexOrThrow(COL_HORA_INICIO)));
        reserva.setHoraFim(cursor.getString(cursor.getColumnIndexOrThrow(COL_HORA_FIM)));
        reserva.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)));
        reserva.setDataCriacao(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATA_CRIACAO)));
        return reserva;
    }
}
