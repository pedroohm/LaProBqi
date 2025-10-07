package com.pedromoura.laprobqi;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.pedromoura.laprobqi.activities.SettingsActivity;

public class MyApp extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        MyApp.context = getApplicationContext();
        
        // Aplicar idioma salvo nas configurações
        SettingsActivity.applySavedLanguage(this);
        
        Log.i("BANCO_DADOS", "onCreate() " + getClass().getName() + " para obter context!");
    }

    public static Context getAppContext(){
        return MyApp.context;
    }
}
