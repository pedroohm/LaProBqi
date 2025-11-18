package com.pedromoura.laprobqi.di;

import android.content.Context;

import com.pedromoura.laprobqi.repository.EquipamentoRepository;
import com.pedromoura.laprobqi.repository.ProdutoRepository;
import com.pedromoura.laprobqi.repository.RegistroUsoRepository;
import com.pedromoura.laprobqi.repository.ReservaRepository;
import com.pedromoura.laprobqi.repository.UsuarioRepository;
import com.pedromoura.laprobqi.repository.impl.EquipamentoRepositoryFirestore;
import com.pedromoura.laprobqi.repository.impl.EquipamentoRepositorySQLite;
import com.pedromoura.laprobqi.repository.impl.ProdutoRepositoryFirestore;
import com.pedromoura.laprobqi.repository.impl.ProdutoRepositorySQLite;
import com.pedromoura.laprobqi.repository.impl.RegistroUsoRepositoryFirestore;
import com.pedromoura.laprobqi.repository.impl.RegistroUsoRepositorySQLite;
import com.pedromoura.laprobqi.repository.impl.ReservaRepositoryFirestore;
import com.pedromoura.laprobqi.repository.impl.ReservaRepositorySQLite;
import com.pedromoura.laprobqi.repository.impl.UsuarioRepositoryFirebase;
import com.pedromoura.laprobqi.repository.impl.UsuarioRepositorySharedPrefs;

/**
 * Provider centralizado para gerenciar instâncias de repositórios.
 * 
 * Suporta três modos de operação:
 * - SQLITE: Usa SQLite + SharedPreferences (padrão atual)
 * - FIREBASE: Usa Cloud Firestore + Firebase Auth
 * - HYBRID: Combina ambos (ex: SQLite local + sincronização Firebase)
 */
public class RepositoryProvider {
    
    /**
     * Modos de operação do repositório
     */
    public enum Mode {
        SQLITE,    // SQLite + SharedPreferences (padrão)
        FIREBASE,  // Cloud Firestore + Firebase Auth
        HYBRID     // Ambos (sincronização futura)
    }
    
    private static RepositoryProvider instance;
    private static Mode currentMode = Mode.SQLITE; // Modo padrão

    private Context context;
    private ProdutoRepository produtoRepository;
    private UsuarioRepository usuarioRepository;
    private EquipamentoRepository equipamentoRepository;
    private ReservaRepository reservaRepository;
    private RegistroUsoRepository registroUsoRepository;

    private RepositoryProvider(Context context) {
        this.context = context.getApplicationContext();
        initializeRepositories();
    }
    
    /**
     * Inicializa os repositórios de acordo com o modo atual.
     */
    private void initializeRepositories() {
        switch (currentMode) {
            case FIREBASE:
                // Modo Firebase: usa Firestore e Firebase Auth
                this.produtoRepository = new ProdutoRepositoryFirestore();
                this.usuarioRepository = new UsuarioRepositoryFirebase();
                this.equipamentoRepository = new EquipamentoRepositoryFirestore();
                this.reservaRepository = new ReservaRepositoryFirestore();
                this.registroUsoRepository = new RegistroUsoRepositoryFirestore();
                break;
                
            case HYBRID:
                // Modo Híbrido: usa Firebase (para implementar sincronização no futuro)
                this.produtoRepository = new ProdutoRepositoryFirestore();
                this.usuarioRepository = new UsuarioRepositoryFirebase();
                this.equipamentoRepository = new EquipamentoRepositoryFirestore();
                this.reservaRepository = new ReservaRepositoryFirestore();
                this.registroUsoRepository = new RegistroUsoRepositoryFirestore();
                break;
                
            case SQLITE:
            default:
                // Modo SQLite: usa SQLite e SharedPreferences (padrão)
                this.produtoRepository = new ProdutoRepositorySQLite(context);
                this.usuarioRepository = new UsuarioRepositorySharedPrefs(context);
                this.equipamentoRepository = new EquipamentoRepositorySQLite(context);
                this.reservaRepository = new ReservaRepositorySQLite(context);
                this.registroUsoRepository = new RegistroUsoRepositorySQLite(context);
                break;
        }
    }
    
    /**
     * Obtém a instância singleton do RepositoryProvider.
     * 
     * @param context Contexto da aplicação
     * @return Instância do RepositoryProvider
     */
    public static synchronized RepositoryProvider getInstance(Context context) {
        if (instance == null) {
            instance = new RepositoryProvider(context);
        }
        return instance;
    }
    
    /**
     * Altera o modo de operação dos repositórios.
     * IMPORTANTE: Isso recria todas as instâncias dos repositórios.
     * 
     * @param mode Novo modo de operação (SQLITE, FIREBASE, HYBRID)
     */
    public static synchronized void setMode(Mode mode) {
        if (currentMode != mode) {
            currentMode = mode;
            if (instance != null) {
                // Reinicializa os repositórios com o novo modo
                instance.initializeRepositories();
            }
        }
    }
    
    /**
     * Obtém o modo de operação atual.
     * 
     * @return Modo atual (SQLITE, FIREBASE, HYBRID)
     */
    public static Mode getCurrentMode() {
        return currentMode;
    }
    
    /**
     * Reseta a instância do provider (útil para testes).
     * CUIDADO: Use apenas em casos específicos como testes unitários.
     */
    public static synchronized void resetInstance() {
        instance = null;
    }

    public ProdutoRepository getProdutoRepository() {
        return produtoRepository;
    }

    public UsuarioRepository getUsuarioRepository() {
        return usuarioRepository;
    }

    public EquipamentoRepository getEquipamentoRepository() {
        return equipamentoRepository;
    }

    public ReservaRepository getReservaRepository() {
        return reservaRepository;
    }

    public RegistroUsoRepository getRegistroUsoRepository() {
        return registroUsoRepository;
    }
}