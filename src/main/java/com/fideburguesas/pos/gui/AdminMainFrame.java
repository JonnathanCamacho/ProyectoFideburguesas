package com.fideburguesas.pos.gui;

import com.fideburguesas.pos.service.*;
import com.fideburguesas.pos.model.Usuario;
import com.fideburguesas.pos.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AdminMainFrame extends JFrame {
    private UsuarioService usuarioService;
    private ProductoService productoService;
    private CategoriaService categoriaService;
    private ComboService comboService;
    private OrdenService ordenService;
    
    private JLabel userLabel;
    private JTabbedPane tabbedPane;
    
    public AdminMainFrame(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
        this.productoService = new ProductoService();
        this.categoriaService = new CategoriaService();
        this.comboService = new ComboService();
        this.ordenService = new OrdenService();
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        setDefaultConfiguration();
    }
    
    private void initializeComponents() {
        Usuario usuario = usuarioService.getUsuarioActual();
        userLabel = new JLabel("Bienvenido, " + usuario.getNombreCompleto() + " | " + 
                              DateUtil.formatDateTime(java.time.LocalDateTime.now()));
        userLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        tabbedPane = new JTabbedPane();
        
        // Panel de Dashboard
        JPanel dashboardPanel = createDashboardPanel();
        tabbedPane.addTab("Dashboard", new ImageIcon(), dashboardPanel, "Resumen general");
        
        // Panel de Usuarios
        JPanel usuariosPanel = createUsuariosPanel();
        tabbedPane.addTab("Usuarios", new ImageIcon(), usuariosPanel, "Gestión de usuarios");
        
        // Panel de Categorías
        JPanel categoriasPanel = createCategoriasPanel();
        tabbedPane.addTab("Categorías", new ImageIcon(), categoriasPanel, "Gestión de categorías");
        
        // Panel de Productos
        JPanel productosPanel = createProductosPanel();
        tabbedPane.addTab("Productos", new ImageIcon(), productosPanel, "Gestión de productos");
        
        // Panel de Combos
        JPanel combosPanel = createCombosPanel();
        tabbedPane.addTab("Combos", new ImageIcon(), combosPanel, "Gestión de combos");
        
        // Panel de Órdenes
        JPanel ordenesPanel = createOrdenesPanel();
        tabbedPane.addTab("Órdenes", new ImageIcon(), ordenesPanel, "Gestión de órdenes");
        
        // Panel de Reportes
        JPanel reportesPanel = createReportesPanel();
        tabbedPane.addTab("Reportes", new ImageIcon(), reportesPanel, "Reportes y estadísticas");
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Panel superior con métricas
        JPanel metricsPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        metricsPanel.setBorder(BorderFactory.createTitledBorder("Métricas de Hoy"));
        metricsPanel.setBackground(Color.WHITE);
        
        // Métricas simuladas (aquí conectarías con los servicios reales)
        metricsPanel.add(createMetricCard("Órdenes", "15", Color.BLUE));
        metricsPanel.add(createMetricCard("Ventas", "₡125,000", Color.GREEN));
        metricsPanel.add(createMetricCard("Productos", "45", Color.ORANGE));
        metricsPanel.add(createMetricCard("Stock Bajo", "3", Color.RED));
        
        metricsPanel.add(createMetricCard("Usuarios Activos", "8", Color.CYAN));
        metricsPanel.add(createMetricCard("Combos", "12", Color.MAGENTA));
        metricsPanel.add(createMetricCard("Categorías", "5", Color.PINK));
        metricsPanel.add(createMetricCard("Promedio Orden", "₡8,333", Color.GRAY));
        
        // Panel central con actividad reciente
        JPanel activityPanel = new JPanel(new BorderLayout());
        activityPanel.setBorder(BorderFactory.createTitledBorder("Actividad Reciente"));
        activityPanel.setBackground(Color.WHITE);
        
        String[] columnNames = {"Hora", "Usuario", "Acción", "Detalle"};
        Object[][] data = {
            {"10:30", "cajero1", "Orden Creada", "ORD-20250617-0001"},
            {"10:25", "admin", "Producto Creado", "Hamburguesa Deluxe"},
            {"10:20", "cajero2", "Orden Entregada", "ORD-20250617-0002"},
            {"10:15", "cocina1", "Orden Lista", "ORD-20250617-0003"},
            {"10:10", "admin", "Usuario Creado", "nuevo_cajero"}
        };
        
        JTable activityTable = new JTable(data, columnNames);
        activityTable.setRowHeight(25);
        activityTable.getTableHeader().setBackground(new Color(74, 144, 226));
        activityTable.getTableHeader().setForeground(Color.BLACK);
        
        JScrollPane scrollPane = new JScrollPane(activityTable);
        activityPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(metricsPanel, BorderLayout.NORTH);
        panel.add(activityPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createMetricCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createRaisedBevelBorder());
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createUsuariosPanel() {
        return new UsuarioManagementPanel(); // Cambiar esta línea
    }
    
    private JPanel createCategoriasPanel() {
        return new CategoriaManagementPanel(); // Cambiar esta línea
    }
    
    private JPanel createProductosPanel() {
        return new ProductoManagementPanel(); // Cambiar esta línea
    }
    
    private JPanel createCombosPanel() {
        return new ComboManagementPanel(); // Cambiar esta línea
    }
    
    private JPanel createOrdenesPanel() {
        return new OrdenManagementPanel(); // Cambiar esta línea
    }
    
    private JPanel createReportesPanel() {
        return new ReportesManagementPanel(); // Cambiar esta línea
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel superior con información del usuario
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
        
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private void setupEventListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
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
            usuarioService.cerrarSesion();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
    
    private void setDefaultConfiguration() {
        setTitle("FideBurguesas POS - Administración");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1024, 768));
        setLocationRelativeTo(null);
        
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/icon.png")));
        } catch (Exception e) {
            // Icono no encontrado
        }
    }
}