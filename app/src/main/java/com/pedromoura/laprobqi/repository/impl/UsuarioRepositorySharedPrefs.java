package com.pedromoura.laprobqi.repository.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.pedromoura.laprobqi.models.Usuario;
import com.pedromoura.laprobqi.repository.UsuarioRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class UsuarioRepositorySharedPrefs implements UsuarioRepository {
    private static final String PREFS_NAME = "users";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NIVEL = "user_nivel";

    private final SharedPreferences prefs;
    private final ExecutorService executor;
    private final Handler mainHandler;

    // Regex para validação de senha: mínimo 8 caracteres, uma maiúscula e um número
    //private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*\\d).{8,}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

    public UsuarioRepositorySharedPrefs(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void registrar(String nome, String email, String senha, String nivelAcesso,
                          OnCompleteListener listener) {
        executor.execute(() -> {
            // Validar senha (RF01)
            if (!PASSWORD_PATTERN.matcher(senha).matches()) {
                mainHandler.post(() -> listener.onComplete(false,
                        "A senha deve ter no mínimo 8 caracteres, com pelo menos uma letra maiúscula e um número"));
                return;
            }

            // Verificar se e-mail já existe
            if (prefs.contains(email + "_password")) {
                mainHandler.post(() -> listener.onComplete(false, "E-mail já cadastrado"));
                return;
            }

            // Salvar dados do usuário
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(email + "_password", senha); // ⚠️ Temporário - mudar para hash depois
            editor.putString(email + "_name", nome);
            editor.putString(email + "_nivel", nivelAcesso);
            editor.apply();

            mainHandler.post(() -> listener.onComplete(true, "Usuário cadastrado com sucesso"));
        });
    }

    @Override
    public void autenticar(String email, String senha, OnAuthListener listener) {
        executor.execute(() -> {
            String senhaArmazenada = prefs.getString(email + "_password", null);

            if (senhaArmazenada == null || !senhaArmazenada.equals(senha)) {
                mainHandler.post(() -> listener.onFailure("E-mail ou senha inválidos")); // RF01
                return;
            }

            String nome = prefs.getString(email + "_name", "");
            String nivelAcesso = prefs.getString(email + "_nivel", "ALUNO");

            // Salvar sessão do usuário atual
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USER_ID, email);
            editor.putString(KEY_USER_NAME, nome);
            editor.putString(KEY_USER_EMAIL, email);
            editor.putString(KEY_USER_NIVEL, nivelAcesso);
            editor.apply();

            mainHandler.post(() -> listener.onSuccess(email, nome, nivelAcesso));
        });
    }

    @Override
    public void obterUsuarioAtual(OnSuccessListener<Usuario> listener) {
        executor.execute(() -> {
            String id = prefs.getString(KEY_USER_ID, null);
            if (id == null) {
                mainHandler.post(() -> listener.onSuccess(null));
                return;
            }

            Usuario usuario = new Usuario(
                    id,
                    prefs.getString(KEY_USER_NAME, ""),
                    prefs.getString(KEY_USER_EMAIL, ""),
                    prefs.getString(KEY_USER_NIVEL, "ALUNO")
            );

            mainHandler.post(() -> listener.onSuccess(usuario));
        });
    }

    @Override
    public void logout(OnCompleteListener listener) {
        executor.execute(() -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(KEY_USER_ID);
            editor.remove(KEY_USER_NAME);
            editor.remove(KEY_USER_EMAIL);
            editor.remove(KEY_USER_NIVEL);
            editor.apply();

            mainHandler.post(() -> listener.onComplete(true, "Logout realizado com sucesso"));
        });
    }

    @Override
    public void resetarSenha(String email, OnCompleteListener listener) {
        // SharedPrefs não suporta envio de email
        // Esta implementação é apenas para compatibilidade com a interface
        mainHandler.post(() -> listener.onComplete(false, 
            "Recuperação de senha não disponível no modo offline. Use Firebase."));
    }

    @Override
    public void atualizarSenha(String senhaAtual, String novaSenha, OnCompleteListener listener) {
        executor.execute(() -> {
            String userId = prefs.getString(KEY_USER_ID, null);
            if (userId == null) {
                mainHandler.post(() -> listener.onComplete(false, "Usuário não autenticado"));
                return;
            }
            
            // Validar senha atual
            String senhaArmazenada = prefs.getString(userId + "_password", null);
            if (senhaArmazenada == null || !senhaArmazenada.equals(senhaAtual)) {
                mainHandler.post(() -> listener.onComplete(false, "Senha atual incorreta"));
                return;
            }
            
            // Validar nova senha
            if (novaSenha == null || novaSenha.length() < 6) {
                mainHandler.post(() -> listener.onComplete(false, "Nova senha deve ter no mínimo 6 caracteres"));
                return;
            }
            
            if (senhaAtual.equals(novaSenha)) {
                mainHandler.post(() -> listener.onComplete(false, "A nova senha deve ser diferente da atual"));
                return;
            }
            
            // Atualizar senha
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(userId + "_password", novaSenha);
            editor.apply();
            
            mainHandler.post(() -> listener.onComplete(true, "Senha alterada com sucesso"));
        });
    }
}