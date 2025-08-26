package com.fideburguesas.pos.gui;

import com.fideburguesas.pos.service.UsuarioService;
import com.fideburguesas.pos.model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

public class UsuarioManagementPanel extends JPanel {
    private UsuarioService usuarioService;
    private JTable usuariosTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, refreshButton;
    
    public UsuarioManagementPanel() {
        this.usuarioService = new UsuarioService();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadUsuarios();
    }
    
    private void initializeComponents() {
        // Tabla de usuarios
        String[] columnNames = {"ID", "Username", "Nombre", "Apellido", "Email", "Tipo", "Activo"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla no editable
            }
        };
        usuariosTable = new JTable(tableModel);
        usuariosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usuariosTable.setRowHeight(25);

        // Botones
        addButton = new JButton("Agregar Usuario");
        editButton = new JButton("Editar Usuario");
        deleteButton = new JButton("Eliminar Usuario");
        refreshButton = new JButton("Actualizar");

        // NUEVOS ESTILOS MÁS VISIBLES:

        // Botón Agregar - Verde suave
        addButton.setBackground(new Color(144, 238, 144)); // Light green
        addButton.setForeground(new Color(0, 100, 0)); // Dark green text
        addButton.setBorder(BorderFactory.createRaisedBevelBorder());
        addButton.setFocusPainted(false);

        // Botón Editar - Azul suave
        editButton.setBackground(new Color(173, 216, 230)); // Light blue
        editButton.setForeground(new Color(0, 0, 139)); // Dark blue text
        editButton.setBorder(BorderFactory.createRaisedBevelBorder());
        editButton.setFocusPainted(false);

        // Botón Eliminar - Rojo suave
        deleteButton.setBackground(new Color(255, 182, 193)); // Light pink
        deleteButton.setForeground(new Color(139, 0, 0)); // Dark red text
        deleteButton.setBorder(BorderFactory.createRaisedBevelBorder());
        deleteButton.setFocusPainted(false);

        // Botón Actualizar - Gris suave
        refreshButton.setBackground(new Color(211, 211, 211)); // Light gray
        refreshButton.setForeground(new Color(105, 105, 105)); // Dark gray text
        refreshButton.setBorder(BorderFactory.createRaisedBevelBorder());
        refreshButton.setFocusPainted(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel superior con título
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("Gestión de Usuarios");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titlePanel.add(titleLabel);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        
        // Tabla con scroll
        JScrollPane scrollPane = new JScrollPane(usuariosTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Usuarios"));
        
        add(titlePanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventListeners() {
        addButton.addActionListener(e -> showAddUserDialog());
        editButton.addActionListener(e -> showEditUserDialog());
        deleteButton.addActionListener(e -> deleteSelectedUser());
        refreshButton.addActionListener(e -> loadUsuarios());
    }
    
    private void loadUsuarios() {
        try {
            List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
            tableModel.setRowCount(0); // Limpiar tabla
            
            for (Usuario usuario : usuarios) {
                Object[] row = {
                    usuario.getId(),
                    usuario.getUsername(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.getEmail(),
                    usuario.getTipoUsuario().toString(),
                    usuario.isActivo() ? "Sí" : "No"
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar usuarios: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showAddUserDialog() {
        UsuarioDialog dialog = new UsuarioDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                "Agregar Usuario", null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            try {
                Usuario usuario = dialog.getUsuario();
                usuarioService.guardarUsuario(usuario);
                loadUsuarios();
                JOptionPane.showMessageDialog(this, "Usuario creado exitosamente");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error al crear usuario: " + e.getMessage(),
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showEditUserDialog() {
        int selectedRow = usuariosTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario para editar");
            return;
        }
        
        Long userId = (Long) tableModel.getValueAt(selectedRow, 0);
        try {
            Usuario usuario = usuarioService.buscarPorId(userId).orElse(null);
            if (usuario != null) {
                UsuarioDialog dialog = new UsuarioDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                        "Editar Usuario", usuario);
                dialog.setVisible(true);
                
                if (dialog.isConfirmed()) {
                    Usuario usuarioModificado = dialog.getUsuario();
                    usuarioModificado.setId(userId);
                    usuarioService.guardarUsuario(usuarioModificado);
                    loadUsuarios();
                    JOptionPane.showMessageDialog(this, "Usuario actualizado exitosamente");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al editar usuario: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteSelectedUser() {
        int selectedRow = usuariosTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario para eliminar");
            return;
        }
        
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        if ("admin".equals(username)) {
            JOptionPane.showMessageDialog(this, "No se puede eliminar el usuario administrador");
            return;
        }
        
        int option = JOptionPane.showConfirmDialog(this,
            "¿Está seguro que desea eliminar este usuario?",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            try {
                Long userId = (Long) tableModel.getValueAt(selectedRow, 0);
                usuarioService.eliminarUsuario(userId);
                loadUsuarios();
                JOptionPane.showMessageDialog(this, "Usuario eliminado exitosamente");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error al eliminar usuario: " + e.getMessage(),
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}