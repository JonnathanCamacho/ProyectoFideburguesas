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
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportesManagementPanel extends JPanel {
    private OrdenService ordenService;
    private ProductoService productoService;
    private UsuarioService usuarioService;
    
    // Componentes principales
    private JTabbedPane tabbedPane;
    private JComboBox<String> periodoComboBox;
    private JLabel totalVentasLabel, totalOrdenesLabel, promedioOrdenLabel;
    private JLabel mejorCajeroLabel, mejorProductoLabel, stockBajoLabel;
    
    // Tablas para reportes
    private JTable ventasDiariasTable, productosTable, cajerosTable, categoriasTable;
    private DefaultTableModel ventasDiariasModel, productosModel, cajerosModel, categoriasModel;
    
    // Paneles de gráficos (simulados con texto por ahora)
    private JTextArea ventasGraficoArea, productosGraficoArea;
    
    public ReportesManagementPanel() {
        this.ordenService = new OrdenService();
        this.productoService = new ProductoService();
        this.usuarioService = new UsuarioService();
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadReportes();
    }
    
    private void initializeComponents() {
        // Componentes principales
        tabbedPane = new JTabbedPane();
        
        periodoComboBox = new JComboBox<>(new String[]{
            "Hoy", "Ayer", "Esta semana", "Este mes", "Últimos 30 días", "Este año"
        });
        
        // Labels de resumen general
        totalVentasLabel = new JLabel("Total Ventas: ₡0.00");
        totalOrdenesLabel = new JLabel("Total Órdenes: 0");
        promedioOrdenLabel = new JLabel("Promedio por Orden: ₡0.00");
        mejorCajeroLabel = new JLabel("Mejor Cajero: N/A");
        mejorProductoLabel = new JLabel("Producto Top: N/A");
        stockBajoLabel = new JLabel("Productos con Stock Bajo: 0");
        
        // Configurar estilos
        Font headerFont = new Font("Arial", Font.BOLD, 14);
        totalVentasLabel.setFont(headerFont);
        totalVentasLabel.setForeground(new Color(0, 128, 0));
        totalOrdenesLabel.setFont(headerFont);
        totalOrdenesLabel.setForeground(new Color(0, 0, 139));
        promedioOrdenLabel.setFont(headerFont);
        stockBajoLabel.setForeground(Color.RED);
        
        // Inicializar tablas
        initializeTables();
        
        // Áreas de gráficos (simuladas)
        ventasGraficoArea = new JTextArea(15, 40);
        ventasGraficoArea.setEditable(false);
        ventasGraficoArea.setFont(new Font("Courier", Font.PLAIN, 12));
        ventasGraficoArea.setBackground(new Color(245, 245, 245));
        
        productosGraficoArea = new JTextArea(15, 40);
        productosGraficoArea.setEditable(false);
        productosGraficoArea.setFont(new Font("Courier", Font.PLAIN, 12));
        productosGraficoArea.setBackground(new Color(245, 245, 245));
    }
    
    private void initializeTables() {
        // Tabla ventas diarias
        String[] ventasColumns = {"Fecha", "Órdenes", "Ventas", "Promedio"};
        ventasDiariasModel = new DefaultTableModel(ventasColumns, 0);
        ventasDiariasTable = new JTable(ventasDiariasModel);
        ventasDiariasTable.setRowHeight(25);
        
        // Tabla productos más vendidos
        String[] productosColumns = {"Producto", "Cantidad Vendida", "Ingresos", "% del Total"};
        productosModel = new DefaultTableModel(productosColumns, 0);
        productosTable = new JTable(productosModel);
        productosTable.setRowHeight(25);
        
        // Tabla rendimiento de cajeros
        String[] cajerosColumns = {"Cajero", "Órdenes", "Ventas", "Promedio", "Último Login"};
        cajerosModel = new DefaultTableModel(cajerosColumns, 0);
        cajerosTable = new JTable(cajerosModel);
        cajerosTable.setRowHeight(25);
        
        // Tabla ventas por categoría
        String[] categoriasColumns = {"Categoría", "Productos Vendidos", "Ingresos", "% del Total"};
        categoriasModel = new DefaultTableModel(categoriasColumns, 0);
        categoriasTable = new JTable(categoriasModel);
        categoriasTable.setRowHeight(25);
        
        // Configurar colores de headers
        Color headerColor = new Color(70, 130, 180);
        ventasDiariasTable.getTableHeader().setBackground(headerColor);
        ventasDiariasTable.getTableHeader().setForeground(Color.BLACK);
        productosTable.getTableHeader().setBackground(headerColor);
        productosTable.getTableHeader().setForeground(Color.BLACK);
        cajerosTable.getTableHeader().setBackground(headerColor);
        cajerosTable.getTableHeader().setForeground(Color.BLACK);
        categoriasTable.getTableHeader().setBackground(headerColor);
        categoriasTable.getTableHeader().setForeground(Color.BLACK);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Panel superior con título y controles
        JPanel topPanel = createTopPanel();
        
        // Panel de resumen general
        JPanel summaryPanel = createSummaryPanel();
        
        // Crear tabs de reportes
        createReporteTabs();
        
        add(topPanel, BorderLayout.NORTH);
        add(summaryPanel, BorderLayout.CENTER);
        add(tabbedPane, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Título
        JLabel titleLabel = new JLabel("📊 Reportes y Estadísticas");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(70, 130, 180));
        
        // Panel de controles
        JPanel controlsPanel = new JPanel(new FlowLayout());
        controlsPanel.setBackground(Color.WHITE);
        
        controlsPanel.add(new JLabel("Período:"));
        controlsPanel.add(periodoComboBox);
        
        JButton actualizarButton = new JButton("🔄 Actualizar");
        actualizarButton.setBackground(new Color(70, 130, 180));
        actualizarButton.setForeground(Color.BLACK);
        actualizarButton.setBorder(BorderFactory.createRaisedBevelBorder());
        actualizarButton.setFocusPainted(false);
        actualizarButton.addActionListener(e -> loadReportes());
        controlsPanel.add(actualizarButton);
        
        JButton exportarButton = new JButton("📤 Exportar");
        exportarButton.setBackground(new Color(34, 139, 34));
        exportarButton.setForeground(Color.BLACK);
        exportarButton.setBorder(BorderFactory.createRaisedBevelBorder());
        exportarButton.setFocusPainted(false);
        exportarButton.addActionListener(e -> exportarReportes());
        controlsPanel.add(exportarButton);
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(controlsPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Resumen General"));
        
        // Crear cards de resumen
        panel.add(createSummaryCard("💰 Ventas", totalVentasLabel, new Color(0, 128, 0)));
        panel.add(createSummaryCard("📋 Órdenes", totalOrdenesLabel, new Color(0, 0, 139)));
        panel.add(createSummaryCard("📊 Promedio", promedioOrdenLabel, new Color(255, 140, 0)));
        panel.add(createSummaryCard("👤 Mejor Cajero", mejorCajeroLabel, new Color(138, 43, 226)));
        panel.add(createSummaryCard("🏆 Producto Top", mejorProductoLabel, new Color(220, 20, 60)));
        panel.add(createSummaryCard("⚠️ Stock Bajo", stockBajoLabel, Color.RED));
        
        return panel;
    }
    
    private JPanel createSummaryCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(color);
        
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void createReporteTabs() {
        tabbedPane.setPreferredSize(new Dimension(0, 300));
        
        // Tab 1: Ventas Diarias
        JPanel ventasPanel = new JPanel(new BorderLayout());
        ventasPanel.add(new JScrollPane(ventasDiariasTable), BorderLayout.CENTER);
        JPanel ventasGraphPanel = new JPanel(new BorderLayout());
        ventasGraphPanel.setBorder(BorderFactory.createTitledBorder("Gráfico de Ventas"));
        ventasGraphPanel.add(new JScrollPane(ventasGraficoArea), BorderLayout.CENTER);
        
        JSplitPane ventasSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                                               new JScrollPane(ventasDiariasTable), 
                                               ventasGraphPanel);
        ventasSplit.setDividerLocation(400);
        tabbedPane.addTab("📈 Ventas Diarias", ventasSplit);
        
        // Tab 2: Productos Más Vendidos
        JSplitPane productosSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                                                  new JScrollPane(productosTable), 
                                                  new JScrollPane(productosGraficoArea));
        productosSplit.setDividerLocation(400);
        tabbedPane.addTab("🛍️ Productos Top", productosSplit);
        
        // Tab 3: Rendimiento de Cajeros
        tabbedPane.addTab("👥 Cajeros", new JScrollPane(cajerosTable));
        
        // Tab 4: Ventas por Categoría
        tabbedPane.addTab("📂 Categorías", new JScrollPane(categoriasTable));
        
        // Tab 5: Análisis Avanzado
        JPanel analisisPanel = createAnalisisAvanzadoPanel();
        tabbedPane.addTab("🔍 Análisis", analisisPanel);
    }
    
    private JPanel createAnalisisAvanzadoPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel 1: Tendencias
        JPanel tendenciasPanel = new JPanel(new BorderLayout());
        tendenciasPanel.setBorder(BorderFactory.createTitledBorder("Tendencias de Ventas"));
        JTextArea tendenciasArea = new JTextArea(8, 30);
        tendenciasArea.setEditable(false);
        tendenciasArea.setBackground(new Color(245, 245, 245));
        tendenciasPanel.add(new JScrollPane(tendenciasArea), BorderLayout.CENTER);
        
        // Panel 2: Comparativas
        JPanel comparativasPanel = new JPanel(new BorderLayout());
        comparativasPanel.setBorder(BorderFactory.createTitledBorder("Comparativas Mensuales"));
        JTextArea comparativasArea = new JTextArea(8, 30);
        comparativasArea.setEditable(false);
        comparativasArea.setBackground(new Color(245, 245, 245));
        comparativasPanel.add(new JScrollPane(comparativasArea), BorderLayout.CENTER);
        
        // Panel 3: Proyecciones
        JPanel proyeccionesPanel = new JPanel(new BorderLayout());
        proyeccionesPanel.setBorder(BorderFactory.createTitledBorder("Proyecciones"));
        JTextArea proyeccionesArea = new JTextArea(8, 30);
        proyeccionesArea.setEditable(false);
        proyeccionesArea.setBackground(new Color(245, 245, 245));
        proyeccionesPanel.add(new JScrollPane(proyeccionesArea), BorderLayout.CENTER);
        
        // Panel 4: Recomendaciones
        JPanel recomendacionesPanel = new JPanel(new BorderLayout());
        recomendacionesPanel.setBorder(BorderFactory.createTitledBorder("Recomendaciones"));
        JTextArea recomendacionesArea = new JTextArea(8, 30);
        recomendacionesArea.setEditable(false);
        recomendacionesArea.setBackground(new Color(245, 245, 245));
        recomendacionesArea.setText(
            "📊 RECOMENDACIONES AUTOMÁTICAS:\n\n" +
            "• Incrementar stock de productos populares\n" +
            "• Crear promociones para productos de baja rotación\n" +
            "• Capacitar cajeros con menor rendimiento\n" +
            "• Optimizar horarios según picos de venta\n" +
            "• Revisar precios de categorías con bajo margen\n\n" +
            "🎯 PRÓXIMAS ACCIONES:\n" +
            "• Análisis de satisfacción de clientes\n" +
            "• Evaluación de combos más rentables\n" +
            "• Implementar programa de fidelización"
        );
        recomendacionesPanel.add(new JScrollPane(recomendacionesArea), BorderLayout.CENTER);
        
        panel.add(tendenciasPanel);
        panel.add(comparativasPanel);
        panel.add(proyeccionesPanel);
        panel.add(recomendacionesPanel);
        
        return panel;
    }
    
    private void setupEventListeners() {
        periodoComboBox.addActionListener(e -> loadReportes());
    }
    
    private void loadReportes() {
        try {
            loadResumenGeneral();
            loadVentasDiarias();
            loadProductosMasVendidos();
            loadRendimientoCajeros();
            loadVentasPorCategoria();
            loadGraficos();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar reportes: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadResumenGeneral() throws SQLException {
        // Obtener período seleccionado
        LocalDate fechaInicio = getFechaInicio();
        LocalDate fechaFin = LocalDate.now();
        
        // Calcular métricas generales
        List<Orden> ordenes = ordenService.obtenerOrdenesPorFecha(fechaFin); // Por ahora solo hoy
        
        BigDecimal totalVentas = ordenes.stream()
            .filter(o -> o.getEstado() == Orden.EstadoOrden.ENTREGADA)
            .map(Orden::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalOrdenes = (int) ordenes.stream()
            .filter(o -> o.getEstado() == Orden.EstadoOrden.ENTREGADA)
            .count();
        
        double promedioOrden = totalOrdenes > 0 ? 
            totalVentas.doubleValue() / totalOrdenes : 0;
        
        // Actualizar labels
        totalVentasLabel.setText("Total Ventas: " + CurrencyUtil.formatCurrency(totalVentas));
        totalOrdenesLabel.setText("Total Órdenes: " + totalOrdenes);
        promedioOrdenLabel.setText("Promedio por Orden: " + CurrencyUtil.formatCurrency(promedioOrden));
        
        // Mejor cajero (simplificado)
        if (!ordenes.isEmpty()) {
            Map<String, Long> ordenesPorCajero = ordenes.stream()
                .filter(o -> o.getEstado() == Orden.EstadoOrden.ENTREGADA)
                .collect(Collectors.groupingBy(Orden::getCajeroNombre, Collectors.counting()));
            
            String mejorCajero = ordenesPorCajero.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
            
            mejorCajeroLabel.setText("Mejor Cajero: " + mejorCajero);
        }
        
        // Stock bajo
        List<Producto> productosBajoStock = productoService.obtenerProductosBajoStock();
        stockBajoLabel.setText("Productos con Stock Bajo: " + productosBajoStock.size());
        
        // Producto más vendido (simulado)
        mejorProductoLabel.setText("Producto Top: Hamburguesa Clásica");
    }
    
    private void loadVentasDiarias() throws SQLException {
        ventasDiariasModel.setRowCount(0);
        
        // Simular datos de ventas diarias (últimos 7 días)
        for (int i = 6; i >= 0; i--) {
            LocalDate fecha = LocalDate.now().minusDays(i);
            List<Orden> ordenesDelDia = ordenService.obtenerOrdenesPorFecha(fecha);
            
            int ordenesEntregadas = (int) ordenesDelDia.stream()
                .filter(o -> o.getEstado() == Orden.EstadoOrden.ENTREGADA)
                .count();
            
            BigDecimal ventasDelDia = ordenesDelDia.stream()
                .filter(o -> o.getEstado() == Orden.EstadoOrden.ENTREGADA)
                .map(Orden::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            double promedio = ordenesEntregadas > 0 ? 
                ventasDelDia.doubleValue() / ordenesEntregadas : 0;
            
            Object[] row = {
                DateUtil.formatDate(fecha),
                ordenesEntregadas,
                CurrencyUtil.formatCurrency(ventasDelDia),
                CurrencyUtil.formatCurrency(promedio)
            };
            ventasDiariasModel.addRow(row);
        }
    }
    
    private void loadProductosMasVendidos() throws SQLException {
        productosModel.setRowCount(0);
        
        // Simular datos de productos más vendidos
        String[][] productosVendidos = {
            {"Hamburguesa Clásica", "45", "382,500", "35%"},
            {"Coca Cola", "38", "57,000", "15%"},
            {"Hamburguesa BBQ", "32", "304,000", "25%"},
            {"Papas Fritas", "28", "84,000", "12%"},
            {"Agua", "25", "25,000", "8%"},
            {"Combo Familiar", "12", "114,000", "5%"}
        };
        
        for (String[] row : productosVendidos) {
            productosModel.addRow(row);
        }
    }
    
    private void loadRendimientoCajeros() throws SQLException {
        cajerosModel.setRowCount(0);
        
        List<Usuario> cajeros = usuarioService.obtenerUsuariosPorTipo(Usuario.TipoUsuario.CAJERO);
        
        for (Usuario cajero : cajeros) {
            // Simular datos de rendimiento
            int ordenesAtendidas = (int)(Math.random() * 20) + 5;
            double ventas = (Math.random() * 100000) + 50000;
            double promedio = ventas / ordenesAtendidas;
            String ultimoLogin = cajero.getUltimoLogin() != null ? 
                DateUtil.formatDateTime(cajero.getUltimoLogin()) : "Nunca";
            
            Object[] row = {
                cajero.getNombreCompleto(),
                ordenesAtendidas,
                CurrencyUtil.formatCurrency(ventas),
                CurrencyUtil.formatCurrency(promedio),
                ultimoLogin
            };
            cajerosModel.addRow(row);
        }
    }
    
    private void loadVentasPorCategoria() throws SQLException {
        categoriasModel.setRowCount(0);
        
        // Simular datos de ventas por categoría
        String[][] ventasCategorias = {
            {"Hamburguesas", "77", "686,500", "45%"},
            {"Bebidas", "63", "82,000", "15%"},
            {"Acompañamientos", "28", "84,000", "12%"},
            {"Combos", "12", "114,000", "18%"},
            {"Postres", "15", "45,000", "6%"},
            {"Ensaladas", "8", "32,000", "4%"}
        };
        
        for (String[] row : ventasCategorias) {
            categoriasModel.addRow(row);
        }
    }
    
    private void loadGraficos() {
        // Gráfico de ventas (ASCII art simulado)
        ventasGraficoArea.setText(
            "GRÁFICO DE VENTAS - ÚLTIMOS 7 DÍAS\n" +
            "=====================================\n\n" +
            "Lun  ████████████████░░░░  ₡156,000\n" +
            "Mar  ██████████████████░░  ₡189,000\n" +
            "Mié  ████████████░░░░░░░░  ₡145,000\n" +
            "Jue  ████████████████████  ₡210,000\n" +
            "Vie  ██████████████████░░  ₡195,000\n" +
            "Sáb  ████████████████░░░░  ₡178,000\n" +
            "Dom  ██████████░░░░░░░░░░  ₡123,000\n\n" +
            "Tendencia: ↗️ +12% vs semana anterior\n" +
            "Mejor día: Jueves (₡210,000)\n" +
            "Meta semanal: ₡1,200,000 ✅\n" +
            "Proyección mensual: ₡5,850,000"
        );
        
        // Gráfico de productos
        productosGraficoArea.setText(
            "TOP 5 PRODUCTOS MÁS VENDIDOS\n" +
            "=============================\n\n" +
            "🍔 Hamburguesa Clásica\n" +
            "   ████████████████████ 45 unidades\n\n" +
            "🥤 Coca Cola\n" +
            "   ███████████████░░░░░ 38 unidades\n\n" +
            "🍔 Hamburguesa BBQ\n" +
            "   ██████████████░░░░░░ 32 unidades\n\n" +
            "🍟 Papas Fritas\n" +
            "   ████████████░░░░░░░░ 28 unidades\n\n" +
            "💧 Agua\n" +
            "   ██████████░░░░░░░░░░ 25 unidades\n\n" +
            "📊 Margen promedio: 65%\n" +
            "🎯 Oportunidad: Promocionar postres\n" +
            "⚡ Recomendación: Bundle con bebidas"
        );
    }
    
    private LocalDate getFechaInicio() {
        String periodo = (String) periodoComboBox.getSelectedItem();
        LocalDate hoy = LocalDate.now();
        
        switch (periodo) {
            case "Ayer":
                return hoy.minusDays(1);
            case "Esta semana":
                return hoy.minusWeeks(1);
            case "Este mes":
                return hoy.minusMonths(1);
            case "Últimos 30 días":
                return hoy.minusDays(30);
            case "Este año":
                return hoy.withDayOfYear(1);
            default: // "Hoy"
                return hoy;
        }
    }
    
    private void exportarReportes() {
        String[] opciones = {
            "📊 Reporte Completo (PDF)",
            "📈 Ventas Diarias (Excel)",
            "🛍️ Productos Top (CSV)",
            "👥 Rendimiento Cajeros (PDF)",
            "📂 Análisis por Categorías (Excel)"
        };
        
        String seleccion = (String) JOptionPane.showInputDialog(
            this,
            "Seleccione el tipo de reporte a exportar:",
            "Exportar Reportes",
            JOptionPane.QUESTION_MESSAGE,
            null,
            opciones,
            opciones[0]
        );
        
        if (seleccion != null) {
            JOptionPane.showMessageDialog(this,
                "🚧 Funcionalidad de exportación en desarrollo...\n\n" +
                "Reporte seleccionado: " + seleccion + "\n\n" +
                "Próximamente incluirá:\n" +
                "• Exportación a PDF con gráficos\n" +
                "• Exportación a Excel con tablas dinámicas\n" +
                "• Exportación a CSV para análisis externo\n" +
                "• Programación de reportes automáticos\n" +
                "• Envío por email",
                "Exportar",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}