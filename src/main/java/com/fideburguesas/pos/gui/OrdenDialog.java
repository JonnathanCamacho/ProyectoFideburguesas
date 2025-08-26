package com.fideburguesas.pos.gui;

import com.fideburguesas.pos.model.Orden;
import com.fideburguesas.pos.model.OrdenDetalle;
import com.fideburguesas.pos.model.Producto;
import com.fideburguesas.pos.model.Combo;
import com.fideburguesas.pos.service.ProductoService;
import com.fideburguesas.pos.service.ComboService;
import com.fideburguesas.pos.service.OrdenService;
import com.fideburguesas.pos.util.CurrencyUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrdenDialog extends JDialog {
    private JTextArea observacionesField;
    private JTable productosTable;
    private DefaultTableModel productosTableModel;
    private JComboBox<Object> itemComboBox; // Productos y combos
    private JSpinner cantidadSpinner;
    private JLabel subtotalLabel, impuestosLabel, totalLabel;
    
    private boolean confirmed = false;
    private Orden orden;
    private ProductoService productoService;
    private ComboService comboService;
    private OrdenService ordenService;
    private List<OrdenDetalle> detallesOrden;
    
    public OrdenDialog(JFrame parent, String title, Orden orden) {
        super(parent, title, true);
        this.orden = orden;
        this.productoService = new ProductoService();
        this.comboService = new ComboService();
        this.ordenService = new OrdenService();
        this.detallesOrden = new ArrayList<>();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        setDefaultConfiguration();
        loadItems();
        
        if (orden != null) {
            loadOrdenData();
        }
        
        calculateTotals();
    }
    
    private void initializeComponents() {
        observacionesField = new JTextArea(3, 30);
        observacionesField.setLineWrap(true);
        observacionesField.setWrapStyleWord(true);
        
        // Tabla de productos de la orden
        String[] columnNames = {"Producto/Combo", "Cantidad", "Precio Unit.", "Subtotal", "Acción"};
        productosTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Solo la columna de acción
            }
        };
        productosTable = new JTable(productosTableModel);
        productosTable.setRowHeight(30);
        
        // ComboBox para agregar items
        itemComboBox = new JComboBox<>();
        cantidadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        
        // Labels para totales
        subtotalLabel = new JLabel("Subtotal: ₡0.00");
        impuestosLabel = new JLabel("Impuestos (13%): ₡0.00");
        totalLabel = new JLabel("TOTAL: ₡0.00");
        
        subtotalLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        impuestosLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setForeground(new Color(0, 128, 0));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel principal con tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Tab 1: Productos
        JPanel productosPanel = createProductosPanel();
        tabbedPane.addTab("🛍️ Productos", productosPanel);
        
        // Tab 2: Información adicional
        JPanel infoPanel = createInfoPanel();
        tabbedPane.addTab("📝 Información", infoPanel);
        
        // Panel de totales
        JPanel totalesPanel = createTotalesPanel();
        
        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        
        JButton saveButton = new JButton("💾 Guardar Orden");
        JButton cancelButton = new JButton("❌ Cancelar");
        
        saveButton.setBackground(new Color(60, 179, 113));
        saveButton.setForeground(Color.BLACK);
        saveButton.setBorder(BorderFactory.createRaisedBevelBorder());
        saveButton.setFocusPainted(false);
        saveButton.setFont(new Font("Arial", Font.BOLD, 12));
        
        cancelButton.setBackground(new Color(255, 182, 193));
        cancelButton.setForeground(new Color(139, 0, 0));
        cancelButton.setBorder(BorderFactory.createRaisedBevelBorder());
        cancelButton.setFocusPainted(false);
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(tabbedPane, BorderLayout.CENTER);
        add(totalesPanel, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Event listeners
        saveButton.addActionListener(e -> save());
        cancelButton.addActionListener(e -> cancel());
    }
    
    private JPanel createProductosPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Panel superior para agregar productos
        JPanel addPanel = new JPanel(new FlowLayout());
        addPanel.setBackground(Color.WHITE);
        addPanel.setBorder(BorderFactory.createTitledBorder("Agregar Producto/Combo"));
        
        addPanel.add(new JLabel("Item:"));
        addPanel.add(itemComboBox);
        addPanel.add(new JLabel("Cantidad:"));
        addPanel.add(cantidadSpinner);
        
        JButton addItemButton = new JButton("➕ Agregar");
        addItemButton.setBackground(new Color(144, 238, 144));
        addItemButton.setForeground(new Color(0, 100, 0));
        addItemButton.setFont(new Font("Arial", Font.BOLD, 12)); // Añadir esto
        addItemButton.addActionListener(e -> {
            System.out.println("DEBUG: Botón Agregar presionado"); // Debug
            agregarItem();
        });
        addPanel.add(addItemButton);
        
        // Tabla de productos
        JScrollPane scrollPane = new JScrollPane(productosTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Productos en la Orden"));
        scrollPane.setPreferredSize(new Dimension(600, 300));
        
        // Configurar columna de acción
        productosTable.getColumn("Acción").setCellRenderer(new ButtonRenderer());
        productosTable.getColumn("Acción").setCellEditor(new ButtonEditor(new JCheckBox()));
        
        panel.add(addPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Observaciones
        JPanel obsPanel = new JPanel(new BorderLayout());
        obsPanel.setBorder(BorderFactory.createTitledBorder("Observaciones"));
        obsPanel.add(new JScrollPane(observacionesField), BorderLayout.CENTER);
        
        panel.add(obsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createTotalesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createTitledBorder("Resumen de Orden"));
        panel.setPreferredSize(new Dimension(200, 0));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(subtotalLabel, gbc);
        
        gbc.gridy = 1;
        panel.add(impuestosLabel, gbc);
        
        gbc.gridy = 2;
        panel.add(new JSeparator(), gbc);
        
        gbc.gridy = 3;
        panel.add(totalLabel, gbc);
        
        return panel;
    }
    
    private void setupEventListeners() {
        // Los event listeners se configuran en setupLayout()
    }
    
    private void loadItems() {
        try {
            itemComboBox.removeAllItems();
            
            // Cargar productos activos
            List<Producto> productos = productoService.obtenerTodosLosProductos();
            for (Producto producto : productos) {
                if (producto.isActivo()) {
                    itemComboBox.addItem(producto);
                }
            }
            
            // Cargar combos activos
            List<Combo> combos = comboService.obtenerCombosActivos();
            for (Combo combo : combos) {
                itemComboBox.addItem(combo);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar productos y combos: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadOrdenData() {
        if (orden.getObservaciones() != null) {
            observacionesField.setText(orden.getObservaciones());
        }
        
        // Cargar detalles de la orden
        if (orden.getDetalles() != null) {
            detallesOrden = new ArrayList<>(orden.getDetalles());
            updateProductosTable();
        }
    }
    
    private void agregarItem() {
        Object selectedItem = itemComboBox.getSelectedItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto o combo");
            return;
        }

        int cantidad = (Integer) cantidadSpinner.getValue();

        try {
            OrdenDetalle detalle = new OrdenDetalle();

            // PRIMERO establecer cantidad
            detalle.setCantidad(cantidad);

            if (selectedItem instanceof Producto) {
                Producto producto = (Producto) selectedItem;

                System.out.println("DEBUG: Agregando producto: " + producto.getNombre() + ", Stock: " + producto.getStock());

                // Verificar stock
                if (producto.getStock() < cantidad) {
                    JOptionPane.showMessageDialog(this, 
                        "Stock insuficiente para " + producto.getNombre() + 
                        "\nDisponible: " + producto.getStock() + 
                        "\nSolicitado: " + cantidad);
                    return;
                }

                detalle.setProductoId(producto.getId());
                detalle.setItemNombre(producto.getNombre());
                detalle.setPrecioUnitario(producto.getPrecio()); // DESPUÉS establecer precio

            } else if (selectedItem instanceof Combo) {
                Combo combo = (Combo) selectedItem;

                System.out.println("DEBUG: Agregando combo: " + combo.getNombre());

                // Verificar stock de combo
                try {
                    if (!comboService.verificarStockDisponibleCombo(combo.getId(), cantidad)) {
                        JOptionPane.showMessageDialog(this, 
                            "Stock insuficiente para el combo " + combo.getNombre());
                        return;
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error al verificar stock del combo: " + e.getMessage());
                    return;
                }

                detalle.setComboId(combo.getId());
                detalle.setItemNombre("COMBO: " + combo.getNombre());
                detalle.setPrecioUnitario(combo.getPrecioConDescuento()); // DESPUÉS establecer precio
            } else {
                JOptionPane.showMessageDialog(this, "Tipo de item no reconocido: " + selectedItem.getClass().getName());
                return;
            }

            // AHORA calcular subtotal manualmente
            detalle.calcularSubtotal();

            // Verificar si el item ya está en la orden
            for (OrdenDetalle existente : detallesOrden) {
                if ((detalle.getProductoId() != null && detalle.getProductoId().equals(existente.getProductoId())) ||
                    (detalle.getComboId() != null && detalle.getComboId().equals(existente.getComboId()))) {
                    JOptionPane.showMessageDialog(this, "El item '" + detalle.getItemNombre() + "' ya está en la orden");
                    return;
                }
            }

            System.out.println("DEBUG: Agregando detalle - " + detalle.getItemNombre() + 
                              ", Cantidad: " + detalle.getCantidad() + 
                              ", Precio: " + detalle.getPrecioUnitario() + 
                              ", Subtotal: " + detalle.getSubtotal());

            detallesOrden.add(detalle);
            updateProductosTable();
            calculateTotals();

            // Reset spinner
            cantidadSpinner.setValue(1);

            System.out.println("DEBUG: Item agregado exitosamente. Total items en orden: " + detallesOrden.size());

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error al agregar item: " + e.getMessage() + 
                "\nTipo: " + e.getClass().getSimpleName());
        }
    }
    
    private void updateProductosTable() {
        productosTableModel.setRowCount(0);
        
        for (OrdenDetalle detalle : detallesOrden) {
            Object[] row = {
                detalle.getItemNombre(),
                detalle.getCantidad(),
                CurrencyUtil.formatCurrency(detalle.getPrecioUnitario()),
                CurrencyUtil.formatCurrency(detalle.getSubtotal()),
                "Eliminar"
            };
            productosTableModel.addRow(row);
        }
    }
    
    private void eliminarItem(int index) {
        if (index >= 0 && index < detallesOrden.size()) {
            detallesOrden.remove(index);
            updateProductosTable();
            calculateTotals();
        }
    }
    
    private void calculateTotals() {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrdenDetalle detalle : detallesOrden) {
            if (detalle.getSubtotal() != null) {
                subtotal = subtotal.add(detalle.getSubtotal());
            } else {
                // Calcular si no está calculado
                detalle.calcularSubtotal();
                if (detalle.getSubtotal() != null) {
                    subtotal = subtotal.add(detalle.getSubtotal());
                }
            }
        }

        BigDecimal impuestos = subtotal.multiply(BigDecimal.valueOf(0.13)); // 13% IVA
        BigDecimal total = subtotal.add(impuestos);

        subtotalLabel.setText("Subtotal: " + CurrencyUtil.formatCurrency(subtotal));
        impuestosLabel.setText("Impuestos (13%): " + CurrencyUtil.formatCurrency(impuestos));
        totalLabel.setText("TOTAL: " + CurrencyUtil.formatCurrency(total));
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
        // Validar que hay productos
        if (detallesOrden.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La orden debe tener al menos un producto");
            return false;
        }
        
        return true;
    }
    
    public Orden getOrden() {
        try {
            Orden nuevaOrden = new Orden();
            
            if (orden == null) {
                // Nueva orden
                String numeroOrden = ordenService.generateNumeroOrden();
                nuevaOrden.setNumero(numeroOrden);
            } else {
                // Editar orden existente
                nuevaOrden.setId(orden.getId());
                nuevaOrden.setNumero(orden.getNumero());
                nuevaOrden.setFecha(orden.getFecha());
            }
            
            nuevaOrden.setObservaciones(observacionesField.getText().trim());
            nuevaOrden.setDetalles(detallesOrden);
            nuevaOrden.calcularTotales();
            
            return nuevaOrden;
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al generar número de orden: " + e.getMessage());
            return null;
        }
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    private void setDefaultConfiguration() {
        setSize(900, 600);
        setLocationRelativeTo(getParent());
        setResizable(true);
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
                eliminarItem(row);
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