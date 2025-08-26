package com.fideburguesas.pos.gui;

import com.fideburguesas.pos.service.ComboService;
import com.fideburguesas.pos.service.ProductoService;
import com.fideburguesas.pos.model.Combo;
import com.fideburguesas.pos.model.ComboDetalle;
import com.fideburguesas.pos.util.CurrencyUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

public class ComboManagementPanel extends JPanel {
    private ComboService comboService;
    private ProductoService productoService;
    private JTable combosTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    
    // Botones principales
    private JButton addButton, editButton, deleteButton, refreshButton;
    private JButton verDetallesButton, duplicarButton, reportButton;
    
    // Filtros y búsqueda
    private JTextField searchField;
    private JCheckBox activosFilter, conDescuentoFilter;
    
    // Información y estadísticas
    private JLabel totalCombosLabel, activosLabel, ahorroPromedioLabel;
    
    public ComboManagementPanel() {
        this.comboService = new ComboService();
        this.productoService = new ProductoService();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadCombos();
        updateStatistics();
    }
    
    private void initializeComponents() {
        // Tabla de combos
        String[] columnNames = {
            "ID", "Código", "Nombre", "Descripción", "Precio", 
            "Descuento", "Precio Final", "Productos", "Estado"
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
                    case 4: case 5: case 6: return String.class;  // Precios (formateados)
                    case 7: return Integer.class; // Cantidad productos
                    default: return String.class;
                }
            }
        };
        
        combosTable = new JTable(tableModel);
        combosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        combosTable.setRowHeight(35);
        combosTable.getTableHeader().setBackground(new Color(139, 69, 19)); // Color marrón para combos
        combosTable.getTableHeader().setForeground(Color.BLACK);
        combosTable.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Configurar anchos de columnas
        combosTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        combosTable.getColumnModel().getColumn(1).setPreferredWidth(100);  // Código
        combosTable.getColumnModel().getColumn(2).setPreferredWidth(180);  // Nombre
        combosTable.getColumnModel().getColumn(3).setPreferredWidth(200);  // Descripción
        combosTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // Precio
        combosTable.getColumnModel().getColumn(5).setPreferredWidth(100);  // Descuento
        combosTable.getColumnModel().getColumn(6).setPreferredWidth(120);  // Precio Final
        combosTable.getColumnModel().getColumn(7).setPreferredWidth(80);   // Productos
        combosTable.getColumnModel().getColumn(8).setPreferredWidth(80);   // Estado
        
        // Row sorter para filtros
        rowSorter = new TableRowSorter<>(tableModel);
        combosTable.setRowSorter(rowSorter);
        
        // Botones principales
        addButton = createStyledButton("🍔 Agregar Combo", new Color(210, 180, 140), new Color(139, 69, 19));
        editButton = createStyledButton("✏️ Editar Combo", new Color(210, 180, 140), new Color(139, 69, 19));
        deleteButton = createStyledButton("🗑️ Eliminar Combo", new Color(255, 182, 193), new Color(139, 0, 0));
        refreshButton = createStyledButton("🔄 Actualizar", new Color(211, 211, 211), new Color(105, 105, 105));
        
        // Botones adicionales
        verDetallesButton = createStyledButton("🔍 Ver Detalles", new Color(173, 216, 230), new Color(0, 0, 139));
        duplicarButton = createStyledButton("📋 Duplicar", new Color(255, 228, 196), new Color(139, 69, 19));
        reportButton = createStyledButton("📊 Reportes", new Color(221, 160, 221), new Color(139, 69, 19));
        
        // Filtros y búsqueda
        searchField = new JTextField(20);
        searchField.setToolTipText("Buscar por código, nombre o descripción");
        
        activosFilter = new JCheckBox("Solo activos", true);
        conDescuentoFilter = new JCheckBox("Con descuento");
        
        // Labels de estadísticas
        totalCombosLabel = new JLabel("Total: 0");
        activosLabel = new JLabel("Activos: 0");
        ahorroPromedioLabel = new JLabel("Ahorro promedio: ₡0.00");
        
        // Estilos para labels de estadísticas
        Font statsFont = new Font("Arial", Font.BOLD, 12);
        totalCombosLabel.setFont(statsFont);
        activosLabel.setFont(statsFont);
        activosLabel.setForeground(new Color(0, 128, 0));
        ahorroPromedioLabel.setFont(statsFont);
        ahorroPromedioLabel.setForeground(new Color(139, 69, 19));
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
        JLabel titleLabel = new JLabel("🍔 Gestión de Combos");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(139, 69, 19));
        
        // Panel de estadísticas
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.add(totalCombosLabel);
        statsPanel.add(new JLabel(" | "));
        statsPanel.add(activosLabel);
        statsPanel.add(new JLabel(" | "));
        statsPanel.add(ahorroPromedioLabel);
        
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
        
        // Checkboxes
        gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(activosFilter, gbc);
        gbc.gridy = 3;
        panel.add(conDescuentoFilter, gbc);
        
        // Botón limpiar filtros
        JButton clearFiltersButton = new JButton("🧹 Limpiar");
        clearFiltersButton.setFont(new Font("Arial", Font.PLAIN, 10));
        clearFiltersButton.setForeground(Color.BLACK);
        clearFiltersButton.addActionListener(e -> clearFilters());
        gbc.gridy = 4; gbc.fill = GridBagConstraints.HORIZONTAL;
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
        panel.add(verDetallesButton, gbc);
        gbc.gridy = 6;
        panel.add(duplicarButton, gbc);
        gbc.gridy = 7;
        panel.add(reportButton, gbc);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Tabla con scroll
        JScrollPane scrollPane = new JScrollPane(combosTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Combos"));
        scrollPane.setBackground(Color.WHITE);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createTitledBorder("Información"));
        
        JLabel helpLabel = new JLabel("💡 Doble clic para editar | Los combos permiten agrupar productos con descuentos especiales");
        helpLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        helpLabel.setForeground(Color.GRAY);
        
        panel.add(helpLabel);
        
        return panel;
    }
    
    private void setupEventListeners() {
        // Botones principales
        addButton.addActionListener(e -> showAddComboDialog());
        editButton.addActionListener(e -> showEditComboDialog());
        deleteButton.addActionListener(e -> deleteSelectedCombo());
        refreshButton.addActionListener(e -> { loadCombos(); updateStatistics(); });
        
        // Botones adicionales
        verDetallesButton.addActionListener(e -> showComboDetails());
        duplicarButton.addActionListener(e -> duplicateCombo());
        reportButton.addActionListener(e -> showReportsDialog());
        
        // Filtros
        searchField.addActionListener(e -> applyFilters());
        activosFilter.addActionListener(e -> applyFilters());
        conDescuentoFilter.addActionListener(e -> applyFilters());
        
        // Doble clic en tabla para editar
        combosTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    showEditComboDialog();
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
    
    private void loadCombos() {
        try {
            List<Combo> combos = comboService.obtenerTodosLosCombos();
            tableModel.setRowCount(0);
            
            for (Combo combo : combos) {
                Object[] row = {
                    combo.getId(),
                    combo.getCodigo(),
                    combo.getNombre(),
                    combo.getDescripcion(),
                    CurrencyUtil.formatCurrency(combo.getPrecio()),
                    CurrencyUtil.formatCurrency(combo.getDescuento()),
                    CurrencyUtil.formatCurrency(combo.getPrecioConDescuento()),
                    combo.getProductos().size(),
                    combo.isActivo() ? "✅ Activo" : "❌ Inactivo"
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            showError("Error al cargar combos: " + e.getMessage());
        }
    }
    
    private void updateStatistics() {
        try {
            List<Combo> todosCombos = comboService.obtenerTodosLosCombos();
            
            int totalCombos = todosCombos.size();
            int combosActivos = (int) todosCombos.stream().filter(Combo::isActivo).count();
            
            // Calcular ahorro promedio
            double ahorroPromedio = todosCombos.stream()
                .filter(Combo::isActivo)
                .mapToDouble(c -> c.getDescuento().doubleValue())
                .average()
                .orElse(0.0);
            
            totalCombosLabel.setText("Total: " + totalCombos);
            activosLabel.setText("Activos: " + combosActivos);
            ahorroPromedioLabel.setText("Ahorro promedio: " + CurrencyUtil.formatCurrency(ahorroPromedio));
            
        } catch (SQLException e) {
            showError("Error al calcular estadísticas: " + e.getMessage());
        }
    }
    
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        boolean soloActivos = activosFilter.isSelected();
        boolean conDescuento = conDescuentoFilter.isSelected();
        
        rowSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                // Filtro de búsqueda
                if (!searchText.isEmpty()) {
                    String codigo = entry.getStringValue(1).toLowerCase();
                    String nombre = entry.getStringValue(2).toLowerCase();
                    String descripcion = entry.getStringValue(3).toLowerCase();
                    
                    if (!codigo.contains(searchText) && 
                        !nombre.contains(searchText) && 
                        !descripcion.contains(searchText)) {
                        return false;
                    }
                }
                
                // Filtro de activos
                if (soloActivos) {
                    String estado = entry.getStringValue(8);
                    if (!estado.contains("Activo")) {
                        return false;
                    }
                }
                
                // Filtro de con descuento
                if (conDescuento) {
                    String descuento = entry.getStringValue(5);
                    if (descuento.equals("₡0.00")) {
                        return false;
                    }
                }
                
                return true;
            }
        });
    }
    
    private void clearFilters() {
        searchField.setText("");
        activosFilter.setSelected(true);
        conDescuentoFilter.setSelected(false);
        applyFilters();
    }
    
    private void showAddComboDialog() {
        ComboDialog dialog = new ComboDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), 
            "Agregar Combo", 
            null
        );
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            try {
                Combo combo = dialog.getCombo();
                comboService.guardarCombo(combo);
                loadCombos();
                updateStatistics();
                showSuccess("Combo creado exitosamente");
            } catch (Exception e) {
                showError("Error al crear combo: " + e.getMessage());
            }
        }
    }
    
    private void showEditComboDialog() {
        int selectedRow = combosTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Seleccione un combo para editar");
            return;
        }
        
        int modelRow = combosTable.convertRowIndexToModel(selectedRow);
        Long comboId = (Long) tableModel.getValueAt(modelRow, 0);
        
        try {
            var combo = comboService.buscarPorId(comboId);
            if (combo.isPresent()) {
                ComboDialog dialog = new ComboDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this), 
                    "Editar Combo", 
                    combo.get()
                );
                dialog.setVisible(true);
                
                if (dialog.isConfirmed()) {
                    Combo comboModificado = dialog.getCombo();
                    comboModificado.setId(comboId);
                    comboService.guardarCombo(comboModificado);
                    loadCombos();
                    updateStatistics();
                    showSuccess("Combo actualizado exitosamente");
                }
            }
        } catch (SQLException e) {
            showError("Error al editar combo: " + e.getMessage());
        }
    }
    
    private void deleteSelectedCombo() {
        int selectedRow = combosTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Seleccione un combo para eliminar");
            return;
        }
        
        int modelRow = combosTable.convertRowIndexToModel(selectedRow);
        String nombreCombo = (String) tableModel.getValueAt(modelRow, 2);
        
        int option = JOptionPane.showConfirmDialog(this,
            "¿Está seguro que desea eliminar el combo?\n\n" +
            "Combo: " + nombreCombo + "\n" +
            "Nota: Se marcará como inactivo, no se eliminará permanentemente.",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            try {
                Long comboId = (Long) tableModel.getValueAt(modelRow, 0);
                comboService.eliminarCombo(comboId);
                loadCombos();
                updateStatistics();
                showSuccess("Combo eliminado exitosamente");
            } catch (SQLException e) {
                showError("Error al eliminar combo: " + e.getMessage());
            }
        }
    }
    
    private void showComboDetails() {
        int selectedRow = combosTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Seleccione un combo para ver detalles");
            return;
        }
        
        int modelRow = combosTable.convertRowIndexToModel(selectedRow);
        Long comboId = (Long) tableModel.getValueAt(modelRow, 0);
        
        try {
            var combo = comboService.buscarPorId(comboId);
            if (combo.isPresent()) {
                showComboDetailsDialog(combo.get());
            }
        } catch (SQLException e) {
            showError("Error al cargar detalles: " + e.getMessage());
        }
    }
    
    private void showComboDetailsDialog(Combo combo) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                   "Detalles del Combo: " + combo.getNombre(), true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Información del combo
        JPanel infoPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Información General"));
        
        infoPanel.add(new JLabel("Código:"));
        infoPanel.add(new JLabel(combo.getCodigo()));
        infoPanel.add(new JLabel("Nombre:"));
        infoPanel.add(new JLabel(combo.getNombre()));
        infoPanel.add(new JLabel("Precio Original:"));
        infoPanel.add(new JLabel(CurrencyUtil.formatCurrency(combo.getPrecio())));
        infoPanel.add(new JLabel("Descuento:"));
        infoPanel.add(new JLabel(CurrencyUtil.formatCurrency(combo.getDescuento())));
        infoPanel.add(new JLabel("Precio Final:"));
        infoPanel.add(new JLabel(CurrencyUtil.formatCurrency(combo.getPrecioConDescuento())));
        infoPanel.add(new JLabel("Estado:"));
        infoPanel.add(new JLabel(combo.isActivo() ? "✅ Activo" : "❌ Inactivo"));
        
        // Tabla de productos
        String[] columnNames = {"Producto", "Cantidad"};
        Object[][] data = new Object[combo.getProductos().size()][2];
        
        for (int i = 0; i < combo.getProductos().size(); i++) {
            ComboDetalle detalle = combo.getProductos().get(i);
            data[i][0] = detalle.getProductoNombre();
            data[i][1] = detalle.getCantidad();
        }
        
        JTable productosTable = new JTable(data, columnNames);
        productosTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(productosTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Productos del Combo"));
        
        JButton cerrarButton = new JButton("Cerrar");
        cerrarButton.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(cerrarButton);
        
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void duplicateCombo() {
        int selectedRow = combosTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Seleccione un combo para duplicar");
            return;
        }
        
        int modelRow = combosTable.convertRowIndexToModel(selectedRow);
        Long comboId = (Long) tableModel.getValueAt(modelRow, 0);
        
        try {
            var combo = comboService.buscarPorId(comboId);
            if (combo.isPresent()) {
                Combo original = combo.get();
                Combo duplicado = new Combo();
                duplicado.setCodigo(original.getCodigo() + "_COPY");
                duplicado.setNombre(original.getNombre() + " (Copia)");
                duplicado.setDescripcion(original.getDescripcion());
                duplicado.setPrecio(original.getPrecio());
                duplicado.setDescuento(original.getDescuento());
                duplicado.setProductos(original.getProductos());
                
                ComboDialog dialog = new ComboDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this), 
                    "Duplicar Combo", 
                    duplicado
                );
                dialog.setVisible(true);
                
                if (dialog.isConfirmed()) {
                    Combo nuevoCombo = dialog.getCombo();
                    comboService.guardarCombo(nuevoCombo);
                    loadCombos();
                    updateStatistics();
                    showSuccess("Combo duplicado exitosamente");
                }
            }
        } catch (SQLException e) {
            showError("Error al duplicar combo: " + e.getMessage());
        }
    }
    
    private void showReportsDialog() {
        JOptionPane.showMessageDialog(this, 
            "📊 Reportes de Combos\n\nFuncionalidad en desarrollo...\n\n" +
            "Próximamente incluirá:\n" +
            "• Combos más vendidos\n" +
            "• Análisis de descuentos\n" +
            "• Rentabilidad por combo\n" +
            "• Comparativa de ventas",
            "Reportes",
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