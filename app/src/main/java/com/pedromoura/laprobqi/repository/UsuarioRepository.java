package com.pedromoura.laprobqi.repository;

import com.pedromoura.laprobqi.models.Usuario;

public interface UsuarioRepository {
    void registrar(String nome, String email, String senha, String nivelAcesso, OnCompleteListener listener);
    void autenticar(String email, String senha, OnAuthListener listener);
    void obterUsuarioAtual(OnSuccessListener<Usuario> listener);
    void logout(OnCompleteListener listener);

    interface OnCompleteListener {
        void onComplete(boolean sucesso, String mensagem);
    }

    interface OnAuthListener {
        void onSuccess(String userId, String nome, String nivelAcesso);
        void onFailure(String mensagem);
    }

    interface OnSuccessListener<T> {
        void onSuccess(T result);
    }
}