package com.fideburguesas.pos.util;

import java.util.regex.Pattern;

public class ValidationUtil {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^[0-9]{8,15}$");
    
    private static final Pattern CODE_PATTERN = 
        Pattern.compile("^[A-Z0-9]{3,10}$");
    
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.replaceAll("[\\s()-]", "")).matches();
    }
    
    public static boolean isValidCode(String code) {
        return code != null && CODE_PATTERN.matcher(code.toUpperCase()).matches();
    }
    
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    public static boolean isValidNumber(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static boolean isValidInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static String cleanString(String value) {
        return value != null ? value.trim() : "";
    }
    
    public static String formatPhone(String phone) {
        String cleaned = phone.replaceAll("[\\s()-]", "");
        if (cleaned.length() == 8) {
            return cleaned.substring(0, 4) + "-" + cleaned.substring(4);
        }
        return phone;
    }
    
    public static String generateCode(String prefix, int sequence) {
        return String.format("%s%04d", prefix, sequence);
    }
}