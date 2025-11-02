package com.pedromoura.laprobqi.utils;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.pedromoura.laprobqi.di.RepositoryProvider;
import com.pedromoura.laprobqi.models.Usuario;
import com.pedromoura.laprobqi.repository.UsuarioRepository;

/**
 * Helper class para gerenciar permissões de acesso baseadas no nível do usuário (RF02)
 * 
 * Níveis de acesso:
 * - COORDENADOR: Acesso total (criar, editar, deletar)
 * - ALUNO: Acesso restrito (apenas visualizar e usar funcionalidades básicas)
 */
public class PermissionHelper {

    /**
     * Interface funcional simples para callback de sucesso
     */
    public interface OnSuccessCallback {
        void onSuccess(Usuario usuario);
    }

    /**
     * Interface para callback de verificação de permissão (com sucesso e falha)
     */
    public interface PermissionCallback {
        void onPermissionGranted(Usuario usuario);
        void onPermissionDenied();
    }

    /**
     * Interface simples para callback booleano
     */
    public interface BooleanCallback {
        void onResult(boolean isCoordenador);
    }

    /**
     * Verifica se o usuário atual é coordenador (callback booleano simples)
     * 
     * @param context Contexto da aplicação
     * @param callback Callback com resultado booleano
     */
    public static void verificarPermissaoCoordenador(Context context, BooleanCallback callback) {
        UsuarioRepository repository = RepositoryProvider.getInstance(context).getUsuarioRepository();
        
        repository.obterUsuarioAtual(usuario -> {
            boolean isCoordenador = usuario != null && usuario.isCoordenador();
            callback.onResult(isCoordenador);
        });
    }

    /**
     * Verifica permissão e fecha a Activity se não autorizado
     * Uso típico em onCreate() de Activities restritas
     * 
     * @param activity Activity a ser fechada se não autorizado
     * @param mensagemErro Mensagem a ser exibida (ou null para mensagem padrão)
     * @param onSuccess Callback executado se usuário tiver permissão
     */
    public static void verificarPermissaoOuFechar(Activity activity, 
                                                   String mensagemErro,
                                                   OnSuccessCallback onSuccess) {
        UsuarioRepository repository = RepositoryProvider.getInstance(activity).getUsuarioRepository();
        
        repository.obterUsuarioAtual(usuario -> {
            if (usuario == null || !usuario.isCoordenador()) {
                String msg = mensagemErro != null ? mensagemErro : 
                            "Acesso negado: funcionalidade exclusiva para coordenadores";
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
                activity.finish();
            } else {
                if (onSuccess != null) {
                    onSuccess.onSuccess(usuario);
                }
            }
        });
    }

    /**
     * Verifica se usuário tem permissão e mostra toast se não tiver
     * Útil para validar ações em botões/cliques
     * 
     * @param context Contexto
     * @param onSuccess Callback se tiver permissão
     */
    public static void verificarPermissaoComToast(Context context, OnSuccessCallback onSuccess) {
        UsuarioRepository repository = RepositoryProvider.getInstance(context).getUsuarioRepository();
        
        repository.obterUsuarioAtual(usuario -> {
            if (usuario == null || !usuario.isCoordenador()) {
                Toast.makeText(context, 
                    "Você não tem permissão para executar esta ação", 
                    Toast.LENGTH_SHORT).show();
            } else {
                if (onSuccess != null) {
                    onSuccess.onSuccess(usuario);
                }
            }
        });
    }

    /**
     * Obtém usuário atual e executa callback
     * 
     * @param context Contexto
     * @param callback Callback com usuário (pode ser null se não autenticado)
     */
    public static void obterUsuarioAtual(Context context, UsuarioCallback callback) {
        UsuarioRepository repository = RepositoryProvider.getInstance(context).getUsuarioRepository();
        repository.obterUsuarioAtual(callback::onUsuarioCarregado);
    }

    /**
     * Callback simples para obter usuário
     */
    public interface UsuarioCallback {
        void onUsuarioCarregado(Usuario usuario);
    }

    /**
     * Valida se ação de edição/deleção pode ser executada
     * 
     * @param context Contexto
     * @param onAllowed Callback se ação permitida
     * @param showToast Se deve mostrar toast de erro
     */
    public static void validarAcaoEdicao(Context context, Runnable onAllowed, boolean showToast) {
        verificarPermissaoCoordenador(context, isCoordenador -> {
            if (isCoordenador) {
                if (onAllowed != null) {
                    onAllowed.run();
                }
            } else if (showToast) {
                Toast.makeText(context, 
                    "Apenas coordenadores podem editar ou deletar", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
}
