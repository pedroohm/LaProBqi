package com.pedromoura.laprobqi.di;

import android.content.Context;

import com.pedromoura.laprobqi.repository.ProdutoRepository;
import com.pedromoura.laprobqi.repository.UsuarioRepository;
import com.pedromoura.laprobqi.repository.impl.ProdutoRepositorySQLite;
import com.pedromoura.laprobqi.repository.impl.UsuarioRepositorySharedPrefs;

public class RepositoryProvider {
    private static RepositoryProvider instance;

    private ProdutoRepository produtoRepository;
    private UsuarioRepository usuarioRepository;

    private RepositoryProvider(Context context) {
        // ✅ Aqui você pode trocar facilmente para Firebase no futuro
        this.produtoRepository = new ProdutoRepositorySQLite(context.getApplicationContext());
        this.usuarioRepository = new UsuarioRepositorySharedPrefs(context.getApplicationContext());
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
}