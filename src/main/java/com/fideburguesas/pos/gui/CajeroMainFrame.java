package com.fideburguesas.pos.gui;

import com.fideburguesas.pos.service.*;
import com.fideburguesas.pos.model.*;
import com.fideburguesas.pos.util.CurrencyUtil;
import com.fideburguesas.pos.util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CajeroMainFrame extends JFrame {
    private UsuarioService usuarioService;
    private ProductoService productoService;
    private ComboService comboService;
    private CategoriaService categoriaService;
    private OrdenService ordenService;
    
    private JLabel userLabel;
    private JPanel productPanel;
    private JTable ordenTable;
    private DefaultTableModel ordenTableModel;
    private JLabel totalLabel;
    private JButton finalizarOrdenButton;
    private JButton cancelarOrdenButton;
    
    private Orden ordenActual;
    private List<Categoria> categorias;
    
    public CajeroMainFrame(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
        this.productoService = new ProductoService();
        this.comboService = new ComboService();
        this.categoriaService = new CategoriaService();
        this.ordenService = new OrdenService();
        
        try {
            this.categorias = categoriaService.obtenerCategoriasActivas();
            this.ordenActual = ordenService.crearOrden(usuarioService.getUsuarioActual().getId());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al inicializar: " + e.getMessage());
        }
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        setDefaultConfiguration();
        loadProductos();
    }
    
    private void initializeComponents() {
        Usuario usuario = usuarioService.getUsuarioActual();
        userLabel = new JLabel("Cajero: " + usuario.getNombreCompleto() + " | " + 
                              DateUtil.formatDateTime(java.time.LocalDateTime.now()));
        userLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Panel de productos
        productPanel = new JPanel();
        productPanel.setLayout(new BoxLayout(productPanel, BoxLayout.Y_AXIS));
        
        // Tabla de orden
        String[] columnNames = {"Producto", "Cantidad", "Precio Unit.", "Subtotal", "Acción"};
        ordenTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Solo cantidad es editable
            }
        };
        ordenTable = new JTable(ordenTableModel);
        ordenTable.setRowHeight(30);
        ordenTable.getTableHeader().setBackground(new Color(74, 144, 226));
        ordenTable.getTableHeader().setForeground(Color.BLACK);
        
        // Botón para eliminar items
        ordenTable.getColumn("Acción").setCellRenderer(new ButtonRenderer());
        ordenTable.getColumn("Acción").setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // Total
        totalLabel = new JLabel("Total: ₡0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setForeground(new Color(0, 128, 0));
        
        // Botones de acción
        finalizarOrdenButton = new JButton("Finalizar Orden");
        finalizarOrdenButton.setBackground(new Color(34, 139, 34));
        finalizarOrdenButton.setForeground(Color.BLUE);
        finalizarOrdenButton.setEnabled(false);
        
        cancelarOrdenButton = new JButton("Cancelar Orden");
        cancelarOrdenButton.setBackground(new Color(220, 20, 60));
        cancelarOrdenButton.setForeground(Color.BLUE);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel superior
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(74, 144, 226));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        userLabel.setForeground(Color.WHITE);
        topPanel.add(userLabel, BorderLayout.WEST);
        
        JButton logoutButton = new JButton("Cerrar Sesión");
        logoutButton.setBackground(Color.BLUE);
        logoutButton.setForeground(new Color(74, 144, 226));
        logoutButton.addActionListener(e -> logout());
        topPanel.add(logoutButton, BorderLayout.EAST);
        
        // Panel principal dividido
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(500);
        
        // Panel izquierdo - Productos
        JPanel leftPanel = createProductsPanel();
        mainSplitPane.setLeftComponent(leftPanel);
        
        // Panel derecho - Orden actual
        JPanel rightPanel = createOrderPanel();
        mainSplitPane.setRightComponent(rightPanel);
        
        add(topPanel, BorderLayout.NORTH);
        add(mainSplitPane, BorderLayout.CENTER);
    }
    
    private JPanel createProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Productos Disponibles"));
        
        // Panel de categorías
        JPanel categoryPanel = new JPanel(new FlowLayout());
        JButton todasButton = new JButton("Todas");
        todasButton.addActionListener(e -> loadProductos());
        categoryPanel.add(todasButton);
        
        for (Categoria categoria : categorias) {
            JButton categoryButton = new JButton(categoria.getNombre());
            categoryButton.addActionListener(e -> loadProductosPorCategoria(categoria.getId()));
            categoryPanel.add(categoryButton);
        }
        
        // Scroll para productos
        JScrollPane productScrollPane = new JScrollPane(productPanel);
        productScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        panel.add(categoryPanel, BorderLayout.NORTH);
        panel.add(productScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Orden Actual"));
        
        // Tabla de orden
        JScrollPane orderScrollPane = new JScrollPane(ordenTable);
        orderScrollPane.setPreferredSize(new Dimension(400, 300));
        
        // Panel de totales
        JPanel totalsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        totalsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel subtotalLabel = new JLabel("Subtotal: ₡0.00");
        JLabel impuestosLabel = new JLabel("Impuestos (13%): ₡0.00");
        totalsPanel.add(subtotalLabel);
        totalsPanel.add(impuestosLabel);
        totalsPanel.add(totalLabel);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        buttonPanel.add(finalizarOrdenButton);
        buttonPanel.add(cancelarOrdenButton);
        totalsPanel.add(buttonPanel);
        
        panel.add(orderScrollPane, BorderLayout.CENTER);
        panel.add(totalsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadProductos() {
        try {
            List<Producto> productos = productoService.obtenerTodosLosProductos();
            displayProductos(productos);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar productos: " + e.getMessage());
        }
    }
    
    private void loadProductosPorCategoria(Long categoriaId) {
        try {
            List<Producto> productos = productoService.obtenerProductosPorCategoria(categoriaId);
            displayProductos(productos);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar productos: " + e.getMessage());
        }
    }
    
    private void displayProductos(List<Producto> productos) {
        productPanel.removeAll();
        
        for (Producto producto : productos) {
            if (producto.isActivo()) {
                JPanel productCard = createProductCard(producto);
                productPanel.add(productCard);
                productPanel.add(Box.createVerticalStrut(5));
            }
        }
        
        // También mostrar combos
        try {
            List<Combo> combos = comboService.obtenerCombosActivos();
            for (Combo combo : combos) {
                JPanel comboCard = createComboCard(combo);
                productPanel.add(comboCard);
                productPanel.add(Box.createVerticalStrut(5));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        productPanel.revalidate();
        productPanel.repaint();
    }
    
    private JPanel createProductCard(Producto producto) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createRaisedBevelBorder());
        card.setPreferredSize(new Dimension(450, 80));
        
        // Información del producto
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        JLabel nameLabel = new JLabel(producto.getNombre());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        JLabel priceLabel = new JLabel(CurrencyUtil.formatCurrency(producto.getPrecio()));
        priceLabel.setForeground(new Color(0, 128, 0));
        
        JLabel stockLabel = new JLabel("Stock: " + producto.getStock());
        if (producto.necesitaReposicion()) {
            stockLabel.setForeground(Color.RED);
        }
        
        infoPanel.add(nameLabel);
        infoPanel.add(priceLabel);
        infoPanel.add(stockLabel);
        
        // Botón agregar
        JButton addButton = new JButton("Agregar");
        addButton.setBackground(new Color(74, 144, 226));
        addButton.setForeground(Color.BLUE);
        addButton.addActionListener(e -> agregarProductoAOrden(producto));
        
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(addButton, BorderLayout.EAST);
        
        return card;
    }
    
    private JPanel createComboCard(Combo combo) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createRaisedBevelBorder());
        card.setPreferredSize(new Dimension(450, 80));
        card.setBackground(new Color(255, 248, 220)); // Fondo diferente para combos
        
        // Información del combo
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        JLabel nameLabel = new JLabel("COMBO: " + combo.getNombre());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        nameLabel.setForeground(new Color(139, 69, 19));
        
        JLabel priceLabel = new JLabel(CurrencyUtil.formatCurrency(combo.getPrecioConDescuento()));
        priceLabel.setForeground(new Color(0, 128, 0));
        
        JLabel descLabel = new JLabel(combo.getDescripcion());
        descLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        
        infoPanel.add(nameLabel);
        infoPanel.add(priceLabel);
        infoPanel.add(descLabel);
        
        // Botón agregar
        JButton addButton = new JButton("Agregar Combo");
        addButton.setBackground(new Color(139, 69, 19));
        addButton.setForeground(Color.BLUE);
        addButton.addActionListener(e -> agregarComboAOrden(combo));
        
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(addButton, BorderLayout.EAST);
        
        return card;
    }
    
    private void agregarProductoAOrden(Producto producto) {
        if (producto.getStock() <= 0) {
            JOptionPane.showMessageDialog(this, "Producto sin stock disponible");
            return;
        }
        
        OrdenDetalle detalle = new OrdenDetalle(
            ordenActual.getId(),
            producto.getId(),
            producto.getNombre(),
            1,
            producto.getPrecio()
        );
        
        ordenActual.getDetalles().add(detalle);
        actualizarTablaOrden();
    }
    
    private void agregarComboAOrden(Combo combo) {
        try {
            if (!comboService.verificarStockDisponibleCombo(combo.getId(), 1)) {
                JOptionPane.showMessageDialog(this, "Stock insuficiente para el combo");
                return;
            }
            
            OrdenDetalle detalle = new OrdenDetalle(
                ordenActual.getId(),
                combo.getId(),
                "COMBO: " + combo.getNombre(),
                1,
                combo.getPrecioConDescuento(),
                true
            );
            
            ordenActual.getDetalles().add(detalle);
            actualizarTablaOrden();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al verificar stock: " + e.getMessage());
        }
    }
    
    private void actualizarTablaOrden() {
        ordenTableModel.setRowCount(0);
        
        for (int i = 0; i < ordenActual.getDetalles().size(); i++) {
            OrdenDetalle detalle = ordenActual.getDetalles().get(i);
            Object[] row = {
                detalle.getItemNombre(),
                detalle.getCantidad(),
                CurrencyUtil.formatCurrency(detalle.getPrecioUnitario()),
                CurrencyUtil.formatCurrency(detalle.getSubtotal()),
                "Eliminar"
            };
            ordenTableModel.addRow(row);
        }
        
        ordenActual.calcularTotales();
        totalLabel.setText("Total: " + CurrencyUtil.formatCurrency(ordenActual.getTotal()));
        finalizarOrdenButton.setEnabled(!ordenActual.getDetalles().isEmpty());
    }
    
    private void setupEventListeners() {
        finalizarOrdenButton.addActionListener(e -> finalizarOrden());
        cancelarOrdenButton.addActionListener(e -> cancelarOrden());
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });
    }
    
    private void finalizarOrden() {
        if (ordenActual.getDetalles().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La orden está vacía");
            return;
        }
        
        try {
            Orden ordenGuardada = ordenService.guardarOrden(ordenActual);
            
            JOptionPane.showMessageDialog(this, 
                "Orden #" + ordenGuardada.getNumero() + " creada exitosamente\n" +
                "Total: " + CurrencyUtil.formatCurrency(ordenGuardada.getTotal()));
            
            // Generar factura
            generateInvoice(ordenGuardada);
            
            // Crear nueva orden
            ordenActual = ordenService.crearOrden(usuarioService.getUsuarioActual().getId());
            actualizarTablaOrden();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al guardar orden: " + e.getMessage());
        }
    }
    
    private void cancelarOrden() {
        if (!ordenActual.getDetalles().isEmpty()) {
            int option = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro que desea cancelar la orden actual?",
                "Confirmar Cancelación", 
                JOptionPane.YES_NO_OPTION);
            
            if (option == JOptionPane.YES_OPTION) {
                try {
                    ordenActual = ordenService.crearOrden(usuarioService.getUsuarioActual().getId());
                    actualizarTablaOrden();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error al crear nueva orden: " + e.getMessage());
                }
            }
        }
    }
    
    private void generateInvoice(Orden orden) {
        // Aquí implementarías la generación de la factura PDF
        // Por ahora, solo mostrar un diálogo
        JDialog invoiceDialog = new JDialog(this, "Factura - " + orden.getNumero(), true);
        invoiceDialog.setSize(400, 500);
        invoiceDialog.setLocationRelativeTo(this);
        
        JTextArea invoiceArea = new JTextArea();
        invoiceArea.setEditable(false);
        invoiceArea.setFont(new Font("Courier", Font.PLAIN, 12));
        
        StringBuilder invoice = new StringBuilder();
        invoice.append("=====================================\n");
        invoice.append("        FIDEBURGUESAS POS\n");
        invoice.append("=====================================\n");
        invoice.append("Orden #: ").append(orden.getNumero()).append("\n");
        invoice.append("Fecha: ").append(DateUtil.formatDateTime(orden.getFecha())).append("\n");
        invoice.append("Cajero: ").append(orden.getCajeroNombre()).append("\n");
        invoice.append("=====================================\n");
        
        for (OrdenDetalle detalle : orden.getDetalles()) {
            invoice.append(String.format("%-20s %2d x %8s = %8s\n",
                detalle.getItemNombre().substring(0, Math.min(20, detalle.getItemNombre().length())),
                detalle.getCantidad(),
                CurrencyUtil.formatCurrency(detalle.getPrecioUnitario()),
                CurrencyUtil.formatCurrency(detalle.getSubtotal())
            ));
        }
        
        invoice.append("=====================================\n");
        invoice.append(String.format("Subtotal: %25s\n", CurrencyUtil.formatCurrency(orden.getSubtotal())));
        invoice.append(String.format("Impuestos (13%%): %18s\n", CurrencyUtil.formatCurrency(orden.getImpuestos())));
        invoice.append(String.format("TOTAL: %28s\n", CurrencyUtil.formatCurrency(orden.getTotal())));
        invoice.append("=====================================\n");
        invoice.append("    ¡Gracias por su compra!\n");
        invoice.append("=====================================\n");
        
        invoiceArea.setText(invoice.toString());
        
        JScrollPane scrollPane = new JScrollPane(invoiceArea);
        JButton printButton = new JButton("Imprimir");
        JButton closeButton = new JButton("Cerrar");
        
        closeButton.addActionListener(e -> invoiceDialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(printButton);
        buttonPanel.add(closeButton);
        
        invoiceDialog.add(scrollPane, BorderLayout.CENTER);
        invoiceDialog.add(buttonPanel, BorderLayout.SOUTH);
        invoiceDialog.setVisible(true);
    }
    
    private void logout() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea cerrar sesión?",
            "Confirmar Cierre",
            JOptionPane.YES_NO_OPTION
        );
        
        if (option == JOptionPane.YES_OPTION) {
            usuarioService.cerrarSesion();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
    
    private void setDefaultConfiguration() {
        setTitle("FideBurguesas POS - Caja");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1024, 768));
        setLocationRelativeTo(null);
    }
    
    // Clases auxiliares para botones en tabla
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Eliminar");
            setBackground(new Color(220, 20, 60));
            setForeground(Color.WHITE);
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
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = "Eliminar";
            button.setText(label);
            button.setBackground(new Color(220, 20, 60));
            button.setForeground(Color.BLUE);
            isPushed = true;
            return button;
        }
        
        public Object getCellEditorValue() {
            if (isPushed) {
                // Eliminar item de la orden
                int row = ordenTable.getSelectedRow();
                if (row >= 0 && row < ordenActual.getDetalles().size()) {
                    ordenActual.getDetalles().remove(row);
                    actualizarTablaOrden();
                }
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