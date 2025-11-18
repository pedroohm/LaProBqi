package com.pedromoura.laprobqi.utils;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utilitário para conversão bidirecional entre String e Timestamp do Firebase.
 * 
 * Formatos utilizados:
 * - Data: "dd/MM/yyyy" (ex: "15/03/2024")
 * - Hora: "HH:mm:ss" (ex: "14:30:00")
 * - Data e Hora: "dd/MM/yyyy HH:mm:ss" (ex: "15/03/2024 14:30:00")
 */
public class DateConverter {
    
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    private static final Locale LOCALE = new Locale("pt", "BR");
    
    /**
     * Converte uma String de data para Timestamp do Firebase.
     * 
     * @param dateString String no formato "dd/MM/yyyy" (ex: "15/03/2024") ou "yyyy-MM-dd" (ex: "2024-03-15")
     * @return Timestamp do Firebase ou null se a conversão falhar
     */
    public static Timestamp dateToTimestamp(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = dateString.trim();
        
        // Tentar primeiro o formato brasileiro dd/MM/yyyy
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, LOCALE);
            sdf.setLenient(false);
            Date date = sdf.parse(trimmed);
            return new Timestamp(date);
        } catch (ParseException e) {
            // Se falhar, tentar formato ISO yyyy-MM-dd
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", LOCALE);
                isoFormat.setLenient(false);
                Date date = isoFormat.parse(trimmed);
                return new Timestamp(date);
            } catch (ParseException e2) {
                e2.printStackTrace();
                return null;
            }
        }
    }
    
    /**
     * Converte um Timestamp do Firebase para String de data.
     * 
     * @param timestamp Timestamp do Firebase
     * @return String no formato "dd/MM/yyyy" ou null se timestamp for null
     */
    public static String timestampToDate(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, LOCALE);
        return sdf.format(timestamp.toDate());
    }
    
    /**
     * Converte uma String de hora para Timestamp do Firebase.
     * Usa a data atual como referência.
     * 
     * @param timeString String no formato "HH:mm:ss" (ex: "14:30:00")
     * @return Timestamp do Firebase ou null se a conversão falhar
     */
    public static Timestamp timeToTimestamp(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Usa a data atual com a hora fornecida
            SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, LOCALE);
            sdf.setLenient(false);
            Date time = sdf.parse(timeString.trim());
            return new Timestamp(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Converte um Timestamp do Firebase para String de hora.
     * 
     * @param timestamp Timestamp do Firebase
     * @return String no formato "HH:mm:ss" ou null se timestamp for null
     */
    public static String timestampToTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, LOCALE);
        return sdf.format(timestamp.toDate());
    }
    
    /**
     * Converte Strings de data e hora para Timestamp do Firebase.
     * 
     * @param dateString String no formato "dd/MM/yyyy" (ex: "15/03/2024")
     * @param timeString String no formato "HH:mm:ss" (ex: "14:30:00")
     * @return Timestamp do Firebase ou null se a conversão falhar
     */
    public static Timestamp dateTimeToTimestamp(String dateString, String timeString) {
        if (dateString == null || dateString.trim().isEmpty() || 
            timeString == null || timeString.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Normalizar hora: adicionar ":00" se estiver no formato "HH:mm"
            String normalizedTime = timeString.trim();
            if (normalizedTime.matches("\\d{2}:\\d{2}")) {
                normalizedTime = normalizedTime + ":00";
            }
            
            String dateTimeString = dateString.trim() + " " + normalizedTime;
            SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT, LOCALE);
            sdf.setLenient(false);
            Date dateTime = sdf.parse(dateTimeString);
            return new Timestamp(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Converte um Timestamp do Firebase para String de data e hora.
     * 
     * @param timestamp Timestamp do Firebase
     * @return String no formato "dd/MM/yyyy HH:mm:ss" ou null se timestamp for null
     */
    public static String timestampToDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT, LOCALE);
        return sdf.format(timestamp.toDate());
    }
    
    /**
     * Extrai a parte de data de um Timestamp.
     * 
     * @param timestamp Timestamp do Firebase
     * @return String no formato "dd/MM/yyyy" ou null se timestamp for null
     */
    public static String extractDate(Timestamp timestamp) {
        return timestampToDate(timestamp);
    }
    
    /**
     * Extrai a parte de hora de um Timestamp.
     * 
     * @param timestamp Timestamp do Firebase
     * @return String no formato "HH:mm:ss" ou null se timestamp for null
     */
    public static String extractTime(Timestamp timestamp) {
        return timestampToTime(timestamp);
    }
    
    /**
     * Valida se uma String está no formato de data válido.
     * 
     * @param dateString String a ser validada
     * @return true se o formato for válido, false caso contrário
     */
    public static boolean isValidDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return false;
        }
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, LOCALE);
            sdf.setLenient(false);
            sdf.parse(dateString.trim());
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
    
    /**
     * Valida se uma String está no formato de hora válido.
     * 
     * @param timeString String a ser validada
     * @return true se o formato for válido, false caso contrário
     */
    public static boolean isValidTime(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return false;
        }
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, LOCALE);
            sdf.setLenient(false);
            sdf.parse(timeString.trim());
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
