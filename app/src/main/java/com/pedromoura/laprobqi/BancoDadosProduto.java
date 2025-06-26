package com.pedromoura.laprobqi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class BancoDadosProduto extends SQLiteOpenHelper {

    private static final String NOME_BANCO = "produto";
    private static final int VERSAO = 1;

    private static BancoDadosProduto instancia;

    public static synchronized BancoDadosProduto getInstancia(Context context) {
        if (instancia == null) {
            instancia = new BancoDadosProduto(context.getApplicationContext());
        }
        return instancia;
    }

    private BancoDadosProduto(Context context) {
        super(context, NOME_BANCO, null, VERSAO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE produto (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nome TEXT NOT NULL," +
                "tipo TEXT NOT NULL," +
                "validade TEXT," +
                "quantidade REAL," +
                "unidade TEXT," +
                "observacoes TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS produto");
        onCreate(db);
    }

    // Inserir produto
    public void inserirProduto(Produto produto) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nome", produto.getNome());
        valores.put("tipo", produto.getTipo());
        valores.put("validade", produto.getValidade());
        valores.put("quantidade", produto.getQuantidade());
        valores.put("unidade", produto.getUnidade());
        valores.put("observacoes", produto.getObservacoes());
        db.insert("produto", null, valores);
    }

    // Atualizar produto
    public void atualizarProduto(Produto produto) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nome", produto.getNome());
        valores.put("tipo", produto.getTipo());
        valores.put("validade", produto.getValidade());
        valores.put("quantidade", produto.getQuantidade());
        valores.put("unidade", produto.getUnidade());
        valores.put("observacoes", produto.getObservacoes());
        db.update("produto", valores, "id = ?", new String[]{String.valueOf(produto.getId())});
    }

    // Deletar produto
    public void deletarProduto(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("produto", "id = ?", new String[]{String.valueOf(id)});
    }

    // Listar todos os produtos
    public List<Produto> listarProdutos() {
        List<Produto> produtos = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM produto", null);

        if (cursor.moveToFirst()) {
            do {
                Produto produto = new Produto(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getDouble(4),
                        cursor.getString(5),
                        cursor.getString(6)
                );
                produtos.add(produto);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return produtos;
    }
}