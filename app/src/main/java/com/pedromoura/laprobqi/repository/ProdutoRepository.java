package com.pedromoura.laprobqi.repository;

import com.pedromoura.laprobqi.Produto;
import java.util.List;

public interface ProdutoRepository {
    void adicionarProduto(Produto produto, OnCompleteListener listener);
    void listarProdutos(OnSuccessListener<List<Produto>> listener);
    void atualizarProduto(Produto produto, OnCompleteListener listener);
    void removerProduto(int id, OnCompleteListener listener);

    // Callbacks
    interface OnCompleteListener {
        void onComplete(boolean sucesso, String mensagem);
    }

    interface OnSuccessListener<T> {
        void onSuccess(T result);
    }
}