package com.fideburguesas.pos.util;

import com.fideburguesas.pos.config.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AdminReset {
    
    public static void main(String[] args) {
        System.out.println("=== RESET USUARIO ADMIN ===");
        
        try {
            resetAdminUser();
            System.out.println("✅ Usuario admin recreado exitosamente");
            System.out.println("📋 Credenciales:");
            System.out.println("   Usuario: admin");
            System.out.println("   Contraseña: password");
            
        } catch (Exception e) {
            System.err.println("❌ Error al recrear usuario admin: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Recrear el usuario admin con contraseña "password"
     */
    public static void resetAdminUser() throws SQLException {
        DatabaseConfig dbConfig = DatabaseConfig.getInstance();
        Connection connection = dbConfig.getConnection();
        
        // Eliminar usuario admin existente
        String deleteSQL = "DELETE FROM usuarios WHERE username = 'admin'";
        try (PreparedStatement stmt = connection.prepareStatement(deleteSQL)) {
            int deleted = stmt.executeUpdate();
            System.out.println("🗑️  Usuarios admin eliminados: " + deleted);
        }
        
        // Crear nuevo hash para "password"
        String password = "password";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        System.out.println("🔐 Nuevo hash generado: " + hashedPassword);
        
        // Insertar nuevo usuario admin
        String insertSQL = "INSERT INTO usuarios " +
            "(username, password, nombre, apellido, email, tipo_usuario, activo) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
        try (PreparedStatement stmt = connection.prepareStatement(insertSQL)) {
            stmt.setString(1, "admin");
            stmt.setString(2, hashedPassword);
            stmt.setString(3, "Administrador");
            stmt.setString(4, "Sistema");
            stmt.setString(5, "admin@fideburguesas.com");
            stmt.setString(6, "ADMIN");
            stmt.setBoolean(7, true);
            
            int inserted = stmt.executeUpdate();
            System.out.println("✨ Nuevo usuario admin creado. Filas insertadas: " + inserted);
        }
        
        // Verificar que funciona
        verifyAdminLogin(password, hashedPassword);
    }
    
    /**
     * Verificar que el login funciona
     */
    private static void verifyAdminLogin(String password, String hash) {
        boolean isValid = BCrypt.checkpw(password, hash);
        System.out.println("🔍 Verificación de contraseña: " + (isValid ? "✅ VÁLIDA" : "❌ INVÁLIDA"));
        
        if (!isValid) {
            System.err.println("⚠️  ADVERTENCIA: La verificación falló, puede haber un problema con BCrypt");
        }
    }
    
    /**
     * Método para usar desde otras clases
     */
    public static boolean recreateAdminUser() {
        try {
            resetAdminUser();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al recrear admin: " + e.getMessage());
            return false;
        }
    }
}