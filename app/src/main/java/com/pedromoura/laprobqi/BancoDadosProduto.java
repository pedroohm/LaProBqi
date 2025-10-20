package com.pedromoura.laprobqi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.pedromoura.laprobqi.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe para gerenciar operações de produtos no banco de dados.
 * Agora usa o DatabaseHelper centralizado.
 */
public class BancoDadosProduto {

    private static final String TAG = "BancoDadosProduto";

    private static BancoDadosProduto instancia;
    private DatabaseHelper dbHelper;

    public static synchronized BancoDadosProduto getInstancia(Context context) {
        if (instancia == null) {
            instancia = new BancoDadosProduto(context.getApplicationContext());
        }
        return instancia;
    }

    private BancoDadosProduto(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }
    
    private SQLiteDatabase getWritableDatabase() {
        return dbHelper.getWritableDatabase();
    }
    
    private SQLiteDatabase getReadableDatabase() {
        return dbHelper.getReadableDatabase();
    }

    // Inserir produto com tratamento de erro
    public boolean inserirProduto(Produto produto) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues valores = new ContentValues();
            valores.put("nome", produto.getNome());
            valores.put("tipo", produto.getTipo());
            valores.put("validade", produto.getValidade());
            valores.put("quantidade", produto.getQuantidade());
            valores.put("unidade", produto.getUnidade());
            valores.put("observacoes", produto.getObservacoes());
            
            long id = db.insert("produto", null, valores);
            Log.i(TAG, "Produto inserido com ID: " + id);
            return id != -1;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inserir produto: " + e.getMessage());
            return false;
        }
    }

    // Atualizar produto com tratamento de erro
    public boolean atualizarProduto(Produto produto) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues valores = new ContentValues();
            valores.put("nome", produto.getNome());
            valores.put("tipo", produto.getTipo());
            valores.put("validade", produto.getValidade());
            valores.put("quantidade", produto.getQuantidade());
            valores.put("unidade", produto.getUnidade());
            valores.put("observacoes", produto.getObservacoes());
            
            int linhasAfetadas = db.update("produto", valores, "id = ?", 
                    new String[]{String.valueOf(produto.getId())});
            Log.i(TAG, "Produto atualizado. Linhas afetadas: " + linhasAfetadas);
            return linhasAfetadas > 0;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar produto: " + e.getMessage());
            return false;
        }
    }

    // Deletar produto com tratamento de erro
    public boolean deletarProduto(int id) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            int linhasAfetadas = db.delete("produto", "id = ?", 
                    new String[]{String.valueOf(id)});
            Log.i(TAG, "Produto deletado. Linhas afetadas: " + linhasAfetadas);
            return linhasAfetadas > 0;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao deletar produto: " + e.getMessage());
            return false;
        }
    }

    // Listar todos os produtos com tratamento de erro
    public List<Produto> listarProdutos() {
        List<Produto> produtos = new ArrayList<>();
        Cursor cursor = null;
        
        try {
            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM produto ORDER BY nome", null);

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
            Log.i(TAG, "Listados " + produtos.size() + " produtos");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao listar produtos: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return produtos;
    }

    // Buscar produto por ID
    public Produto buscarProdutoPorId(int id) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM produto WHERE id = ?", 
                    new String[]{String.valueOf(id)});
            
            if (cursor.moveToFirst()) {
                return new Produto(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getDouble(4),
                        cursor.getString(5),
                        cursor.getString(6)
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar produto por ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    // Buscar produtos por nome (para pesquisa)
    public List<Produto> buscarProdutosPorNome(String nome) {
        List<Produto> produtos = new ArrayList<>();
        Cursor cursor = null;
        
        try {
            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM produto WHERE nome LIKE ? ORDER BY nome", 
                    new String[]{"%" + nome + "%"});

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
        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar produtos por nome: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return produtos;
    }
}
