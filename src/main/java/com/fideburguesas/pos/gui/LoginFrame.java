package com.fideburguesas.pos.gui;

import com.fideburguesas.pos.service.UsuarioService;
import com.fideburguesas.pos.model.Usuario;
import com.fideburguesas.pos.config.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LoginFrame extends JFrame {
    private UsuarioService usuarioService;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    
    public LoginFrame() {
        this.usuarioService = new UsuarioService();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        setDefaultConfiguration();
    }
    
    private void initializeComponents() {
        // Campos de entrada
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        
        // Botones
        loginButton = new JButton("Iniciar Sesión");
        loginButton.setBackground(new Color(74, 144, 226));
        loginButton.setForeground(Color.BLUE);
        loginButton.setFocusPainted(false);
        
        // Etiqueta de estado
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel principal
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Logo y título
        JLabel titleLabel = new JLabel("FideBurguesas POS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(51, 51, 51));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 30, 10);
        mainPanel.add(titleLabel, gbc);
        
        // Subtítulo
        JLabel subtitleLabel = new JLabel("Sistema de Punto de Venta");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 10, 20, 10);
        mainPanel.add(subtitleLabel, gbc);
        
        // Usuario
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 10, 5, 5);
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Usuario:"), gbc);
        
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 10);
        gbc.gridx = 1; gbc.gridy = 2;
        mainPanel.add(usernameField, gbc);
        
        // Contraseña
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 10, 5, 5);
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Contraseña:"), gbc);
        
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 10);
        gbc.gridx = 1; gbc.gridy = 3;
        mainPanel.add(passwordField, gbc);
        
        // Botón login
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 10, 10, 10);
        mainPanel.add(loginButton, gbc);
        
        // Estado
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 20, 10);
        mainPanel.add(statusLabel, gbc);
        
        // Panel de credenciales por defecto
        JPanel credentialsPanel = new JPanel(new FlowLayout());
        credentialsPanel.setBackground(new Color(245, 245, 245));
        credentialsPanel.setBorder(BorderFactory.createTitledBorder("Credenciales por defecto"));
        credentialsPanel.add(new JLabel("Usuario: admin | Contraseña: password"));
        
        // PANEL INFERIOR CON BOTÓN DE RESET ADMIN
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(245, 245, 245));
        
        // Agregar panel de credenciales al centro
        bottomPanel.add(credentialsPanel, BorderLayout.CENTER);
        
        // Crear panel para botón de emergencia
        JPanel emergencyPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        emergencyPanel.setBackground(new Color(245, 245, 245));
        
        // Botón de reset admin
        JButton resetAdminButton = new JButton("🔧 Reset Admin");
        resetAdminButton.setFont(new Font("Arial", Font.BOLD, 10));
        resetAdminButton.setBackground(new Color(220, 20, 60)); // Crimson
        resetAdminButton.setForeground(Color.BLUE);
        resetAdminButton.setFocusPainted(false);
        resetAdminButton.setBorder(BorderFactory.createRaisedBevelBorder());
        resetAdminButton.setToolTipText("Recrear usuario administrador (admin/password)");
        resetAdminButton.addActionListener(e -> resetAdminUser());
        
        emergencyPanel.add(resetAdminButton);
        bottomPanel.add(emergencyPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventListeners() {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        // Enter en cualquier campo realiza login
        KeyListener enterListener = new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {}
        };
        
        usernameField.addKeyListener(enterListener);
        passwordField.addKeyListener(enterListener);
    }
    
    /**
     * MÉTODO PARA RESETEAR EL USUARIO ADMIN
     */
    private void resetAdminUser() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "⚠️ ¿Recrear usuario administrador?\n\n" +
            "Esto eliminará el admin actual y creará uno nuevo con:\n" +
            "• Usuario: admin\n" +
            "• Contraseña: password\n\n" +
            "¿Continuar?",
            "Reset Administrador",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Mostrar indicador de progreso
                statusLabel.setText("Recreando usuario admin...");
                statusLabel.setForeground(Color.BLUE);
                
                // Ejecutar reset
                resetAdminInDatabase();
                
                // Mostrar resultado exitoso
                statusLabel.setText("✅ Usuario admin recreado exitosamente");
                statusLabel.setForeground(new Color(0, 128, 0));
                
                JOptionPane.showMessageDialog(this,
                    "✅ Usuario administrador recreado exitosamente\n\n" +
                    "Credenciales:\n" +
                    "• Usuario: admin\n" +
                    "• Contraseña: password\n\n" +
                    "Ya puedes iniciar sesión.",
                    "Reset Completado",
                    JOptionPane.INFORMATION_MESSAGE);
                    
                // Limpiar campos y poner foco en usuario
                usernameField.setText("");
                passwordField.setText("");
                usernameField.requestFocus();
                
            } catch (Exception ex) {
                statusLabel.setText("❌ Error al recrear admin");
                statusLabel.setForeground(Color.RED);
                
                JOptionPane.showMessageDialog(this,
                    "❌ Error al recrear usuario administrador:\n\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * RECREAR ADMIN EN LA BASE DE DATOS
     */
    private void resetAdminInDatabase() throws SQLException {
        DatabaseConfig dbConfig = DatabaseConfig.getInstance();
        Connection connection = dbConfig.getConnection();
        
        // 1. Eliminar admin existente
        String deleteSQL = "DELETE FROM usuarios WHERE username = 'admin'";
        try (PreparedStatement stmt = connection.prepareStatement(deleteSQL)) {
            int deleted = stmt.executeUpdate();
            System.out.println("Usuarios admin eliminados: " + deleted);
        }
        
        // 2. Generar nuevo hash para "password"
        String password = "password";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        System.out.println("Nuevo hash generado: " + hashedPassword);
        
        // 3. Insertar nuevo usuario admin
        String insertSQL = "INSERT INTO usuarios " +
            "(username, password, nombre, apellido, email, tipo_usuario, activo, fecha_creacion) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
            
        try (PreparedStatement stmt = connection.prepareStatement(insertSQL)) {
            stmt.setString(1, "admin");
            stmt.setString(2, hashedPassword);
            stmt.setString(3, "Administrador");
            stmt.setString(4, "Sistema");
            stmt.setString(5, "admin@fideburguesas.com");
            stmt.setString(6, "ADMIN");
            stmt.setBoolean(7, true);
            
            int inserted = stmt.executeUpdate();
            System.out.println("Nuevo usuario admin creado. Filas insertadas: " + inserted);
        }
        
        // 4. Verificar que funciona
        boolean isValid = BCrypt.checkpw(password, hashedPassword);
        System.out.println("Verificación de contraseña: " + (isValid ? "✅ VÁLIDA" : "❌ INVÁLIDA"));
        
        if (!isValid) {
            throw new RuntimeException("Error en la verificación de la contraseña generada");
        }
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty()) {
            showError("Ingrese el nombre de usuario");
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showError("Ingrese la contraseña");
            passwordField.requestFocus();
            return;
        }
        
        // Mostrar indicador de carga
        loginButton.setEnabled(false);
        loginButton.setText("Verificando...");
        statusLabel.setText("Verificando credenciales...");
        statusLabel.setForeground(Color.BLUE);
        
        // Simular proceso de autenticación en hilo separado
        SwingUtilities.invokeLater(() -> {
            try {
                if (usuarioService.autenticar(username, password)) {
                    statusLabel.setText("Acceso concedido");
                    statusLabel.setForeground(Color.GREEN);
                    
                    // Mostrar ventana principal según tipo de usuario
                    SwingUtilities.invokeLater(() -> openMainWindow());
                } else {
                    showError("Usuario o contraseña incorrectos");
                }
            } catch (Exception e) {
                showError("Error de conexión: " + e.getMessage());
            } finally {
                loginButton.setEnabled(true);
                loginButton.setText("Iniciar Sesión");
            }
        });
    }
    
    private void openMainWindow() {
        Usuario usuario = usuarioService.getUsuarioActual();
        
        switch (usuario.getTipoUsuario()) {
            case ADMIN:
                new AdminMainFrame(usuarioService).setVisible(true);
                break;
            case CAJERO:
                new CajeroMainFrame(usuarioService).setVisible(true);
                break;
            case COCINA:
                new CocinaMainFrame(usuarioService).setVisible(true);
                break;
        }
        
        this.dispose();
    }
    
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(Color.RED);
    }
    
    private void setDefaultConfiguration() {
        setTitle("FideBurguesas POS - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        
        // Establecer icono
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/icon.png")));
        } catch (Exception e) {
            // Icono no encontrado, continuar sin él
        }
        
        // Foco inicial
        usernameField.requestFocus();
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}