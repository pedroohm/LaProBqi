package com.pedromoura.laprobqi;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.pedromoura.laprobqi.activities.SettingsActivity;
import com.pedromoura.laprobqi.di.RepositoryProvider;

public class MyApp extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        MyApp.context = getApplicationContext();
        
        // Aplicar idioma salvo nas configurações
        SettingsActivity.applySavedLanguage(this);
        
        // ============================================================
        // CONFIGURAÇÃO DO FIREBASE
        // ============================================================
        // OPÇÃO 1: Usar SQLite (versão incial, armazenamento local)
        // RepositoryProvider.setMode(RepositoryProvider.Mode.SQLITE);
        
        // OPÇÃO 2: Usar Firebase
        RepositoryProvider.setMode(RepositoryProvider.Mode.FIREBASE);
        
        // OPÇÃO 3: Modo híbrido (futuro - sincronização)
        // RepositoryProvider.setMode(RepositoryProvider.Mode.HYBRID);
        // ============================================================
        
        Log.i("BANCO_DADOS", "onCreate() " + getClass().getName() + " para obter context!");
        Log.i("REPOSITORY_MODE", "Modo atual: " + RepositoryProvider.getCurrentMode());
    }

    public static Context getAppContext(){
        return MyApp.context;
    }
}
