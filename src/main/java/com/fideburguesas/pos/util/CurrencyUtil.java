package com.fideburguesas.pos.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.math.RoundingMode;

public class CurrencyUtil {
    
    private static final NumberFormat CURRENCY_FORMAT = 
        NumberFormat.getCurrencyInstance(new Locale("es", "CR"));
    
    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "₡0.00";
        }
        return CURRENCY_FORMAT.format(amount);
    }
    
    public static String formatCurrency(double amount) {
        return formatCurrency(BigDecimal.valueOf(amount));
    }
    
    public static BigDecimal parseCurrency(String text) {
        if (text == null || text.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        try {
            // Remover símbolos de moneda y espacios
            String cleaned = text.replaceAll("[₡$,\\s]", "");
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
    
    public static BigDecimal calculateTax(BigDecimal amount, double taxRate) {
        return amount.multiply(BigDecimal.valueOf(taxRate));
    }
    
    public static BigDecimal calculateTotal(BigDecimal subtotal, BigDecimal tax) {
        return subtotal.add(tax);
    }
    
    public static BigDecimal roundToTwoDecimals(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}