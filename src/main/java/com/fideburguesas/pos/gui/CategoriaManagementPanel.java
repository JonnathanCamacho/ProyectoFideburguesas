package com.fideburguesas.pos.gui;

import com.fideburguesas.pos.service.CategoriaService;
import com.fideburguesas.pos.model.Categoria;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

public class CategoriaManagementPanel extends JPanel {
    private CategoriaService categoriaService;
    private JTable categoriasTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, refreshButton;
    
    public CategoriaManagementPanel() {
        this.categoriaService = new CategoriaService();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadCategorias();
    }
    
    private void initializeComponents() {
        // Tabla de categorías
        String[] columnNames = {"ID", "Nombre", "Descripción", "Activo"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla no editable
            }
        };
        categoriasTable = new JTable(tableModel);
        categoriasTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoriasTable.setRowHeight(25);
        categoriasTable.getTableHeader().setBackground(new Color(74, 144, 226));
        categoriasTable.getTableHeader().setForeground(Color.BLACK);

        // Botones
        addButton = new JButton("Agregar Categoría");
        editButton = new JButton("Editar Categoría");
        deleteButton = new JButton("Eliminar Categoría");
        refreshButton = new JButton("Actualizar");

        // Estilos de botones
        addButton.setBackground(new Color(144, 238, 144)); // Light green
        addButton.setForeground(new Color(0, 100, 0)); // Dark green text
        addButton.setBorder(BorderFactory.createRaisedBevelBorder());
        addButton.setFocusPainted(false);

        editButton.setBackground(new Color(173, 216, 230)); // Light blue
        editButton.setForeground(new Color(0, 0, 139)); // Dark blue text
        editButton.setBorder(BorderFactory.createRaisedBevelBorder());
        editButton.setFocusPainted(false);

        deleteButton.setBackground(new Color(255, 182, 193)); // Light pink
        deleteButton.setForeground(new Color(139, 0, 0)); // Dark red text
        deleteButton.setBorder(BorderFactory.createRaisedBevelBorder());
        deleteButton.setFocusPainted(false);

        refreshButton.setBackground(new Color(211, 211, 211)); // Light gray
        refreshButton.setForeground(new Color(105, 105, 105)); // Dark gray text
        refreshButton.setBorder(BorderFactory.createRaisedBevelBorder());
        refreshButton.setFocusPainted(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Panel superior con título
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("Gestión de Categorías");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titlePanel.add(titleLabel);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.BLUE);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        
        // Tabla con scroll
        JScrollPane scrollPane = new JScrollPane(categoriasTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Categorías"));
        scrollPane.setBackground(Color.WHITE);
        
        add(titlePanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventListeners() {
        addButton.addActionListener(e -> showAddCategoriaDialog());
        editButton.addActionListener(e -> showEditCategoriaDialog());
        deleteButton.addActionListener(e -> deleteSelectedCategoria());
        refreshButton.addActionListener(e -> loadCategorias());
    }
    
    private void loadCategorias() {
        try {
            List<Categoria> categorias = categoriaService.obtenerTodasLasCategorias();
            tableModel.setRowCount(0); // Limpiar tabla
            
            for (Categoria categoria : categorias) {
                Object[] row = {
                    categoria.getId(),
                    categoria.getNombre(),
                    categoria.getDescripcion(),
                    categoria.isActivo() ? "Sí" : "No"
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar categorías: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showAddCategoriaDialog() {
        CategoriaDialog dialog = new CategoriaDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                    "Agregar Categoría", null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            try {
                Categoria categoria = dialog.getCategoria();
                categoriaService.guardarCategoria(categoria);
                loadCategorias();
                JOptionPane.showMessageDialog(this, "Categoría creada exitosamente");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error al crear categoría: " + e.getMessage(),
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showEditCategoriaDialog() {
        int selectedRow = categoriasTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una categoría para editar");
            return;
        }
        
        Long categoriaId = (Long) tableModel.getValueAt(selectedRow, 0);
        try {
            var categoria = categoriaService.buscarPorId(categoriaId);
            if (categoria.isPresent()) {
                CategoriaDialog dialog = new CategoriaDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                            "Editar Categoría", categoria.get());
                dialog.setVisible(true);
                
                if (dialog.isConfirmed()) {
                    Categoria categoriaModificada = dialog.getCategoria();
                    categoriaModificada.setId(categoriaId);
                    categoriaService.guardarCategoria(categoriaModificada);
                    loadCategorias();
                    JOptionPane.showMessageDialog(this, "Categoría actualizada exitosamente");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al editar categoría: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteSelectedCategoria() {
        int selectedRow = categoriasTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una categoría para eliminar");
            return;
        }
        
        int option = JOptionPane.showConfirmDialog(this,
            "¿Está seguro que desea eliminar esta categoría?\n" +
            "Nota: Se marcará como inactiva, no se eliminará permanentemente.",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            try {
                Long categoriaId = (Long) tableModel.getValueAt(selectedRow, 0);
                categoriaService.eliminarCategoria(categoriaId);
                loadCategorias();
                JOptionPane.showMessageDialog(this, "Categoría eliminada exitosamente");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error al eliminar categoría: " + e.getMessage(),
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}