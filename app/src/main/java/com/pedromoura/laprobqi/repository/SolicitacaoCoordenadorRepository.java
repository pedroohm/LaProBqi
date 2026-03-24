package com.pedromoura.laprobqi.repository;

import com.pedromoura.laprobqi.models.SolicitacaoCoordenador;

import java.util.List;


public interface SolicitacaoCoordenadorRepository {
    
    void criarSolicitacao(SolicitacaoCoordenador solicitacao, OnSolicitacaoListener listener);
    
    void listarSolicitacoesPendentes(OnListSolicitacoesListener listener);
    
    void aprovarSolicitacao(String solicitacaoId, String coordenadorId, 
                           String coordenadorNome, OnCompleteListener listener);
    
    void rejeitarSolicitacao(String solicitacaoId, String motivo, OnCompleteListener listener);
    
    void verificarEmailJaSolicitado(String email, OnBooleanListener listener);
    
    void verificarEmailAprovado(String email, OnBooleanListener listener);
    
    // Callbacks
    interface OnSolicitacaoListener {
        void onSuccess(SolicitacaoCoordenador solicitacao);
        void onFailure(String mensagem);
    }
    
    interface OnListSolicitacoesListener {
        void onSuccess(List<SolicitacaoCoordenador> solicitacoes);
        void onFailure(String mensagem);
    }
    
    interface OnCompleteListener {
        void onComplete(boolean sucesso, String mensagem);
    }
    
    interface OnBooleanListener {
        void onSuccess(boolean resultado);
        void onFailure(String mensagem);
    }
}
