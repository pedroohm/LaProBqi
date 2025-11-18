package com.pedromoura.laprobqi.repository.impl;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pedromoura.laprobqi.models.Usuario;
import com.pedromoura.laprobqi.repository.UsuarioRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementação do repositório de usuários usando Firebase Authentication e Firestore.
 * 
 * Características:
 * - Autenticação via Firebase Auth (email/password)
 * - Dados do usuário (exceto senha) armazenados no Firestore
 * - Senha gerenciada exclusivamente pelo Firebase Auth
 * - Coleção: "usuarios"
 */
public class UsuarioRepositoryFirebase implements UsuarioRepository {
    
    private static final String TAG = "UsuarioRepositoryFirebase";
    private static final String COLLECTION_USUARIOS = "usuarios";
    
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    
    public UsuarioRepositoryFirebase() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }
    
    /**
     * Registra um novo usuário no Firebase Auth e salva seus dados no Firestore.
     * 
     * @param nome Nome do usuário
     * @param email Email do usuário
     * @param senha Senha do usuário (armazenada apenas no Firebase Auth)
     * @param nivelAcesso Nível de acesso do usuário
     * @param listener Callback de resultado
     */
    @Override
    public void registrar(String nome, String email, String senha, String nivelAcesso, 
                          UsuarioRepository.OnCompleteListener listener) {
        
        // Validações
        if (nome == null || nome.trim().isEmpty()) {
            listener.onComplete(false, "Nome não pode estar vazio");
            return;
        }
        if (email == null || email.trim().isEmpty()) {
            listener.onComplete(false, "Email não pode estar vazio");
            return;
        }
        if (senha == null || senha.length() < 6) {
            listener.onComplete(false, "Senha deve ter no mínimo 6 caracteres");
            return;
        }
        if (nivelAcesso == null || nivelAcesso.trim().isEmpty()) {
            listener.onComplete(false, "Nível de acesso não pode estar vazio");
            return;
        }
        
        // 1. Criar usuário no Firebase Auth
        firebaseAuth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener(new com.google.android.gms.tasks.OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            
                            // 2. Salvar dados do usuário no Firestore (sem a senha)
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("id", userId);
                            userData.put("nome", nome.trim());
                            userData.put("email", email.trim());
                            userData.put("nivelAcesso", nivelAcesso.trim());
                            
                            firestore.collection(COLLECTION_USUARIOS)
                                .document(userId)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Usuário registrado com sucesso: " + userId);
                                    listener.onComplete(true, "Usuário registrado com sucesso");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Erro ao salvar dados do usuário no Firestore", e);
                                    // Tenta deletar o usuário do Auth se falhou no Firestore
                                    firebaseUser.delete();
                                    listener.onComplete(false, "Erro ao salvar dados do usuário: " + e.getMessage());
                                });
                        } else {
                            listener.onComplete(false, "Erro ao obter usuário autenticado");
                        }
                    } else {
                        String errorMessage = "Erro ao criar usuário";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        Log.e(TAG, "Erro ao criar usuário no Firebase Auth", task.getException());
                        listener.onComplete(false, errorMessage);
                    }
                }
            });
    }
    
    /**
     * Autentica um usuário usando Firebase Auth.
     * 
     * @param email Email do usuário
     * @param senha Senha do usuário
     * @param listener Callback de resultado com dados do usuário
     */
    @Override
    public void autenticar(String email, String senha, UsuarioRepository.OnAuthListener listener) {
        
        // Validações
        if (email == null || email.trim().isEmpty()) {
            listener.onFailure("Email não pode estar vazio");
            return;
        }
        if (senha == null || senha.trim().isEmpty()) {
            listener.onFailure("Senha não pode estar vazia");
            return;
        }
        
        // Autenticar no Firebase Auth
        firebaseAuth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener(new com.google.android.gms.tasks.OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            
                            // Buscar dados do usuário no Firestore
                            firestore.collection(COLLECTION_USUARIOS)
                                .document(userId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String nome = documentSnapshot.getString("nome");
                                        String nivelAcesso = documentSnapshot.getString("nivelAcesso");
                                        
                                        Log.d(TAG, "Usuário autenticado com sucesso: " + userId);
                                        listener.onSuccess(userId, nome, nivelAcesso);
                                    } else {
                                        Log.e(TAG, "Dados do usuário não encontrados no Firestore");
                                        listener.onFailure("Dados do usuário não encontrados");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Erro ao buscar dados do usuário no Firestore", e);
                                    listener.onFailure("Erro ao buscar dados do usuário: " + e.getMessage());
                                });
                        } else {
                            listener.onFailure("Erro ao obter usuário autenticado");
                        }
                    } else {
                        String errorMessage = "Email ou senha inválidos";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        Log.e(TAG, "Erro ao autenticar usuário", task.getException());
                        listener.onFailure(errorMessage);
                    }
                }
            });
    }
    
    /**
     * Obtém o usuário atualmente autenticado.
     * 
     * @param listener Callback com o objeto Usuario
     */
    @Override
    public void obterUsuarioAtual(UsuarioRepository.OnSuccessListener<Usuario> listener) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        
        if (firebaseUser == null) {
            listener.onSuccess(null);
            return;
        }
        
        String userId = firebaseUser.getUid();
        
        // Buscar dados do usuário no Firestore
        firestore.collection(COLLECTION_USUARIOS)
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Usuario usuario = documentSnapshotToUsuario(documentSnapshot);
                    Log.d(TAG, "Usuário atual obtido: " + userId);
                    listener.onSuccess(usuario);
                } else {
                    Log.e(TAG, "Dados do usuário não encontrados no Firestore");
                    listener.onSuccess(null);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao buscar dados do usuário no Firestore", e);
                listener.onSuccess(null);
            });
    }
    
    /**
     * Faz logout do usuário atual.
     * 
     * @param listener Callback de resultado
     */
    @Override
    public void logout(UsuarioRepository.OnCompleteListener listener) {
        try {
            firebaseAuth.signOut();
            Log.d(TAG, "Logout realizado com sucesso");
            listener.onComplete(true, "Logout realizado com sucesso");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao fazer logout", e);
            listener.onComplete(false, "Erro ao fazer logout: " + e.getMessage());
        }
    }
    
    /**
     * Converte um DocumentSnapshot do Firestore para um objeto Usuario.
     * 
     * @param document DocumentSnapshot do Firestore
     * @return Objeto Usuario
     */
    private Usuario documentSnapshotToUsuario(DocumentSnapshot document) {
        String id = document.getId();
        String nome = document.getString("nome");
        String email = document.getString("email");
        String nivelAcesso = document.getString("nivelAcesso");
        
        // Senha não é armazenada no Firestore, então usamos null
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome(nome);
        usuario.setEmail(email);

        usuario.setNivelAcesso(nivelAcesso);
        
        return usuario;
    }
}
