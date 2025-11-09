package com.pedromoura.laprobqi.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Classe centralizada para gerenciar o banco de dados SQLite.
 * Todas as tabelas devem ser criadas aqui para evitar múltiplos bancos.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    
    // Nome e versão do banco de dados
    private static final String DATABASE_NAME = "laprobqi.db";
    private static final int DATABASE_VERSION = 5; // Mudança: id de equipamentos agora é INTEGER AUTOINCREMENT
    
    // Singleton
    private static DatabaseHelper instance;
    
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }
    
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "Criando banco de dados versão " + DATABASE_VERSION);
        
        // 1. Tabela de Produtos
        criarTabelaProdutos(db);
        
        // 2. Tabela de Equipamentos
        criarTabelaEquipamentos(db);
        
        // 3. Tabela de Reservas
        criarTabelaReservas(db);
        
        // 4. Tabela de Registro de Uso
        criarTabelaRegistroUso(db);
        
        Log.i(TAG, "Banco de dados criado com sucesso!");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "Atualizando banco de dados de versão " + oldVersion + " para " + newVersion);
        
        // Por enquanto, vamos dropar e recriar as tabelas
        // Em produção, você faria migrações mais sofisticadas
        db.execSQL("DROP TABLE IF EXISTS produto");
        db.execSQL("DROP TABLE IF EXISTS equipamentos");
        db.execSQL("DROP TABLE IF EXISTS reservas");
        db.execSQL("DROP TABLE IF EXISTS registros_uso");
        
        onCreate(db);
    }
    
    // ========== TABELA DE PRODUTOS ==========
    private void criarTabelaProdutos(SQLiteDatabase db) {
        try {
            String sql = "CREATE TABLE produto (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nome TEXT NOT NULL," +
                    "tipo TEXT NOT NULL," +
                    "validade TEXT," +
                    "quantidade REAL DEFAULT 0," +
                    "unidade TEXT," +
                    "observacoes TEXT)";
            db.execSQL(sql);
            Log.i(TAG, "Tabela 'produto' criada");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar tabela produto: " + e.getMessage());
        }
    }
    
    // ========== TABELA DE EQUIPAMENTOS ==========
    private void criarTabelaEquipamentos(SQLiteDatabase db) {
        try {
            String sql = "CREATE TABLE equipamentos (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nome TEXT NOT NULL," +
                    "descricao TEXT," +
                    "categoria TEXT," +
                    "status TEXT NOT NULL DEFAULT 'DISPONIVEL'," +
                    "localizacao TEXT," +
                    "responsavel TEXT," +
                    "data_aquisicao TEXT," +
                    "numero_serie TEXT," +
                    "observacoes TEXT," +
                    "data_criacao TEXT NOT NULL)";
            db.execSQL(sql);
            Log.i(TAG, "Tabela 'equipamentos' criada");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar tabela equipamentos: " + e.getMessage());
        }
    }
    
    // ========== TABELA DE RESERVAS ==========
    private void criarTabelaReservas(SQLiteDatabase db) {
        try {
            String sql = "CREATE TABLE reservas (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "equipamento_id INTEGER NOT NULL," +
                    "equipamento_nome TEXT NOT NULL," +
                    "usuario_id TEXT NOT NULL," +
                    "usuario_nome TEXT NOT NULL," +
                    "data_reserva TEXT NOT NULL," +
                    "hora_inicio TEXT NOT NULL," +
                    "hora_fim TEXT NOT NULL," +
                    "status TEXT NOT NULL DEFAULT 'ATIVA'," +
                    "data_criacao TEXT NOT NULL," +
                    "FOREIGN KEY(equipamento_id) REFERENCES equipamentos(id))";
            db.execSQL(sql);
            Log.i(TAG, "Tabela 'reservas' criada");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar tabela reservas: " + e.getMessage());
        }
    }
    
    // ========== TABELA DE REGISTRO DE USO ==========
    private void criarTabelaRegistroUso(SQLiteDatabase db) {
        try {
            String sql = "CREATE TABLE registros_uso (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "equipamento_id INTEGER NOT NULL," +
                    "equipamento_nome TEXT NOT NULL," +
                    "usuario_id TEXT NOT NULL," +
                    "usuario_nome TEXT NOT NULL," +
                    "data_inicio TEXT NOT NULL," +
                    "hora_inicio TEXT NOT NULL," +
                    "data_fim TEXT," +
                    "hora_fim TEXT," +
                    "observacoes TEXT," +
                    "status TEXT NOT NULL DEFAULT 'EM_USO'," +
                    "FOREIGN KEY(equipamento_id) REFERENCES equipamentos(id))";
            db.execSQL(sql);
            Log.i(TAG, "Tabela 'registros_uso' criada");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar tabela registros_uso: " + e.getMessage());
        }
    }
    
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Habilita foreign keys
        db.execSQL("PRAGMA foreign_keys=ON");
    }
}
