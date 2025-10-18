package com.pedromoura.laprobqi.repository.impl;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.pedromoura.laprobqi.BancoDadosProduto;
import com.pedromoura.laprobqi.Produto;
import com.pedromoura.laprobqi.repository.ProdutoRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProdutoRepositorySQLite implements ProdutoRepository {
    private final BancoDadosProduto bancoDados;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public ProdutoRepositorySQLite(Context context) {
        this.bancoDados = BancoDadosProduto.getInstancia(context);
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void adicionarProduto(Produto produto, OnCompleteListener listener) {
        executor.execute(() -> {
            try {
                bancoDados.inserirProduto(produto);
                mainHandler.post(() -> listener.onComplete(true, "Produto adicionado com sucesso"));
            } catch (Exception e) {
                mainHandler.post(() -> listener.onComplete(false, "Erro ao adicionar produto: " + e.getMessage()));
            }
        });
    }

    @Override
    public void listarProdutos(OnSuccessListener<List<Produto>> listener) {
        executor.execute(() -> {
            List<Produto> produtos = bancoDados.listarProdutos();
            mainHandler.post(() -> listener.onSuccess(produtos));
        });
    }

    @Override
    public void atualizarProduto(Produto produto, OnCompleteListener listener) {
        executor.execute(() -> {
            try {
                bancoDados.atualizarProduto(produto);
                mainHandler.post(() -> listener.onComplete(true, "Produto atualizado com sucesso"));
            } catch (Exception e) {
                mainHandler.post(() -> listener.onComplete(false, "Erro ao atualizar produto: " + e.getMessage()));
            }
        });
    }

    @Override
    public void removerProduto(int id, OnCompleteListener listener) {
        executor.execute(() -> {
            try {
                bancoDados.deletarProduto(id);
                mainHandler.post(() -> listener.onComplete(true, "Produto removido com sucesso"));
            } catch (Exception e) {
                mainHandler.post(() -> listener.onComplete(false, "Erro ao remover produto: " + e.getMessage()));
            }
        });
    }
}