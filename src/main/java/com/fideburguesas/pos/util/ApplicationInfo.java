package com.fideburguesas.pos.util;

/**
 * Información de la aplicación
 */
public class ApplicationInfo {
    public static final String APP_NAME = "FideBurguesas POS";
    public static final String VERSION = "1.0.0";
    public static final String BUILD_DATE = "2025-06-17";
    public static final String DEVELOPER = "Tu Nombre";
    public static final String COMPANY = "FideBurguesas";
    public static final String COPYRIGHT = "© 2025 FideBurguesas. Todos los derechos reservados.";
    
    public static String getFullVersion() {
        return APP_NAME + " v" + VERSION + " (" + BUILD_DATE + ")";
    }
    
    public static String getAboutText() {
        return String.format(
            "%s\n\n" +
            "Versión: %s\n" +
            "Fecha de construcción: %s\n" +
            "Desarrollador: %s\n\n" +
            "%s",
            APP_NAME, VERSION, BUILD_DATE, DEVELOPER, COPYRIGHT
        );
    }
}