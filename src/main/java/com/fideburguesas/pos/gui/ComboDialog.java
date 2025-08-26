package com.fideburguesas.pos.gui;

import com.fideburguesas.pos.model.Combo;
import com.fideburguesas.pos.model.ComboDetalle;
import com.fideburguesas.pos.model.Producto;
import com.fideburguesas.pos.service.ProductoService;
import com.fideburguesas.pos.util.ValidationUtil;
import com.fideburguesas.pos.util.CurrencyUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ComboDialog extends JDialog {
    private JTextField codigoField, nombreField, descripcionField, precioField, descuentoField;
    private JCheckBox activoCheckBox;
    private JTable productosTable;
    private DefaultTableModel productosTableModel;
    private JComboBox<Producto> productoComboBox;
    private JSpinner cantidadSpinner;
    private JLabel precioFinalLabel;
    
    private boolean confirmed = false;
    private Combo combo;
    private ProductoService productoService;
    private List<ComboDetalle> productosDelCombo;
    
    public ComboDialog(JFrame parent, String title, Combo combo) {
        super(parent, title, true);
        this.combo = combo;
        this.productoService = new ProductoService();
        this.productosDelCombo = new ArrayList<>();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        setDefaultConfiguration();
        loadProductos();
        
        if (combo != null) {
            loadComboData();
        }
        
        calculatePrecioFinal();
    }
    
    private void initializeComponents() {
        codigoField = new JTextField(20);
        nombreField = new JTextField(20);
        descripcionField = new JTextField(20);
        precioField = new JTextField(20);
        descuentoField = new JTextField(20);
        activoCheckBox = new JCheckBox("Activo", true);
        
        // Tabla de productos del combo
        String[] columnNames = {"Producto", "Cantidad", "Acción"};
        productosTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Solo la columna de acción
            }
        };
        productosTable = new JTable(productosTableModel);
        productosTable.setRowHeight(30);
        
        // ComboBox para agregar productos
        productoComboBox = new JComboBox<>();
        cantidadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        
        // Label para precio final
        precioFinalLabel = new JLabel("Precio Final: ₡0.00");
        precioFinalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        precioFinalLabel.setForeground(new Color(0, 128, 0));
        
        // Configurar campos por defecto
        precioField.setText("0.00");
        descuentoField.setText("0.00");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel principal con tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Tab 1: Información básica
        JPanel infoPanel = createInfoPanel();
        tabbedPane.addTab("📝 Información", infoPanel);
        
        // Tab 2: Productos del combo
        JPanel productosPanel = createProductosPanel();
        tabbedPane.addTab("🍔 Productos", productosPanel);
        
        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");
        
        saveButton.setBackground(new Color(144, 238, 144));
        saveButton.setForeground(new Color(0, 100, 0));
        saveButton.setBorder(BorderFactory.createRaisedBevelBorder());
        saveButton.setFocusPainted(false);
        
        cancelButton.setBackground(new Color(255, 182, 193));
        cancelButton.setForeground(new Color(139, 0, 0));
        cancelButton.setBorder(BorderFactory.createRaisedBevelBorder());
        cancelButton.setFocusPainted(false);
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Event listeners
        saveButton.addActionListener(e -> save());
        cancelButton.addActionListener(e -> cancel());
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Código
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Código:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(codigoField, gbc);
        
        // Nombre
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(nombreField, gbc);
        
        // Descripción
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(descripcionField, gbc);
        
        // Precio base
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Precio Base:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(precioField, gbc);
        
        // Descuento
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Descuento:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(descuentoField, gbc);
        
        // Precio final
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(precioFinalLabel, gbc);
        
        // Activo
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(activoCheckBox, gbc);
        
        return panel;
    }
    
    private JPanel createProductosPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Panel superior para agregar productos
        JPanel addPanel = new JPanel(new FlowLayout());
        addPanel.setBackground(Color.WHITE);
        addPanel.setBorder(BorderFactory.createTitledBorder("Agregar Producto"));
        
        addPanel.add(new JLabel("Producto:"));
        addPanel.add(productoComboBox);
        addPanel.add(new JLabel("Cantidad:"));
        addPanel.add(cantidadSpinner);
        
        JButton addProductButton = new JButton("➕ Agregar");
        addProductButton.setBackground(new Color(144, 238, 144));
        addProductButton.setForeground(new Color(0, 100, 0));
        addProductButton.addActionListener(e -> agregarProducto());
        addPanel.add(addProductButton);
        
        // Tabla de productos
        JScrollPane scrollPane = new JScrollPane(productosTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Productos en el Combo"));
        scrollPane.setPreferredSize(new Dimension(500, 200));
        
        // Configurar columna de acción
        productosTable.getColumn("Acción").setCellRenderer(new ButtonRenderer());
        productosTable.getColumn("Acción").setCellEditor(new ButtonEditor(new JCheckBox()));
        
        panel.add(addPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void setupEventListeners() {
        // Calcular precio final cuando cambien precio o descuento
        precioField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { calculatePrecioFinal(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { calculatePrecioFinal(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { calculatePrecioFinal(); }
        });
        
        descuentoField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { calculatePrecioFinal(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { calculatePrecioFinal(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { calculatePrecioFinal(); }
        });
    }
    
    private void loadProductos() {
        try {
            List<Producto> productos = productoService.obtenerTodosLosProductos();
            productoComboBox.removeAllItems();
            
            for (Producto producto : productos) {
                if (producto.isActivo()) {
                    productoComboBox.addItem(producto);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar productos: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadComboData() {
        codigoField.setText(combo.getCodigo());
        nombreField.setText(combo.getNombre());
        descripcionField.setText(combo.getDescripcion());
        precioField.setText(combo.getPrecio().toString());
        descuentoField.setText(combo.getDescuento().toString());
        activoCheckBox.setSelected(combo.isActivo());
        
        // Cargar productos del combo
        productosDelCombo = new ArrayList<>(combo.getProductos());
        updateProductosTable();
    }
    
    private void agregarProducto() {
        Producto selectedProducto = (Producto) productoComboBox.getSelectedItem();
        if (selectedProducto == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto");
            return;
        }
        
        int cantidad = (Integer) cantidadSpinner.getValue();
        
        // Verificar si el producto ya está en el combo
        for (ComboDetalle detalle : productosDelCombo) {
            if (detalle.getProductoId().equals(selectedProducto.getId())) {
                JOptionPane.showMessageDialog(this, "El producto ya está en el combo");
                return;
            }
        }
        
        // Agregar producto
        ComboDetalle detalle = new ComboDetalle();
        detalle.setProductoId(selectedProducto.getId());
        detalle.setProductoNombre(selectedProducto.getNombre());
        detalle.setCantidad(cantidad);
        
        productosDelCombo.add(detalle);
        updateProductosTable();
        
        // Reset spinner
        cantidadSpinner.setValue(1);
    }
    
    private void updateProductosTable() {
        productosTableModel.setRowCount(0);
        
        for (ComboDetalle detalle : productosDelCombo) {
            Object[] row = {
                detalle.getProductoNombre(),
                detalle.getCantidad(),
                "Eliminar"
            };
            productosTableModel.addRow(row);
        }
    }
    
    private void eliminarProducto(int index) {
        if (index >= 0 && index < productosDelCombo.size()) {
            productosDelCombo.remove(index);
            updateProductosTable();
        }
    }
    
    private void calculatePrecioFinal() {
        try {
            BigDecimal precio = new BigDecimal(precioField.getText().isEmpty() ? "0" : precioField.getText());
            BigDecimal descuento = new BigDecimal(descuentoField.getText().isEmpty() ? "0" : descuentoField.getText());
            BigDecimal precioFinal = precio.subtract(descuento);
            
            precioFinalLabel.setText("Precio Final: " + CurrencyUtil.formatCurrency(precioFinal));
        } catch (NumberFormatException e) {
            precioFinalLabel.setText("Precio Final: ₡0.00");
        }
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
        // Validar código
        if (!ValidationUtil.isNotEmpty(codigoField.getText())) {
            showFieldError("El código es requerido", codigoField);
            return false;
        }
        
        // Validar nombre
        if (!ValidationUtil.isNotEmpty(nombreField.getText())) {
            showFieldError("El nombre es requerido", nombreField);
            return false;
        }
        
        // Validar precio
        if (!ValidationUtil.isValidNumber(precioField.getText())) {
            showFieldError("El precio debe ser un número válido", precioField);
            return false;
        }
        
        // Validar descuento
        if (!ValidationUtil.isValidNumber(descuentoField.getText())) {
            showFieldError("El descuento debe ser un número válido", descuentoField);
            return false;
        }
        
        // Validar que hay productos
        if (productosDelCombo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El combo debe tener al menos un producto");
            return false;
        }
        
        return true;
    }
    
    private void showFieldError(String message, JTextField field) {
        JOptionPane.showMessageDialog(this, message);
        field.requestFocus();
        field.selectAll();
    }
    
    public Combo getCombo() {
        Combo c = new Combo();
        c.setCodigo(ValidationUtil.cleanString(codigoField.getText()).toUpperCase());
        c.setNombre(ValidationUtil.cleanString(nombreField.getText()));
        c.setDescripcion(ValidationUtil.cleanString(descripcionField.getText()));
        c.setPrecio(new BigDecimal(precioField.getText()));
        c.setDescuento(new BigDecimal(descuentoField.getText()));
        c.setActivo(activoCheckBox.isSelected());
        c.setProductos(new ArrayList<>(productosDelCombo));
        
        return c;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    private void setDefaultConfiguration() {
        setSize(650, 500);
        setLocationRelativeTo(getParent());
        setResizable(false);
        codigoField.requestFocus();
    }
    
    // Clases auxiliares para botones en tabla
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText("🗑️ Eliminar");
            setBackground(new Color(255, 182, 193));
            setForeground(new Color(139, 0, 0));
            return this;
        }
    }
    
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = "🗑️ Eliminar";
            button.setText(label);
            button.setBackground(new Color(255, 182, 193));
            button.setForeground(new Color(139, 0, 0));
            isPushed = true;
            return button;
        }
        
        public Object getCellEditorValue() {
            if (isPushed) {
                int row = productosTable.getSelectedRow();
                eliminarProducto(row);
            }
            isPushed = false;
            return label;
        }
        
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}