package com.fideburguesas.pos.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtil {
    
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "";
    }
    
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMAT) : "";
    }
    
    public static String formatTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(TIME_FORMAT) : "";
    }
    
    public static LocalDate parseDate(String dateText) {
        try {
            return LocalDate.parse(dateText, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    public static LocalDateTime parseDateTime(String dateTimeText) {
        try {
            return LocalDateTime.parse(dateTimeText, DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    public static boolean isToday(LocalDate date) {
        return LocalDate.now().equals(date);
    }
    
    public static boolean isToday(LocalDateTime dateTime) {
        return isToday(dateTime.toLocalDate());
    }
    
    public static String getRelativeTime(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(dateTime, now).toMinutes();
        
        if (minutes < 1) {
            return "Ahora";
        } else if (minutes < 60) {
            return minutes + " min ago";
        } else if (minutes < 1440) { // 24 horas
            return (minutes / 60) + " h ago";
        } else {
            return formatDate(dateTime.toLocalDate());
        }
    }
}