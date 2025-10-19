package com.pedromoura.laprobqi.di;

import android.content.Context;

import com.pedromoura.laprobqi.repository.EquipamentoRepository;
import com.pedromoura.laprobqi.repository.ProdutoRepository;
import com.pedromoura.laprobqi.repository.RegistroUsoRepository;
import com.pedromoura.laprobqi.repository.ReservaRepository;
import com.pedromoura.laprobqi.repository.UsuarioRepository;
import com.pedromoura.laprobqi.repository.impl.EquipamentoRepositorySQLite;
import com.pedromoura.laprobqi.repository.impl.ProdutoRepositorySQLite;
import com.pedromoura.laprobqi.repository.impl.RegistroUsoRepositorySQLite;
import com.pedromoura.laprobqi.repository.impl.ReservaRepositorySQLite;
import com.pedromoura.laprobqi.repository.impl.UsuarioRepositorySharedPrefs;

public class RepositoryProvider {
    private static RepositoryProvider instance;

    private ProdutoRepository produtoRepository;
    private UsuarioRepository usuarioRepository;
    private EquipamentoRepository equipamentoRepository;
    private ReservaRepository reservaRepository;
    private RegistroUsoRepository registroUsoRepository;

    private RepositoryProvider(Context context) {
        // ✅ Aqui você pode trocar facilmente para Firebase no futuro
        this.produtoRepository = new ProdutoRepositorySQLite(context.getApplicationContext());
        this.usuarioRepository = new UsuarioRepositorySharedPrefs(context.getApplicationContext());
        this.equipamentoRepository = new EquipamentoRepositorySQLite(context.getApplicationContext());
        this.reservaRepository = new ReservaRepositorySQLite(context.getApplicationContext());
        this.registroUsoRepository = new RegistroUsoRepositorySQLite(context.getApplicationContext());
    }

    public static synchronized RepositoryProvider getInstance(Context context) {
        if (instance == null) {
            instance = new RepositoryProvider(context);
        }
        return instance;
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