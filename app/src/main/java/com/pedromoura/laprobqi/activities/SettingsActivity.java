package com.pedromoura.laprobqi.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.pedromoura.laprobqi.R;
import com.pedromoura.laprobqi.di.RepositoryProvider;
import com.pedromoura.laprobqi.repository.UsuarioRepository;

import java.util.Locale;

public class SettingsActivity extends Activity {

    private RadioGroup radioGroupLanguage;
    private RadioButton radioPortuguese, radioEnglish;
    private TextView textUserName, textUserLevel;
    private SharedPreferences prefs;
    private UsuarioRepository usuarioRepository;
    private static final String PREF_LANGUAGE = "app_language";
    private static final String PREF_LANGUAGE_CODE = "language_code";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        initializeViews();
        loadUserProfile(); // RF02: Carregar perfil do usuário
        loadLanguageSettings();
    }

    private void initializeViews() {
        radioGroupLanguage = findViewById(R.id.radioGroupLanguage);
        radioPortuguese = findViewById(R.id.radioPortuguese);
        radioEnglish = findViewById(R.id.radioEnglish);
        textUserName = findViewById(R.id.textUserName);
        textUserLevel = findViewById(R.id.textUserLevel);
        
        prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        usuarioRepository = RepositoryProvider.getInstance(this).getUsuarioRepository();
    }

    // RF02: Carregar e exibir informações do perfil do usuário
    private void loadUserProfile() {
        usuarioRepository.obterUsuarioAtual(usuario -> {
            if (usuario != null) {
                textUserName.setText("Usuário: " + usuario.getNome());
                
                // Exibir nível de acesso com cores diferentes
                if (usuario.isCoordenador()) {
                    textUserLevel.setText(R.string.level_coordinator);
                    textUserLevel.setTextColor(getResources().getColor(R.color.red_brown));
                } else {
                    textUserLevel.setText(R.string.level_student);
                    textUserLevel.setTextColor(getResources().getColor(R.color.moustard_gold));
                }
            } else {
                textUserName.setText("Usuário: Não identificado");
                textUserLevel.setText("-");
            }
        });
    }

    private void loadLanguageSettings() {
        String currentLanguage = prefs.getString(PREF_LANGUAGE_CODE, "pt");
        
        if ("pt".equals(currentLanguage)) {
            radioPortuguese.setChecked(true);
        } else if ("en".equals(currentLanguage)) {
            radioEnglish.setChecked(true);
        }
        
        // Configurar listener para mudança de idioma
        radioGroupLanguage.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioPortuguese) {
                    changeLanguage("pt");
                } else if (checkedId == R.id.radioEnglish) {
                    changeLanguage("en");
                }
            }
        });
    }

    private void changeLanguage(String languageCode) {
        // Salvar preferência de idioma
        prefs.edit().putString(PREF_LANGUAGE_CODE, languageCode).apply();
        
        // Aplicar mudança de idioma
        setLocale(languageCode);
        
        // Mostrar feedback ao usuário
        String message = languageCode.equals("pt") ? 
            "Idioma alterado para Português" : 
            "Language changed to English";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        
        Configuration config = new Configuration();
        config.locale = locale;
        
        Resources resources = getResources();
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        
        // Reiniciar a activity para aplicar as mudanças
        recreate();
    }

    public void clickSettings(View view) {
        // Navegar de volta para o menu principal
        Intent intent = new Intent(this, InitialMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void clickChangePassword(View view) {
        // TODO: Implementar mudança de senha
        Toast.makeText(this, "Funcionalidade de mudança de senha em desenvolvimento", Toast.LENGTH_SHORT).show();
    }

    public void clickLogout(View view) {
        new AlertDialog.Builder(this)
            .setTitle("Encerrar Sessão")
            .setMessage("Tem certeza que deseja sair da sua conta?")
            .setPositiveButton("Sim", (dialog, which) -> {
                usuarioRepository.logout((sucesso, mensagem) -> {
                    if (sucesso) {
                        Toast.makeText(this, "Sessão encerrada com sucesso", Toast.LENGTH_SHORT).show();
                        
                        // Redirecionar para tela de login
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Erro ao encerrar sessão: " + mensagem, Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    public void clickSaveSettings(View view) {
        // As configurações já são salvas automaticamente quando o usuário muda o idioma
        Toast.makeText(this, "Configurações salvas com sucesso!", Toast.LENGTH_SHORT).show();
        
        // Voltar para o menu principal
        Intent intent = new Intent(this, InitialMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // Método para aplicar o idioma salvo quando o app inicia
    public static void applySavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        String savedLanguage = prefs.getString(PREF_LANGUAGE_CODE, "pt");
        
        // Forçar português como padrão se não houver configuração salva
        if (savedLanguage == null || savedLanguage.isEmpty()) {
            savedLanguage = "pt";
            prefs.edit().putString(PREF_LANGUAGE_CODE, "pt").apply();
        }
        
        Locale locale = new Locale(savedLanguage);
        Locale.setDefault(locale);
        
        Configuration config = new Configuration();
        config.locale = locale;
        
        Resources resources = context.getResources();
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
