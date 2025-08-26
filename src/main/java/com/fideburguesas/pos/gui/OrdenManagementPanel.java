package com.fideburguesas.pos.gui;

import com.fideburguesas.pos.service.OrdenService;
import com.fideburguesas.pos.service.UsuarioService;
import com.fideburguesas.pos.model.Orden;
import com.fideburguesas.pos.model.Usuario;
import com.fideburguesas.pos.util.CurrencyUtil;
import com.fideburguesas.pos.util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

public class OrdenManagementPanel extends JPanel {
    private OrdenService ordenService;
    private UsuarioService usuarioService;
    private JTable ordenesTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    
    // Botones principales
    private JButton crearOrdenButton, verDetallesButton, cambiarEstadoButton, cancelarButton, refreshButton;
    private JButton reimprimirButton, reportesButton, exportButton;
    
    // Filtros y búsqueda
    private JTextField searchField;
    private JComboBox<String> estadoFilter, cajeroFilter;
    private JComboBox<String> periodoFilter;
    private JCheckBox soloHoyFilter;
    
    // Información y estadísticas
    private JLabel totalOrdenesLabel, ventasHoyLabel, ordenesHoyLabel;
    private JLabel promedioOrdenLabel, estadoDistribucionLabel;
    
    public OrdenManagementPanel() {
        this.ordenService = new OrdenService();
        this.usuarioService = new UsuarioService();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadCajeros();
        loadOrdenes();
        updateStatistics();
    }
    
    private void initializeComponents() {
        // Tabla de órdenes
        String[] columnNames = {
            "ID", "Número", "Fecha", "Cajero", "Estado", 
            "Items", "Subtotal", "Impuestos", "Total", "Acciones"
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
                    case 5: return Integer.class; // Items
                    case 6: case 7: case 8: return String.class; // Montos (formateados)
                    default: return String.class;
                }
            }
        };
        
        ordenesTable = new JTable(tableModel);
        ordenesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ordenesTable.setRowHeight(35);
        ordenesTable.getTableHeader().setBackground(new Color(34, 139, 34)); // Verde para órdenes
        ordenesTable.getTableHeader().setForeground(Color.BLACK);
        ordenesTable.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Configurar anchos de columnas
        ordenesTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        ordenesTable.getColumnModel().getColumn(1).setPreferredWidth(150);  // Número
        ordenesTable.getColumnModel().getColumn(2).setPreferredWidth(130);  // Fecha
        ordenesTable.getColumnModel().getColumn(3).setPreferredWidth(120);  // Cajero
        ordenesTable.getColumnModel().getColumn(4).setPreferredWidth(120);  // Estado
        ordenesTable.getColumnModel().getColumn(5).setPreferredWidth(60);   // Items
        ordenesTable.getColumnModel().getColumn(6).setPreferredWidth(100);  // Subtotal
        ordenesTable.getColumnModel().getColumn(7).setPreferredWidth(100);  // Impuestos
        ordenesTable.getColumnModel().getColumn(8).setPreferredWidth(100);  // Total
        ordenesTable.getColumnModel().getColumn(9).setPreferredWidth(80);   // Acciones
        
        // Row sorter para filtros
        rowSorter = new TableRowSorter<>(tableModel);
        ordenesTable.setRowSorter(rowSorter);
        
        // Botones principales
        crearOrdenButton = createStyledButton("➕ Nueva Orden", new Color(60, 179, 113), Color.BLACK);
        verDetallesButton = createStyledButton("🔍 Ver Detalles", new Color(173, 216, 230), new Color(0, 0, 139));
        cambiarEstadoButton = createStyledButton("🔄 Cambiar Estado", new Color(255, 215, 0), new Color(184, 134, 11));
        cancelarButton = createStyledButton("❌ Cancelar Orden", new Color(255, 182, 193), new Color(139, 0, 0));
        refreshButton = createStyledButton("🔄 Actualizar", new Color(211, 211, 211), new Color(105, 105, 105));
        
        // Botones adicionales
        reimprimirButton = createStyledButton("🖨️ Reimprimir", new Color(221, 160, 221), new Color(139, 69, 19));
        reportesButton = createStyledButton("📊 Reportes", new Color(144, 238, 144), new Color(0, 100, 0));
        exportButton = createStyledButton("📤 Exportar", new Color(176, 196, 222), new Color(25, 25, 112));
        
        // Filtros y búsqueda
        searchField = new JTextField(15);
        searchField.setToolTipText("Buscar por número de orden");
        
        estadoFilter = new JComboBox<>(new String[]{
            "Todos los estados", "PENDIENTE", "EN_PREPARACION", "LISTA", "ENTREGADA", "CANCELADA"
        });
        
        cajeroFilter = new JComboBox<>();
        
        periodoFilter = new JComboBox<>(new String[]{
            "Hoy", "Ayer", "Esta semana", "Este mes", "Últimos 30 días", "Personalizado"
        });
        
        soloHoyFilter = new JCheckBox("Solo hoy", true);
        
        // Labels de estadísticas
        totalOrdenesLabel = new JLabel("Total: 0");
        ventasHoyLabel = new JLabel("Ventas hoy: ₡0.00");
        ordenesHoyLabel = new JLabel("Órdenes hoy: 0");
        promedioOrdenLabel = new JLabel("Promedio: ₡0.00");
        estadoDistribucionLabel = new JLabel("Pendientes: 0 | En preparación: 0 | Listas: 0");
        
        // Estilos para labels de estadísticas
        Font statsFont = new Font("Arial", Font.BOLD, 12);
        totalOrdenesLabel.setFont(statsFont);
        ventasHoyLabel.setFont(statsFont);
        ventasHoyLabel.setForeground(new Color(0, 128, 0));
        ordenesHoyLabel.setFont(statsFont);
        ordenesHoyLabel.setForeground(new Color(34, 139, 34));
        promedioOrdenLabel.setFont(statsFont);
        promedioOrdenLabel.setForeground(new Color(0, 0, 139));
        estadoDistribucionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        estadoDistribucionLabel.setForeground(Color.GRAY);
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
        leftPanel.setPreferredSize(new Dimension(280, 0));
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
        JLabel titleLabel = new JLabel("📋 Gestión de Órdenes");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(34, 139, 34));
        
        // Panel de estadísticas principales
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.add(totalOrdenesLabel);
        statsPanel.add(new JLabel(" | "));
        statsPanel.add(ordenesHoyLabel);
        statsPanel.add(new JLabel(" | "));
        statsPanel.add(ventasHoyLabel);
        statsPanel.add(new JLabel(" | "));
        statsPanel.add(promedioOrdenLabel);
        
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
        
        // Búsqueda por número
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Número orden:"), gbc);
        gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(searchField, gbc);
        
        // Estado
        gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Estado:"), gbc);
        gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(estadoFilter, gbc);
        
        // Cajero
        gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Cajero:"), gbc);
        gbc.gridy = 5; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(cajeroFilter, gbc);
        
        // Período
        gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Período:"), gbc);
        gbc.gridy = 7; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(periodoFilter, gbc);
        
        // Checkbox solo hoy
        gbc.gridy = 8; gbc.fill = GridBagConstraints.NONE;
        panel.add(soloHoyFilter, gbc);
        
        // Botón limpiar filtros
        JButton clearFiltersButton = new JButton("🧹 Limpiar");
        clearFiltersButton.setFont(new Font("Arial", Font.PLAIN, 10));
        clearFiltersButton.setForeground(Color.BLACK);
        clearFiltersButton.addActionListener(e -> clearFilters());
        gbc.gridy = 9; gbc.fill = GridBagConstraints.HORIZONTAL;
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
        
        // Botón CREAR ORDEN destacado
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(crearOrdenButton, gbc);
        
        // Separador
        gbc.gridy = 1;
        panel.add(new JSeparator(), gbc);
        
        // Botones principales
        gbc.gridy = 2;
        panel.add(verDetallesButton, gbc);
        gbc.gridy = 3;
        panel.add(cambiarEstadoButton, gbc);
        gbc.gridy = 4;
        panel.add(cancelarButton, gbc);
        gbc.gridy = 5;
        panel.add(refreshButton, gbc);
        
        // Separador
        gbc.gridy = 6;
        panel.add(new JSeparator(), gbc);
        
        // Botones adicionales
        gbc.gridy = 7;
        panel.add(reimprimirButton, gbc);
        gbc.gridy = 8;
        panel.add(reportesButton, gbc);
        gbc.gridy = 9;
        panel.add(exportButton, gbc);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Tabla con scroll
        JScrollPane scrollPane = new JScrollPane(ordenesTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Órdenes"));
        scrollPane.setBackground(Color.WHITE);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createTitledBorder("Resumen"));
        
        // Panel izquierdo con ayuda
        JLabel helpLabel = new JLabel("💡 Doble clic para ver detalles | Estados: Pendiente → En preparación → Lista → Entregada");
        helpLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        helpLabel.setForeground(Color.GRAY);
        
        // Panel derecho con distribución de estados
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(new Color(245, 245, 245));
        rightPanel.add(estadoDistribucionLabel);
        
        panel.add(helpLabel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void setupEventListeners() {
        // Botones principales
        crearOrdenButton.addActionListener(e -> crearNuevaOrden());
        verDetallesButton.addActionListener(e -> verDetallesOrden());
        cambiarEstadoButton.addActionListener(e -> cambiarEstadoOrden());
        cancelarButton.addActionListener(e -> cancelarOrden());
        refreshButton.addActionListener(e -> { loadOrdenes(); updateStatistics(); });
        
        // Botones adicionales
        reimprimirButton.addActionListener(e -> reimprimirOrden());
        reportesButton.addActionListener(e -> showReportsDialog());
        exportButton.addActionListener(e -> exportOrdenes());
        
        // Filtros
        searchField.addActionListener(e -> applyFilters());
        estadoFilter.addActionListener(e -> applyFilters());
        cajeroFilter.addActionListener(e -> applyFilters());
        periodoFilter.addActionListener(e -> applyFilters());
        soloHoyFilter.addActionListener(e -> applyFilters());
        
        // Doble clic en tabla
        ordenesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    verDetallesOrden();
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
    
    private void loadCajeros() {
        try {
            List<Usuario> cajeros = usuarioService.obtenerUsuariosPorTipo(Usuario.TipoUsuario.CAJERO);
            
            cajeroFilter.removeAllItems();
            cajeroFilter.addItem("Todos los cajeros");
            
            for (Usuario cajero : cajeros) {
                cajeroFilter.addItem(cajero.getNombreCompleto());
            }
        } catch (SQLException e) {
            showError("Error al cargar cajeros: " + e.getMessage());
        }
    }
    
    private void loadOrdenes() {
        try {
            List<Orden> ordenes;
            
            if (soloHoyFilter.isSelected()) {
                ordenes = ordenService.obtenerOrdenesPorFecha(LocalDate.now());
            } else {
                ordenes = ordenService.obtenerTodasLasOrdenes();
            }
            
            tableModel.setRowCount(0);
            
            for (Orden orden : ordenes) {
                // Determinar color/estado visual
                String estadoDisplay = getEstadoDisplay(orden.getEstado());
                
                Object[] row = {
                    orden.getId(),
                    orden.getNumero(),
                    DateUtil.formatDateTime(orden.getFecha()),
                    orden.getCajeroNombre() != null ? orden.getCajeroNombre() : "N/A",
                    estadoDisplay,
                    orden.getDetalles() != null ? orden.getDetalles().size() : 0,
                    CurrencyUtil.formatCurrency(orden.getSubtotal()),
                    CurrencyUtil.formatCurrency(orden.getImpuestos()),
                    CurrencyUtil.formatCurrency(orden.getTotal()),
                    "Ver"
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            showError("Error al cargar órdenes: " + e.getMessage());
        }
    }
    
    private String getEstadoDisplay(Orden.EstadoOrden estado) {
        switch (estado) {
            case PENDIENTE:
                return "🟡 Pendiente";
            case EN_PREPARACION:
                return "🟠 En preparación";
            case LISTA:
                return "🟢 Lista";
            case ENTREGADA:
                return "✅ Entregada";
            case CANCELADA:
                return "❌ Cancelada";
            default:
                return estado.toString();
        }
    }
    
    private void updateStatistics() {
        try {
            // Órdenes de hoy
            List<Orden> ordenesHoy = ordenService.obtenerOrdenesPorFecha(LocalDate.now());
            
            // Todas las órdenes (para el filtro actual)
            List<Orden> todasOrdenes = ordenService.obtenerTodasLasOrdenes();
            
            // Calcular estadísticas
            int totalOrdenes = todasOrdenes.size();
            int ordenesHoyCount = ordenesHoy.size();
            
            // Ventas de hoy
            BigDecimal ventasHoy = ordenesHoy.stream()
                .filter(o -> o.getEstado() == Orden.EstadoOrden.ENTREGADA)
                .map(Orden::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Promedio por orden
            double promedioOrden = ordenesHoy.stream()
                .filter(o -> o.getEstado() == Orden.EstadoOrden.ENTREGADA)
                .mapToDouble(o -> o.getTotal().doubleValue())
                .average()
                .orElse(0.0);
            
            // Distribución por estados (órdenes de hoy)
            long pendientes = ordenesHoy.stream().filter(o -> o.getEstado() == Orden.EstadoOrden.PENDIENTE).count();
            long enPreparacion = ordenesHoy.stream().filter(o -> o.getEstado() == Orden.EstadoOrden.EN_PREPARACION).count();
            long listas = ordenesHoy.stream().filter(o -> o.getEstado() == Orden.EstadoOrden.LISTA).count();
            
            // Actualizar labels
            totalOrdenesLabel.setText("Total: " + totalOrdenes);
            ordenesHoyLabel.setText("Órdenes hoy: " + ordenesHoyCount);
            ventasHoyLabel.setText("Ventas hoy: " + CurrencyUtil.formatCurrency(ventasHoy));
            promedioOrdenLabel.setText("Promedio: " + CurrencyUtil.formatCurrency(promedioOrden));
            estadoDistribucionLabel.setText(String.format(
                "Pendientes: %d | En preparación: %d | Listas: %d", 
                pendientes, enPreparacion, listas));
            
        } catch (SQLException e) {
            showError("Error al calcular estadísticas: " + e.getMessage());
        }
    }
    
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedEstado = (String) estadoFilter.getSelectedItem();
        String selectedCajero = (String) cajeroFilter.getSelectedItem();
        
        rowSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                // Filtro de búsqueda por número
                if (!searchText.isEmpty()) {
                    String numero = entry.getStringValue(1).toLowerCase();
                    if (!numero.contains(searchText)) {
                        return false;
                    }
                }
                
                // Filtro de estado
                if (selectedEstado != null && !selectedEstado.equals("Todos los estados")) {
                    String estado = entry.getStringValue(4);
                    if (!estado.contains(selectedEstado.replace("_", " ").toLowerCase())) {
                        return false;
                    }
                }
                
                // Filtro de cajero
                if (selectedCajero != null && !selectedCajero.equals("Todos los cajeros")) {
                    String cajero = entry.getStringValue(3);
                    if (!cajero.equals(selectedCajero)) {
                        return false;
                    }
                }
                
                return true;
            }
        });
    }
    
    private void clearFilters() {
        searchField.setText("");
        estadoFilter.setSelectedIndex(0);
        cajeroFilter.setSelectedIndex(0);
        periodoFilter.setSelectedIndex(0);
        soloHoyFilter.setSelected(true);
        applyFilters();
    }
    
    // ======= NUEVA FUNCIONALIDAD: CREAR ORDEN =======
    private void crearNuevaOrden() {
        // Verificar que hay usuario logueado
        Usuario usuarioActual = usuarioService.getUsuarioActual();
        if (usuarioActual == null) {
            // Si no hay usuario logueado desde admin, permitir seleccionar cajero
            showCajeroSelectionDialog();
            return;
        }

        OrdenDialog dialog = new OrdenDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), 
            "Crear Nueva Orden", 
            null
        );
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                Orden orden = dialog.getOrden();
                orden.setCajeroId(usuarioActual.getId());

                Orden ordenGuardada = ordenService.guardarOrden(orden);
                loadOrdenes();
                updateStatistics();
                showSuccess("Orden #" + ordenGuardada.getNumero() + " creada exitosamente\n" +
                           "Total: " + CurrencyUtil.formatCurrency(ordenGuardada.getTotal()));
            } catch (Exception e) {
                showError("Error al crear orden: " + e.getMessage());
            }
        }
    }
    
    private void showCajeroSelectionDialog() {
        try {
            List<Usuario> cajeros = usuarioService.obtenerUsuariosPorTipo(Usuario.TipoUsuario.CAJERO);

            if (cajeros.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No hay cajeros registrados en el sistema.\n" +
                    "Debe crear al menos un usuario de tipo CAJERO para crear órdenes.");
                return;
            }

            // Crear array para el JOptionPane
            Usuario[] caserosArray = cajeros.toArray(new Usuario[0]);

            Usuario cajeroSeleccionado = (Usuario) JOptionPane.showInputDialog(
                this,
                "Seleccione el cajero que creará la orden:",
                "Seleccionar Cajero",
                JOptionPane.QUESTION_MESSAGE,
                null,
                caserosArray,
                caserosArray[0]
            );

            if (cajeroSeleccionado != null) {
                // Continuar con la creación de orden
                crearOrdenConCajero(cajeroSeleccionado);
            }

        } catch (SQLException e) {
            showError("Error al cargar cajeros: " + e.getMessage());
        }
    }
    
    private void crearOrdenConCajero(Usuario cajero) {
        OrdenDialog dialog = new OrdenDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), 
            "Crear Nueva Orden - Cajero: " + cajero.getNombreCompleto(), 
            null
        );
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                Orden orden = dialog.getOrden();
                orden.setCajeroId(cajero.getId());

                Orden ordenGuardada = ordenService.guardarOrden(orden);
                loadOrdenes();
                updateStatistics();
                showSuccess("Orden #" + ordenGuardada.getNumero() + " creada exitosamente\n" +
                           "Cajero: " + cajero.getNombreCompleto() + "\n" +
                           "Total: " + CurrencyUtil.formatCurrency(ordenGuardada.getTotal()));
            } catch (Exception e) {
                showError("Error al crear orden: " + e.getMessage());
            }
        }
    }
    
    private void verDetallesOrden() {
        int selectedRow = ordenesTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Seleccione una orden para ver detalles");
            return;
        }
        
        int modelRow = ordenesTable.convertRowIndexToModel(selectedRow);
        Long ordenId = (Long) tableModel.getValueAt(modelRow, 0);
        
        try {
            var orden = ordenService.buscarPorId(ordenId);
            if (orden.isPresent()) {
                OrdenDetailsDialog dialog = new OrdenDetailsDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    orden.get()
                );
                dialog.setVisible(true);
            }
        } catch (SQLException e) {
            showError("Error al cargar detalles: " + e.getMessage());
        }
    }
    
    private void cambiarEstadoOrden() {
        int selectedRow = ordenesTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Seleccione una orden para cambiar estado");
            return;
        }
        
        int modelRow = ordenesTable.convertRowIndexToModel(selectedRow);
        Long ordenId = (Long) tableModel.getValueAt(modelRow, 0);
        String numeroOrden = (String) tableModel.getValueAt(modelRow, 1);
        
        // Mostrar diálogo para seleccionar nuevo estado
        Orden.EstadoOrden[] estados = Orden.EstadoOrden.values();
        Orden.EstadoOrden nuevoEstado = (Orden.EstadoOrden) JOptionPane.showInputDialog(
            this,
            "Seleccione el nuevo estado para la orden " + numeroOrden + ":",
            "Cambiar Estado",
            JOptionPane.QUESTION_MESSAGE,
            null,
            estados,
            estados[0]
        );
        
        if (nuevoEstado != null) {
            try {
                ordenService.cambiarEstadoOrden(ordenId, nuevoEstado);
                loadOrdenes();
                updateStatistics();
                showSuccess("Estado de orden actualizado exitosamente");
            } catch (SQLException e) {
                showError("Error al cambiar estado: " + e.getMessage());
            }
        }
    }
    
    private void cancelarOrden() {
        int selectedRow = ordenesTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Seleccione una orden para cancelar");
            return;
        }
        
        int modelRow = ordenesTable.convertRowIndexToModel(selectedRow);
        Long ordenId = (Long) tableModel.getValueAt(modelRow, 0);
        String numeroOrden = (String) tableModel.getValueAt(modelRow, 1);
        
        int option = JOptionPane.showConfirmDialog(this,
            "¿Está seguro que desea cancelar la orden?\n\n" +
            "Orden: " + numeroOrden + "\n" +
            "Nota: Esta acción devolverá el stock de los productos.",
            "Confirmar Cancelación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            try {
                ordenService.cancelarOrden(ordenId);
                loadOrdenes();
                updateStatistics();
                showSuccess("Orden cancelada exitosamente");
            } catch (SQLException e) {
                showError("Error al cancelar orden: " + e.getMessage());
            }
        }
    }
    
    private void reimprimirOrden() {
        int selectedRow = ordenesTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Seleccione una orden para reimprimir");
            return;
        }
        
        int modelRow = ordenesTable.convertRowIndexToModel(selectedRow);
        Long ordenId = (Long) tableModel.getValueAt(modelRow, 0);
        
        try {
            var orden = ordenService.buscarPorId(ordenId);
            if (orden.isPresent()) {
                // Aquí llamarías al método de impresión
                showInfo("Funcionalidad de reimpresión en desarrollo...\n" +
                        "Próximamente se podrá reimprimir tickets de órdenes.");
            }
        } catch (SQLException e) {
            showError("Error al reimprimir: " + e.getMessage());
        }
    }
    
    private void showReportsDialog() {
        JOptionPane.showMessageDialog(this, 
            "📊 Reportes de Órdenes\n\nFuncionalidad en desarrollo...\n\n" +
            "Próximamente incluirá:\n" +
            "• Reporte de ventas diarias\n" +
            "• Análisis por cajero\n" +
            "• Productos más vendidos\n" +
            "• Tendencias de ventas\n" +
            "• Comparativas por período",
            "Reportes",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exportOrdenes() {
        JOptionPane.showMessageDialog(this, 
            "📤 Exportar Órdenes\n\nFuncionalidad en desarrollo...\n\n" +
            "Próximamente incluirá:\n" +
            "• Exportar a Excel\n" +
            "• Exportar a PDF\n" +
            "• Exportar a CSV\n" +
            "• Filtros personalizados",
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
    
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
}