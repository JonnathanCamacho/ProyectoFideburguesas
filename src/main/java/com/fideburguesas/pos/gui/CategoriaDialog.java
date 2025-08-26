package com.fideburguesas.pos.gui;

import com.fideburguesas.pos.model.Categoria;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CategoriaDialog extends JDialog {
    private JTextField nombreField, descripcionField;
    private JCheckBox activoCheckBox;
    private boolean confirmed = false;
    private Categoria categoria;
    
    public CategoriaDialog(JFrame parent, String title, Categoria categoria) {
        super(parent, title, true);
        this.categoria = categoria;
        initializeComponents();
        setupLayout();
        setupEventListeners();
        setDefaultConfiguration();
        
        if (categoria != null) {
            loadCategoriaData();
        }
    }
    
    private void initializeComponents() {
        nombreField = new JTextField(20);
        descripcionField = new JTextField(20);
        activoCheckBox = new JCheckBox("Activo", true);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Nombre
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(nombreField, gbc);
        
        // Descripción
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(descripcionField, gbc);
        
        // Activo
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(activoCheckBox, gbc);
        
        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.BLUE);
        
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
        // Enter key para guardar
        nombreField.addActionListener(e -> save());
        descripcionField.addActionListener(e -> save());
    }
    
    private void loadCategoriaData() {
        nombreField.setText(categoria.getNombre());
        descripcionField.setText(categoria.getDescripcion());
        activoCheckBox.setSelected(categoria.isActivo());
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
        if (nombreField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es requerido");
            nombreField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    public Categoria getCategoria() {
        Categoria cat = new Categoria();
        cat.setNombre(nombreField.getText().trim());
        cat.setDescripcion(descripcionField.getText().trim());
        cat.setActivo(activoCheckBox.isSelected());
        return cat;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    private void setDefaultConfiguration() {
        setSize(400, 200);
        setLocationRelativeTo(getParent());
        setResizable(false);
        nombreField.requestFocus();
    }
}