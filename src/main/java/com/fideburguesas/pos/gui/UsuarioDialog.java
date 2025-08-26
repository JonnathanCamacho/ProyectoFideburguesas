package com.fideburguesas.pos.gui;

import com.fideburguesas.pos.model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UsuarioDialog extends JDialog {
    private JTextField usernameField, nombreField, apellidoField, emailField, telefonoField;
    private JPasswordField passwordField;
    private JComboBox<Usuario.TipoUsuario> tipoComboBox;
    private JCheckBox activoCheckBox;
    private boolean confirmed = false;
    private Usuario usuario;
    
    public UsuarioDialog(JFrame parent, String title, Usuario usuario) {
        super(parent, title, true);
        this.usuario = usuario;
        initializeComponents();
        setupLayout();
        setupEventListeners();
        setDefaultConfiguration();
        
        if (usuario != null) {
            loadUsuarioData();
        }
    }
    
    private void initializeComponents() {
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        nombreField = new JTextField(20);
        apellidoField = new JTextField(20);
        emailField = new JTextField(20);
        telefonoField = new JTextField(20);
        
        tipoComboBox = new JComboBox<>(Usuario.TipoUsuario.values());
        activoCheckBox = new JCheckBox("Activo", true);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(passwordField, gbc);
        
        // Nombre
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(nombreField, gbc);
        
        // Apellido
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Apellido:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(apellidoField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(emailField, gbc);
        
        // Teléfono
        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(telefonoField, gbc);
        
        // Tipo Usuario
        gbc.gridx = 0; gbc.gridy = 6; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Tipo Usuario:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(tipoComboBox, gbc);
        
        // Activo
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        formPanel.add(activoCheckBox, gbc);
        
        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");
        
        saveButton.setBackground(new Color(144, 238, 144)); // Light green
        saveButton.setForeground(new Color(0, 100, 0)); // Dark green text
        saveButton.setBorder(BorderFactory.createRaisedBevelBorder());
        saveButton.setFocusPainted(false);
        
        cancelButton.setBackground(new Color(255, 182, 193)); // Light pink
        cancelButton.setForeground(new Color(139, 0, 0)); // Dark red text
        cancelButton.setBorder(BorderFactory.createRaisedBevelBorder());
        cancelButton.setFocusPainted(false);
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Event listeners
        saveButton.addActionListener(e -> save());
        cancelButton.addActionListener(e -> cancel());
    }
    
    private void setupEventListeners() {
        // Ya implementado en setupLayout()
    }
    
    private void loadUsuarioData() {
        usernameField.setText(usuario.getUsername());
        nombreField.setText(usuario.getNombre());
        apellidoField.setText(usuario.getApellido());
        emailField.setText(usuario.getEmail());
        telefonoField.setText(usuario.getTelefono());
        tipoComboBox.setSelectedItem(usuario.getTipoUsuario());
        activoCheckBox.setSelected(usuario.isActivo());
        
        // Para edición, no mostrar password
        passwordField.setText("");
    }
    
    private void save() {
        if (validateFields()) {
            confirmed = true;
            dispose();
        }
    }
    
    private void cancel() {
        confirmed = false;
        dispose();
    }
    
    private boolean validateFields() {
        if (usernameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username es requerido");
            return false;
        }
        
        if (usuario == null && passwordField.getPassword().length == 0) {
            JOptionPane.showMessageDialog(this, "Contraseña es requerida para nuevos usuarios");
            return false;
        }
        
        if (nombreField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre es requerido");
            return false;
        }
        
        if (apellidoField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Apellido es requerido");
            return false;
        }
        
        return true;
    }
    
    public Usuario getUsuario() {
        Usuario user = new Usuario();
        user.setUsername(usernameField.getText().trim());
        
        // Solo actualizar password si se proporcionó uno nuevo
        if (passwordField.getPassword().length > 0) {
            user.setPassword(new String(passwordField.getPassword()));
        } else if (usuario != null) {
            user.setPassword(usuario.getPassword());
        }
        
        user.setNombre(nombreField.getText().trim());
        user.setApellido(apellidoField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setTelefono(telefonoField.getText().trim());
        user.setTipoUsuario((Usuario.TipoUsuario) tipoComboBox.getSelectedItem());
        user.setActivo(activoCheckBox.isSelected());
        
        return user;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    private void setDefaultConfiguration() {
        setSize(400, 350);
        setLocationRelativeTo(getParent());
        setResizable(false);
    }
}