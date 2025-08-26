package com.fideburguesas.pos.gui;

import com.fideburguesas.pos.model.Orden;
import com.fideburguesas.pos.model.OrdenDetalle;
import com.fideburguesas.pos.util.CurrencyUtil;
import com.fideburguesas.pos.util.DateUtil;

import javax.swing.*;
import java.awt.*;

public class OrdenDetailsDialog extends JDialog {
    private Orden orden;
    
    public OrdenDetailsDialog(JFrame parent, Orden orden) {
        super(parent, "Detalles de Orden #" + orden.getNumero(), true);
        this.orden = orden;
        initializeComponents();
        setupLayout();
        setDefaultConfiguration();
    }
    
    private void initializeComponents() {
        // Los componentes se crearán en setupLayout()
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel principal con tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Tab 1: Información general
        JPanel infoPanel = createInfoPanel();
        tabbedPane.addTab("📋 Información", infoPanel);
        
        // Tab 2: Detalles de productos
        JPanel detallesPanel = createDetallesPanel();
        tabbedPane.addTab("🛍️ Productos", detallesPanel);
        
        // Tab 3: Resumen financiero
        JPanel resumenPanel = createResumenPanel();
        tabbedPane.addTab("💰 Resumen", resumenPanel);
        
        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        
        JButton imprimirButton = new JButton("🖨️ Imprimir");
        JButton cerrarButton = new JButton("Cerrar");
        
        imprimirButton.setBackground(new Color(173, 216, 230));
        imprimirButton.setForeground(new Color(0, 0, 139));
        imprimirButton.setBorder(BorderFactory.createRaisedBevelBorder());
        imprimirButton.setFocusPainted(false);
        
        cerrarButton.setBackground(new Color(211, 211, 211));
        cerrarButton.setForeground(new Color(105, 105, 105));
        cerrarButton.setBorder(BorderFactory.createRaisedBevelBorder());
        cerrarButton.setFocusPainted(false);
        
        buttonPanel.add(imprimirButton);
        buttonPanel.add(cerrarButton);
        
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Event listeners
        imprimirButton.addActionListener(e -> imprimirOrden());
        cerrarButton.addActionListener(e -> dispose());
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Información básica
        addInfoRow(panel, gbc, 0, "Número de Orden:", orden.getNumero());
        addInfoRow(panel, gbc, 1, "Fecha:", DateUtil.formatDateTime(orden.getFecha()));
        addInfoRow(panel, gbc, 2, "Cajero:", orden.getCajeroNombre());
        
        // Estado con color
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Estado:"), gbc);
        gbc.gridx = 1;
        JLabel estadoLabel = new JLabel(orden.getEstado().toString());
        estadoLabel.setForeground(getColorForEstado(orden.getEstado()));
        estadoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(estadoLabel, gbc);
        
        addInfoRow(panel, gbc, 4, "Cantidad de Items:", String.valueOf(orden.getDetalles().size()));
        
        // Observaciones
        if (orden.getObservaciones() != null && !orden.getObservaciones().trim().isEmpty()) {
            gbc.gridx = 0; gbc.gridy = 5;
            panel.add(new JLabel("Observaciones:"), gbc);
            gbc.gridx = 1; gbc.gridwidth = 2;
            JTextArea obsArea = new JTextArea(orden.getObservaciones());
            obsArea.setEditable(false);
            obsArea.setBackground(panel.getBackground());
            obsArea.setBorder(BorderFactory.createLoweredBevelBorder());
            obsArea.setRows(3);
            panel.add(new JScrollPane(obsArea), gbc);
        }
        
        return panel;
    }
    
    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(labelComp, gbc);
        
        gbc.gridx = 1;
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(valueComp, gbc);
    }
    
    private JPanel createDetallesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Tabla de detalles
        String[] columnNames = {"Cantidad", "Producto", "Precio Unit.", "Subtotal", "Observaciones"};
        Object[][] data = new Object[orden.getDetalles().size()][5];
        
        for (int i = 0; i < orden.getDetalles().size(); i++) {
            OrdenDetalle detalle = orden.getDetalles().get(i);
            data[i][0] = detalle.getCantidad();
            data[i][1] = detalle.getItemNombre();
            data[i][2] = CurrencyUtil.formatCurrency(detalle.getPrecioUnitario());
            data[i][3] = CurrencyUtil.formatCurrency(detalle.getSubtotal());
            data[i][4] = detalle.getObservaciones() != null ? detalle.getObservaciones() : "";
        }
        
        JTable detallesTable = new JTable(data, columnNames);
        detallesTable.setRowHeight(30);
        detallesTable.setFont(new Font("Arial", Font.PLAIN, 12));
        detallesTable.getTableHeader().setBackground(new Color(34, 139, 34));
        detallesTable.getTableHeader().setForeground(Color.BLACK);
        
        // Configurar anchos
        detallesTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Cantidad
        detallesTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Producto
        detallesTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Precio Unit
        detallesTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Subtotal
        detallesTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Observaciones
        
        JScrollPane scrollPane = new JScrollPane(detallesTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Productos de la Orden"));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createResumenPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Panel central con totales
        JPanel totalesPanel = new JPanel(new GridBagLayout());
        totalesPanel.setBackground(Color.WHITE);
        totalesPanel.setBorder(BorderFactory.createTitledBorder("Resumen Financiero"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.anchor = GridBagConstraints.EAST;
        
        // Subtotal
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel subtotalLabel = new JLabel("Subtotal:");
        subtotalLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        totalesPanel.add(subtotalLabel, gbc);
        
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        JLabel subtotalValue = new JLabel(CurrencyUtil.formatCurrency(orden.getSubtotal()));
        subtotalValue.setFont(new Font("Arial", Font.PLAIN, 14));
        totalesPanel.add(subtotalValue, gbc);
        
        // Impuestos
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        JLabel impuestosLabel = new JLabel("Impuestos (13%):");
        impuestosLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        totalesPanel.add(impuestosLabel, gbc);
        
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        JLabel impuestosValue = new JLabel(CurrencyUtil.formatCurrency(orden.getImpuestos()));
        impuestosValue.setFont(new Font("Arial", Font.PLAIN, 14));
        totalesPanel.add(impuestosValue, gbc);
        
        // Línea separadora
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        totalesPanel.add(new JSeparator(), gbc);
        
        // Total
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel totalLabel = new JLabel("TOTAL:");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalesPanel.add(totalLabel, gbc);
        
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        JLabel totalValue = new JLabel(CurrencyUtil.formatCurrency(orden.getTotal()));
        totalValue.setFont(new Font("Arial", Font.BOLD, 16));
        totalValue.setForeground(new Color(0, 128, 0));
        totalesPanel.add(totalValue, gbc);
        
        panel.add(totalesPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private Color getColorForEstado(Orden.EstadoOrden estado) {
        switch (estado) {
            case PENDIENTE:
                return new Color(255, 140, 0); // Naranja
            case EN_PREPARACION:
                return new Color(255, 69, 0); // Rojo-naranja
            case LISTA:
                return new Color(34, 139, 34); // Verde
            case ENTREGADA:
                return new Color(0, 100, 0); // Verde oscuro
            case CANCELADA:
                return Color.RED;
            default:
                return Color.BLACK;
        }
    }
    
    private void imprimirOrden() {
        JOptionPane.showMessageDialog(this,
            "🖨️ Funcionalidad de impresión en desarrollo...\n\n" +
            "Próximamente se podrá:\n" +
            "• Imprimir ticket de la orden\n" +
            "• Configurar impresora\n" +
            "• Previsualizar antes de imprimir",
            "Imprimir",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void setDefaultConfiguration() {
        setSize(700, 500);
        setLocationRelativeTo(getParent());
        setResizable(true);
    }
}