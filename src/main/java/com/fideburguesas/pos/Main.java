package com.fideburguesas.pos;

import com.fideburguesas.pos.config.DatabaseConfig;
import com.fideburguesas.pos.gui.LoginFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Clase principal de la aplicación FideBurguesas POS
 * Sistema de Punto de Venta completo tipo Odoo
 * 
 * @author Tu Nombre
 * @version 1.0
 */
public class Main {
    
    public static void main(String[] args) {
        // Configurar Look and Feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // CORREGIDO
            
            // Configuraciones adicionales de UI
            setupUIDefaults();
            
        } catch (Exception e) {
            System.err.println("Error al configurar Look and Feel: " + e.getMessage());
        }
        
        // Mostrar splash screen
        showSplashScreen();
        
        // Inicializar base de datos
        try {
            DatabaseConfig.getInstance();
            System.out.println("Base de datos inicializada correctamente");
        } catch (Exception e) {
            System.err.println("Error al inicializar base de datos: " + e.getMessage());
            JOptionPane.showMessageDialog(null, 
                "Error al conectar con la base de datos.\nLa aplicación se cerrará.\n\nError: " + e.getMessage(),
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        
        // Iniciar aplicación en EDT
        SwingUtilities.invokeLater(() -> {
            try {
                new LoginFrame().setVisible(true);
            } catch (Exception e) {
                System.err.println("Error al inicializar interfaz: " + e.getMessage());
                JOptionPane.showMessageDialog(null, 
                    "Error al inicializar la aplicación: " + e.getMessage(),
                    "Error de Aplicación", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    /**
     * Configurar defaults de UI para mejor apariencia
     */
    private static void setupUIDefaults() {
        // Configurar colores y fuentes por defecto
        UIManager.put("Button.font", new Font("Arial", Font.PLAIN, 12));
        UIManager.put("Label.font", new Font("Arial", Font.PLAIN, 12));
        UIManager.put("TextField.font", new Font("Arial", Font.PLAIN, 12));
        UIManager.put("Table.font", new Font("Arial", Font.PLAIN, 11));
        UIManager.put("TableHeader.font", new Font("Arial", Font.BOLD, 12));
        
        // Configurar colores
        UIManager.put("Button.background", new Color(74, 144, 226));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE);
        
        // Configurar comportamiento de tablas
        UIManager.put("Table.selectionBackground", new Color(184, 207, 229));
        UIManager.put("Table.selectionForeground", Color.BLACK);
        UIManager.put("Table.gridColor", new Color(208, 215, 229));
        
        // Configurar tooltips
        UIManager.put("ToolTip.background", new Color(255, 255, 225));
        UIManager.put("ToolTip.foreground", Color.BLACK);
        UIManager.put("ToolTip.font", new Font("Arial", Font.PLAIN, 11));
        
        // Configurar scrollbars
        UIManager.put("ScrollBar.width", 16);
    }
    
    /**
     * Mostrar splash screen durante la inicialización
     */
    private static void showSplashScreen() {
        JWindow splash = new JWindow();
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createLineBorder(new Color(74, 144, 226), 2));
        
        // Logo y título
        JLabel titleLabel = new JLabel("FideBurguesas POS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(74, 144, 226));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 20, 10, 20));
        
        JLabel subtitleLabel = new JLabel("Sistema de Punto de Venta", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.GRAY);
        
        JLabel versionLabel = new JLabel("Versión 1.0 - Cargando...", SwingConstants.CENTER);
        versionLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        versionLabel.setForeground(Color.GRAY);
        versionLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 30, 20));
        
        // Barra de progreso
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        progressBar.setString("Inicializando sistema...");
        progressBar.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        
        content.add(titleLabel, BorderLayout.NORTH);
        content.add(subtitleLabel, BorderLayout.CENTER);
        content.add(versionLabel, BorderLayout.SOUTH);
        content.add(progressBar, BorderLayout.PAGE_END);
        
        splash.setContentPane(content);
        splash.setSize(400, 200);
        splash.setLocationRelativeTo(null);
        splash.setVisible(true);
        
        // Simular tiempo de carga
        Timer timer = new Timer(3000, e -> splash.dispose());
        timer.setRepeats(false);
        timer.start();
    }
}