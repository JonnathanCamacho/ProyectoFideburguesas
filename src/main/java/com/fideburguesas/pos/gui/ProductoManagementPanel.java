package com.fideburguesas.pos.gui;

import com.fideburguesas.pos.service.ProductoService;
import com.fideburguesas.pos.service.CategoriaService;
import com.fideburguesas.pos.model.Producto;
import com.fideburguesas.pos.model.Categoria;
import com.fideburguesas.pos.util.CurrencyUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

public class ProductoManagementPanel extends JPanel {
    private ProductoService productoService;
    private CategoriaService categoriaService;
    private JTable productosTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    
    // Botones principales
    private JButton addButton, editButton, deleteButton, refreshButton;
    private JButton stockButton, reportButton, exportButton;
    
    // Filtros y búsqueda
    private JTextField searchField;
    private JComboBox<Categoria> categoriaFilter;
    private JCheckBox stockBajoFilter, activosFilter;
    
    // Información y estadísticas
    private JLabel totalProductosLabel, stockBajoLabel, valorInventarioLabel;
    
    public ProductoManagementPanel() {
        this.productoService = new ProductoService();
        this.categoriaService = new CategoriaService();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadCategorias();
        loadProductos();
        updateStatistics();
    }
    
    private void initializeComponents() {
        // Tabla de productos
        String[] columnNames = {
            "ID", "Código", "Nombre", "Categoría", "Precio", 
            "Stock", "Stock Mín", "Estado", "Imagen"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0: return Long.class;    // ID
                    case 4: return String.class;  // Precio (formateado)
                    case 5: case 6: return Integer.class; // Stock
                    default: return String.class;
                }
            }
        };
        
        productosTable = new JTable(tableModel);
        productosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productosTable.setRowHeight(30);
        productosTable.getTableHeader().setBackground(new Color(74, 144, 226));
        productosTable.getTableHeader().setForeground(Color.BLACK);
        productosTable.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Configurar anchos de columnas
        productosTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        productosTable.getColumnModel().getColumn(1).setPreferredWidth(100);  // Código
        productosTable.getColumnModel().getColumn(2).setPreferredWidth(200);  // Nombre
        productosTable.getColumnModel().getColumn(3).setPreferredWidth(120);  // Categoría
        productosTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // Precio
        productosTable.getColumnModel().getColumn(5).setPreferredWidth(70);   // Stock
        productosTable.getColumnModel().getColumn(6).setPreferredWidth(80);   // Stock Mín
        productosTable.getColumnModel().getColumn(7).setPreferredWidth(80);   // Estado
        productosTable.getColumnModel().getColumn(8).setPreferredWidth(80);   // Imagen
        
        // Row sorter para filtros
        rowSorter = new TableRowSorter<>(tableModel);
        productosTable.setRowSorter(rowSorter);
        
        // Botones principales
        addButton = createStyledButton("➕ Agregar Producto", new Color(144, 238, 144), new Color(0, 100, 0));
        editButton = createStyledButton("✏️ Editar Producto", new Color(173, 216, 230), new Color(0, 0, 139));
        deleteButton = createStyledButton("🗑️ Eliminar Producto", new Color(255, 182, 193), new Color(139, 0, 0));
        refreshButton = createStyledButton("🔄 Actualizar", new Color(211, 211, 211), new Color(105, 105, 105));
        
        // Botones adicionales
        stockButton = createStyledButton("📦 Gestionar Stock", new Color(255, 215, 0), new Color(184, 134, 11));
        reportButton = createStyledButton("📊 Reportes", new Color(221, 160, 221), new Color(139, 69, 19));
        exportButton = createStyledButton("📤 Exportar", new Color(176, 196, 222), new Color(25, 25, 112));
        
        // Filtros y búsqueda
        searchField = new JTextField(20);
        searchField.setToolTipText("Buscar por código, nombre o descripción");
        
        categoriaFilter = new JComboBox<>();
        categoriaFilter.setToolTipText("Filtrar por categoría");
        
        stockBajoFilter = new JCheckBox("Solo stock bajo");
        activosFilter = new JCheckBox("Solo activos", true);
        
        // Labels de estadísticas
        totalProductosLabel = new JLabel("Total: 0");
        stockBajoLabel = new JLabel("Stock bajo: 0");
        valorInventarioLabel = new JLabel("Valor inventario: ₡0.00");
        
        // Estilos para labels de estadísticas
        Font statsFont = new Font("Arial", Font.BOLD, 12);
        totalProductosLabel.setFont(statsFont);
        stockBajoLabel.setFont(statsFont);
        stockBajoLabel.setForeground(Color.RED);
        valorInventarioLabel.setFont(statsFont);
        valorInventarioLabel.setForeground(new Color(0, 128, 0));
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        return button;
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Panel superior - Título y estadísticas
        JPanel topPanel = createTopPanel();
        
        // Panel de filtros
        JPanel filterPanel = createFilterPanel();
        
        // Panel de botones
        JPanel buttonPanel = createButtonPanel();
        
        // Panel central - Tabla
        JPanel centerPanel = createCenterPanel();
        
        // Panel inferior - Información adicional
        JPanel bottomPanel = createBottomPanel();
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Panel lateral izquierdo con filtros y botones
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setPreferredSize(new Dimension(250, 0));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Filtros y Acciones"));
        
        leftPanel.add(filterPanel, BorderLayout.NORTH);
        leftPanel.add(buttonPanel, BorderLayout.CENTER);
        
        add(leftPanel, BorderLayout.WEST);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Título
        JLabel titleLabel = new JLabel("🛍️ Gestión de Productos");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(74, 144, 226));
        
        // Panel de estadísticas
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.add(totalProductosLabel);
        statsPanel.add(new JLabel(" | "));
        statsPanel.add(stockBajoLabel);
        statsPanel.add(new JLabel(" | "));
        statsPanel.add(valorInventarioLabel);
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(statsPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Filtros"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Búsqueda
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Buscar:"), gbc);
        gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(searchField, gbc);
        
        // Categoría
        gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Categoría:"), gbc);
        gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(categoriaFilter, gbc);
        
        // Checkboxes
        gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        panel.add(stockBajoFilter, gbc);
        gbc.gridy = 5;
        panel.add(activosFilter, gbc);
        
        // Botón limpiar filtros
        JButton clearFiltersButton = new JButton("🧹 Limpiar");
        clearFiltersButton.setFont(new Font("Arial", Font.PLAIN, 10));
        clearFiltersButton.setForeground(Color.BLACK);
        clearFiltersButton.addActionListener(e -> clearFilters());
        gbc.gridy = 6; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(clearFiltersButton, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Acciones"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Botones principales
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(addButton, gbc);
        gbc.gridy = 1;
        panel.add(editButton, gbc);
        gbc.gridy = 2;
        panel.add(deleteButton, gbc);
        gbc.gridy = 3;
        panel.add(refreshButton, gbc);
        
        // Separador
        gbc.gridy = 4;
        panel.add(new JSeparator(), gbc);
        
        // Botones adicionales
        gbc.gridy = 5;
        panel.add(stockButton, gbc);
        gbc.gridy = 6;
        panel.add(reportButton, gbc);
        gbc.gridy = 7;
        panel.add(exportButton, gbc);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Tabla con scroll
        JScrollPane scrollPane = new JScrollPane(productosTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Productos"));
        scrollPane.setBackground(Color.WHITE);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createTitledBorder("Información"));
        
        JLabel helpLabel = new JLabel("💡 Doble clic en una fila para editar | Click derecho para más opciones");
        helpLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        helpLabel.setForeground(Color.GRAY);
        
        panel.add(helpLabel);
        
        return panel;
    }
    
    private void setupEventListeners() {
        // Botones principales
        addButton.addActionListener(e -> showAddProductoDialog());
        editButton.addActionListener(e -> showEditProductoDialog());
        deleteButton.addActionListener(e -> deleteSelectedProducto());
        refreshButton.addActionListener(e -> { loadProductos(); updateStatistics(); });
        
        // Botones adicionales
        stockButton.addActionListener(e -> showStockManagementDialog());
        reportButton.addActionListener(e -> showReportsDialog());
        exportButton.addActionListener(e -> exportProductos());
        
        // Filtros
        searchField.addActionListener(e -> applyFilters());
        categoriaFilter.addActionListener(e -> applyFilters());
        stockBajoFilter.addActionListener(e -> applyFilters());
        activosFilter.addActionListener(e -> applyFilters());
        
        // Doble clic en tabla para editar
        productosTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    showEditProductoDialog();
                }
            }
        });
        
        // Buscar mientras escribe
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
        });
    }
    
    private void loadCategorias() {
        try {
            List<Categoria> categorias = categoriaService.obtenerCategoriasActivas();
            
            categoriaFilter.removeAllItems();
            categoriaFilter.addItem(new Categoria(-1L, "Todas las categorías", ""));
            
            for (Categoria categoria : categorias) {
                categoriaFilter.addItem(categoria);
            }
        } catch (SQLException e) {
            showError("Error al cargar categorías: " + e.getMessage());
        }
    }
    
    private void loadProductos() {
        try {
            List<Producto> productos = productoService.obtenerTodosLosProductos();
            tableModel.setRowCount(0);
            
            for (Producto producto : productos) {
                Object[] row = {
                    producto.getId(),
                    producto.getCodigo(),
                    producto.getNombre(),
                    producto.getCategoria() != null ? producto.getCategoria() : "Sin categoría",
                    CurrencyUtil.formatCurrency(producto.getPrecio()),
                    producto.getStock(),
                    producto.getStockMinimo(),
                    producto.isActivo() ? "✅ Activo" : "❌ Inactivo",
                    producto.getImagen() != null ? "📷" : "🚫"
                };
                tableModel.addRow(row);
                
                // Resaltar productos con stock bajo
                if (producto.necesitaReposicion()) {
                    int rowIndex = tableModel.getRowCount() - 1;
                    // Aquí podrías agregar coloreado de filas si quisieras
                }
            }
            
        } catch (SQLException e) {
            showError("Error al cargar productos: " + e.getMessage());
        }
    }
    
    private void updateStatistics() {
        try {
            List<Producto> todosProductos = productoService.obtenerTodosLosProductos();
            List<Producto> stockBajo = productoService.obtenerProductosBajoStock();
            
            int totalProductos = todosProductos.size();
            int productosStockBajo = stockBajo.size();
            
            // Calcular valor del inventario
            double valorTotal = todosProductos.stream()
                .filter(Producto::isActivo)
                .mapToDouble(p -> p.getPrecio().doubleValue() * p.getStock())
                .sum();
            
            totalProductosLabel.setText("Total: " + totalProductos);
            stockBajoLabel.setText("Stock bajo: " + productosStockBajo);
            valorInventarioLabel.setText("Valor inventario: " + CurrencyUtil.formatCurrency(valorTotal));
            
        } catch (SQLException e) {
            showError("Error al calcular estadísticas: " + e.getMessage());
        }
    }
    
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        Categoria selectedCategoria = (Categoria) categoriaFilter.getSelectedItem();
        boolean soloStockBajo = stockBajoFilter.isSelected();
        boolean soloActivos = activosFilter.isSelected();
        
        rowSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                int row = entry.getIdentifier();
                
                // Filtro de búsqueda
                if (!searchText.isEmpty()) {
                    String codigo = entry.getStringValue(1).toLowerCase();
                    String nombre = entry.getStringValue(2).toLowerCase();
                    
                    if (!codigo.contains(searchText) && !nombre.contains(searchText)) {
                        return false;
                    }
                }
                
                // Filtro de categoría
                if (selectedCategoria != null && selectedCategoria.getId() != -1L) {
                    String categoria = entry.getStringValue(3);
                    if (!categoria.equals(selectedCategoria.getNombre())) {
                        return false;
                    }
                }
                
                // Filtro de stock bajo
                if (soloStockBajo) {
                    Integer stock = (Integer) entry.getValue(5);
                    Integer stockMin = (Integer) entry.getValue(6);
                    if (stock > stockMin) {
                        return false;
                    }
                }
                
                // Filtro de activos
                if (soloActivos) {
                    String estado = entry.getStringValue(7);
                    if (!estado.contains("Activo")) {
                        return false;
                    }
                }
                
                return true;
            }
        });
    }
    
    private void clearFilters() {
        searchField.setText("");
        categoriaFilter.setSelectedIndex(0);
        stockBajoFilter.setSelected(false);
        activosFilter.setSelected(true);
        applyFilters();
    }
    
    private void showAddProductoDialog() {
        ProductoDialog dialog = new ProductoDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), 
            "Agregar Producto", 
            null
        );
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            try {
                Producto producto = dialog.getProducto();
                productoService.guardarProducto(producto);
                loadProductos();
                updateStatistics();
                showSuccess("Producto creado exitosamente");
            } catch (Exception e) {
                showError("Error al crear producto: " + e.getMessage());
            }
        }
    }
    
    private void showEditProductoDialog() {
        int selectedRow = productosTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Seleccione un producto para editar");
            return;
        }
        
        // Convertir índice de vista a modelo (por los filtros)
        int modelRow = productosTable.convertRowIndexToModel(selectedRow);
        Long productoId = (Long) tableModel.getValueAt(modelRow, 0);
        
        try {
            var producto = productoService.buscarPorId(productoId);
            if (producto.isPresent()) {
                ProductoDialog dialog = new ProductoDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this), 
                    "Editar Producto", 
                    producto.get()
                );
                dialog.setVisible(true);
                
                if (dialog.isConfirmed()) {
                    Producto productoModificado = dialog.getProducto();
                    productoModificado.setId(productoId);
                    productoService.guardarProducto(productoModificado);
                    loadProductos();
                    updateStatistics();
                    showSuccess("Producto actualizado exitosamente");
                }
            }
        } catch (SQLException e) {
            showError("Error al editar producto: " + e.getMessage());
        }
    }
    
    private void deleteSelectedProducto() {
        int selectedRow = productosTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Seleccione un producto para eliminar");
            return;
        }
        
        int modelRow = productosTable.convertRowIndexToModel(selectedRow);
        String nombreProducto = (String) tableModel.getValueAt(modelRow, 2);
        
        int option = JOptionPane.showConfirmDialog(this,
            "¿Está seguro que desea eliminar el producto?\n\n" +
            "Producto: " + nombreProducto + "\n" +
            "Nota: Se marcará como inactivo, no se eliminará permanentemente.",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            try {
                Long productoId = (Long) tableModel.getValueAt(modelRow, 0);
                productoService.eliminarProducto(productoId);
                loadProductos();
                updateStatistics();
                showSuccess("Producto eliminado exitosamente");
            } catch (SQLException e) {
                showError("Error al eliminar producto: " + e.getMessage());
            }
        }
    }
    
    private void showStockManagementDialog() {
        JOptionPane.showMessageDialog(this, 
            "🚧 Gestión de Stock\n\nFuncionalidad en desarrollo...\n\n" +
            "Próximamente incluirá:\n" +
            "• Ajustes de inventario\n" +
            "• Movimientos de stock\n" +
            "• Alertas automáticas\n" +
            "• Histórico de cambios",
            "Gestión de Stock",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showReportsDialog() {
        JOptionPane.showMessageDialog(this, 
            "📊 Reportes de Productos\n\nFuncionalidad en desarrollo...\n\n" +
            "Próximamente incluirá:\n" +
            "• Reporte de inventario\n" +
            "• Productos más vendidos\n" +
            "• Análisis de rentabilidad\n" +
            "• Productos con stock bajo",
            "Reportes",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exportProductos() {
        JOptionPane.showMessageDialog(this, 
            "📤 Exportar Productos\n\nFuncionalidad en desarrollo...\n\n" +
            "Próximamente incluirá:\n" +
            "• Exportar a Excel\n" +
            "• Exportar a PDF\n" +
            "• Exportar a CSV\n" +
            "• Plantillas personalizadas",
            "Exportar",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }
}