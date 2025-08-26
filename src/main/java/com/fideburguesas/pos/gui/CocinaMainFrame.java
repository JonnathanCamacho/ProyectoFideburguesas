package com.fideburguesas.pos.gui;

import com.fideburguesas.pos.service.*;
import com.fideburguesas.pos.model.*;
import com.fideburguesas.pos.util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.List;
import javax.swing.Timer;

public class CocinaMainFrame extends JFrame {
    private UsuarioService usuarioService;
    private OrdenService ordenService;
    
    private JLabel userLabel;
    private JTable ordenesTable;
    private DefaultTableModel ordenesTableModel;
    private JPanel ordenesPanel;
    private Timer refreshTimer;
    
    public CocinaMainFrame(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
        this.ordenService = new OrdenService();
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        setDefaultConfiguration();
        loadOrdenesPendientes();
        startAutoRefresh();
    }
    
    private void initializeComponents() {
        Usuario usuario = usuarioService.getUsuarioActual();
        userLabel = new JLabel("Cocina: " + usuario.getNombreCompleto() + " | " + 
                              DateUtil.formatDateTime(java.time.LocalDateTime.now()));
        userLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Tabla de órdenes
        String[] columnNames = {"#Orden", "Hora", "Cajero", "Estado", "Items", "Acciones"};
        ordenesTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Solo columna de acciones
            }
        };
        ordenesTable = new JTable(ordenesTableModel);
        ordenesTable.setRowHeight(40);
        ordenesTable.getTableHeader().setBackground(new Color(74, 144, 226));
        ordenesTable.getTableHeader().setForeground(Color.BLACK);
        
        // Panel para mostrar órdenes como cards
        ordenesPanel = new JPanel();
        ordenesPanel.setLayout(new BoxLayout(ordenesPanel, BoxLayout.Y_AXIS));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel superior
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(74, 144, 226));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        userLabel.setForeground(Color.WHITE);
        topPanel.add(userLabel, BorderLayout.WEST);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(74, 144, 226));
        
        JButton refreshButton = new JButton("Actualizar");
        refreshButton.setBackground(Color.WHITE);
        refreshButton.setForeground(new Color(74, 144, 226));
        refreshButton.addActionListener(e -> loadOrdenesPendientes());
        
        JButton logoutButton = new JButton("Cerrar Sesión");
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(new Color(74, 144, 226));
        logoutButton.addActionListener(e -> logout());
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(logoutButton);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Panel central con scroll
        JScrollPane scrollPane = new JScrollPane(ordenesPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadOrdenesPendientes() {
        try {
            List<Orden> ordenes = ordenService.obtenerOrdenesPendientesCocina();
            displayOrdenes(ordenes);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar órdenes: " + e.getMessage());
        }
    }
    
    private void displayOrdenes(List<Orden> ordenes) {
        ordenesPanel.removeAll();
        
        if (ordenes.isEmpty()) {
            JLabel noOrdersLabel = new JLabel("No hay órdenes pendientes", SwingConstants.CENTER);
            noOrdersLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            noOrdersLabel.setForeground(Color.GRAY);
            ordenesPanel.add(noOrdersLabel);
        } else {
            for (Orden orden : ordenes) {
                JPanel ordenCard = createOrdenCard(orden);
                ordenesPanel.add(ordenCard);
                ordenesPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        ordenesPanel.revalidate();
        ordenesPanel.repaint();
    }
    
    private JPanel createOrdenCard(Orden orden) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createRaisedBevelBorder());
        card.setPreferredSize(new Dimension(0, 200));
        
        // Color según estado
        Color backgroundColor = Color.WHITE;
        switch (orden.getEstado()) {
            case PENDIENTE:
                backgroundColor = new Color(255, 248, 220); // Beige claro
                break;
            case EN_PREPARACION:
                backgroundColor = new Color(255, 239, 213); // Naranja claro
                break;
        }
        card.setBackground(backgroundColor);
        
        // Panel superior con información básica
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(backgroundColor);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel ordenLabel = new JLabel("ORDEN #" + orden.getNumero());
        ordenLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel tiempoLabel = new JLabel(DateUtil.getRelativeTime(orden.getFecha()));
        tiempoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        tiempoLabel.setForeground(Color.GRAY);
        
        JLabel estadoLabel = new JLabel(orden.getEstado().toString());
        estadoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        estadoLabel.setForeground(getColorForEstado(orden.getEstado()));
        
        JPanel headerInfo = new JPanel(new GridLayout(3, 1));
        headerInfo.setBackground(backgroundColor);
        headerInfo.add(ordenLabel);
        headerInfo.add(tiempoLabel);
        headerInfo.add(estadoLabel);
        
        headerPanel.add(headerInfo, BorderLayout.WEST);
        
        // Panel de detalles
        JPanel detallesPanel = new JPanel();
        detallesPanel.setLayout(new BoxLayout(detallesPanel, BoxLayout.Y_AXIS));
        detallesPanel.setBackground(backgroundColor);
        detallesPanel.setBorder(BorderFactory.createTitledBorder("Items de la Orden"));
        
        for (OrdenDetalle detalle : orden.getDetalles()) {
            JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            itemPanel.setBackground(backgroundColor);
            
            JLabel cantidadLabel = new JLabel(String.valueOf(detalle.getCantidad()));
            cantidadLabel.setFont(new Font("Arial", Font.BOLD, 14));
            cantidadLabel.setPreferredSize(new Dimension(30, 20));
            cantidadLabel.setHorizontalAlignment(SwingConstants.CENTER);
            cantidadLabel.setOpaque(true);
            cantidadLabel.setBackground(new Color(74, 144, 226));
            cantidadLabel.setForeground(Color.WHITE);
            
            JLabel itemLabel = new JLabel(detalle.getItemNombre());
            itemLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            
            itemPanel.add(cantidadLabel);
            itemPanel.add(itemLabel);
            
            if (detalle.getObservaciones() != null && !detalle.getObservaciones().trim().isEmpty()) {
                JLabel obsLabel = new JLabel("- " + detalle.getObservaciones());
                obsLabel.setFont(new Font("Arial", Font.ITALIC, 10));
                obsLabel.setForeground(Color.BLUE);
                itemPanel.add(obsLabel);
            }
            
            detallesPanel.add(itemPanel);
        }
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(backgroundColor);
        
        if (orden.getEstado() == Orden.EstadoOrden.PENDIENTE) {
            JButton iniciarButton = new JButton("Iniciar Preparación");
            iniciarButton.setBackground(new Color(255, 165, 0));
            iniciarButton.setForeground(Color.WHITE);
            iniciarButton.addActionListener(e -> cambiarEstadoOrden(orden.getId(), Orden.EstadoOrden.EN_PREPARACION));
            buttonPanel.add(iniciarButton);
        } else if (orden.getEstado() == Orden.EstadoOrden.EN_PREPARACION) {
            JButton listaButton = new JButton("Marcar Lista");
            listaButton.setBackground(new Color(34, 139, 34));
            listaButton.setForeground(Color.WHITE);
            listaButton.addActionListener(e -> cambiarEstadoOrden(orden.getId(), Orden.EstadoOrden.LISTA));
            buttonPanel.add(listaButton);
        }
        
        JButton verDetallesButton = new JButton("Ver Detalles");
        verDetallesButton.setBackground(new Color(74, 144, 226));
        verDetallesButton.setForeground(Color.WHITE);
        verDetallesButton.addActionListener(e -> mostrarDetallesOrden(orden));
        buttonPanel.add(verDetallesButton);
        
        card.add(headerPanel, BorderLayout.NORTH);
        card.add(detallesPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private Color getColorForEstado(Orden.EstadoOrden estado) {
        switch (estado) {
            case PENDIENTE:
                return new Color(255, 140, 0); // Naranja
            case EN_PREPARACION:
                return new Color(255, 69, 0); // Rojo-naranja
            case LISTA:
                return new Color(34, 139, 34); // Verde
            default:
                return Color.BLACK;
        }
    }
    
    private void cambiarEstadoOrden(Long ordenId, Orden.EstadoOrden nuevoEstado) {
        try {
            ordenService.cambiarEstadoOrden(ordenId, nuevoEstado);
            loadOrdenesPendientes(); // Recargar la lista
            
            String mensaje = "Orden actualizada a: " + nuevoEstado.toString();
            JOptionPane.showMessageDialog(this, mensaje, "Estado Actualizado", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar orden: " + e.getMessage());
        }
    }
    
    private void mostrarDetallesOrden(Orden orden) {
        JDialog detallesDialog = new JDialog(this, "Detalles de Orden #" + orden.getNumero(), true);
        detallesDialog.setSize(500, 400);
        detallesDialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Información general
        JPanel infoPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Información General"));
        
        infoPanel.add(new JLabel("Número de Orden:"));
        infoPanel.add(new JLabel(orden.getNumero()));
        
        infoPanel.add(new JLabel("Fecha:"));
        infoPanel.add(new JLabel(DateUtil.formatDateTime(orden.getFecha())));
        
        infoPanel.add(new JLabel("Cajero:"));
        infoPanel.add(new JLabel(orden.getCajeroNombre()));
        
        infoPanel.add(new JLabel("Estado:"));
        JLabel estadoLabel = new JLabel(orden.getEstado().toString());
        estadoLabel.setForeground(getColorForEstado(orden.getEstado()));
        infoPanel.add(estadoLabel);
        
        infoPanel.add(new JLabel("Total:"));
        infoPanel.add(new JLabel(com.fideburguesas.pos.util.CurrencyUtil.formatCurrency(orden.getTotal())));
        
        // Tabla de detalles
        String[] columnNames = {"Cantidad", "Producto", "Precio Unit.", "Subtotal", "Observaciones"};
        Object[][] data = new Object[orden.getDetalles().size()][5];
        
        for (int i = 0; i < orden.getDetalles().size(); i++) {
            OrdenDetalle detalle = orden.getDetalles().get(i);
            data[i][0] = detalle.getCantidad();
            data[i][1] = detalle.getItemNombre();
            data[i][2] = com.fideburguesas.pos.util.CurrencyUtil.formatCurrency(detalle.getPrecioUnitario());
            data[i][3] = com.fideburguesas.pos.util.CurrencyUtil.formatCurrency(detalle.getSubtotal());
            data[i][4] = detalle.getObservaciones() != null ? detalle.getObservaciones() : "";
        }
        
        JTable detallesTable = new JTable(data, columnNames);
        detallesTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(detallesTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Detalles de la Orden"));
        
        // Botón cerrar
        JButton cerrarButton = new JButton("Cerrar");
        cerrarButton.addActionListener(e -> detallesDialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(cerrarButton);
        
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        detallesDialog.add(mainPanel);
        detallesDialog.setVisible(true);
    }
    
    private void startAutoRefresh() {
        // Actualizar cada 30 segundos
        refreshTimer = new Timer(30000, e -> loadOrdenesPendientes());
        refreshTimer.start();
    }
    
    private void setupEventListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (refreshTimer != null) {
                    refreshTimer.stop();
                }
                logout();
            }
        });
    }
    
    private void logout() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea cerrar sesión?",
            "Confirmar Cierre",
            JOptionPane.YES_NO_OPTION
        );
        
        if (option == JOptionPane.YES_OPTION) {
            if (refreshTimer != null) {
                refreshTimer.stop();
            }
            usuarioService.cerrarSesion();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
    
    private void setDefaultConfiguration() {
        setTitle("FideBurguesas POS - Cocina");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
    }
}