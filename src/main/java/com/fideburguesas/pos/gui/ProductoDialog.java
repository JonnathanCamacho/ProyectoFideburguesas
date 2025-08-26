package com.fideburguesas.pos.gui;

import com.fideburguesas.pos.model.Producto;
import com.fideburguesas.pos.model.Categoria;
import com.fideburguesas.pos.service.CategoriaService;
import com.fideburguesas.pos.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ProductoDialog extends JDialog {
    private JTextField codigoField, nombreField, descripcionField, precioField;
    private JTextField stockField, stockMinimoField, imagenField;
    private JComboBox<Categoria> categoriaComboBox;
    private JCheckBox activoCheckBox;
    private boolean confirmed = false;
    private Producto producto;
    private CategoriaService categoriaService;
    
    public ProductoDialog(JFrame parent, String title, Producto producto) {
        super(parent, title, true);
        this.producto = producto;
        this.categoriaService = new CategoriaService();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        setDefaultConfiguration();
        loadCategorias();
        
        if (producto != null) {
            loadProductoData();
        }
    }
    
    private void initializeComponents() {
        codigoField = new JTextField(20);
        nombreField = new JTextField(20);
        descripcionField = new JTextField(20);
        precioField = new JTextField(20);
        stockField = new JTextField(20);
        stockMinimoField = new JTextField(20);
        imagenField = new JTextField(20);
        categoriaComboBox = new JComboBox<>();
        activoCheckBox = new JCheckBox("Activo", true);
        
        // Configurar campos numéricos
        stockField.setText("0");
        stockMinimoField.setText("5");
        precioField.setText("0.00");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Código
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Código:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(codigoField, gbc);
        
        // Nombre
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(nombreField, gbc);
        
        // Descripción
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(descripcionField, gbc);
        
        // Precio
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Precio:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(precioField, gbc);
        
        // Categoría
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Categoría:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(categoriaComboBox, gbc);
        
        // Stock
        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Stock:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(stockField, gbc);
        
        // Stock Mínimo
        gbc.gridx = 0; gbc.gridy = 6; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Stock Mínimo:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(stockMinimoField, gbc);
        
        // Imagen
        gbc.gridx = 0; gbc.gridy = 7; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Imagen:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        JPanel imagenPanel = new JPanel(new BorderLayout());
        imagenPanel.add(imagenField, BorderLayout.CENTER);
        JButton browsePicBtn = new JButton("📁");
        browsePicBtn.setToolTipText("Seleccionar imagen");
        browsePicBtn.addActionListener(e -> selectImage());
        imagenPanel.add(browsePicBtn, BorderLayout.EAST);
        formPanel.add(imagenPanel, gbc);
        
        // Activo
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(activoCheckBox, gbc);
        
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
        
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Event listeners
        saveButton.addActionListener(e -> save());
        cancelButton.addActionListener(e -> cancel());
    }
    
    private void setupEventListeners() {
        // Enter key para guardar (solo en algunos campos)
        codigoField.addActionListener(e -> nombreField.requestFocus());
        nombreField.addActionListener(e -> descripcionField.requestFocus());
        precioField.addActionListener(e -> save());
    }
    
    private void loadCategorias() {
        try {
            List<Categoria> categorias = categoriaService.obtenerCategoriasActivas();
            categoriaComboBox.removeAllItems();
            
            for (Categoria categoria : categorias) {
                categoriaComboBox.addItem(categoria);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar categorías: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadProductoData() {
        codigoField.setText(producto.getCodigo());
        nombreField.setText(producto.getNombre());
        descripcionField.setText(producto.getDescripcion());
        precioField.setText(producto.getPrecio().toString());
        stockField.setText(producto.getStock().toString());
        stockMinimoField.setText(producto.getStockMinimo().toString());
        imagenField.setText(producto.getImagen());
        activoCheckBox.setSelected(producto.isActivo());
        
        // Seleccionar categoría
        for (int i = 0; i < categoriaComboBox.getItemCount(); i++) {
            Categoria cat = categoriaComboBox.getItemAt(i);
            if (cat.getId().equals(producto.getCategoriaId())) {
                categoriaComboBox.setSelectedIndex(i);
                break;
            }
        }
    }
    
    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Imágenes", "jpg", "jpeg", "png", "gif"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            imagenField.setText(fileChooser.getSelectedFile().getAbsolutePath());
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
        
        if (!ValidationUtil.isValidCode(codigoField.getText())) {
            showFieldError("El código debe tener entre 3-10 caracteres alfanuméricos", codigoField);
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
        
        try {
            BigDecimal precio = new BigDecimal(precioField.getText());
            if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                showFieldError("El precio debe ser mayor a cero", precioField);
                return false;
            }
        } catch (NumberFormatException e) {
            showFieldError("El precio debe ser un número válido", precioField);
            return false;
        }
        
        // Validar stock
        if (!ValidationUtil.isValidInteger(stockField.getText())) {
            showFieldError("El stock debe ser un número entero válido", stockField);
            return false;
        }
        
        // Validar stock mínimo
        if (!ValidationUtil.isValidInteger(stockMinimoField.getText())) {
            showFieldError("El stock mínimo debe ser un número entero válido", stockMinimoField);
            return false;
        }
        
        // Validar categoría
        if (categoriaComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una categoría");
            return false;
        }
        
        return true;
    }
    
    private void showFieldError(String message, JTextField field) {
        JOptionPane.showMessageDialog(this, message);
        field.requestFocus();
        field.selectAll();
    }
    
    public Producto getProducto() {
        Producto prod = new Producto();
        prod.setCodigo(ValidationUtil.cleanString(codigoField.getText()).toUpperCase());
        prod.setNombre(ValidationUtil.cleanString(nombreField.getText()));
        prod.setDescripcion(ValidationUtil.cleanString(descripcionField.getText()));
        prod.setPrecio(new BigDecimal(precioField.getText()));
        prod.setStock(Integer.parseInt(stockField.getText()));
        prod.setStockMinimo(Integer.parseInt(stockMinimoField.getText()));
        prod.setImagen(ValidationUtil.cleanString(imagenField.getText()));
        prod.setActivo(activoCheckBox.isSelected());
        
        Categoria selectedCategoria = (Categoria) categoriaComboBox.getSelectedItem();
        if (selectedCategoria != null) {
            prod.setCategoriaId(selectedCategoria.getId());
        }
        
        return prod;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    private void setDefaultConfiguration() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setResizable(false);
        codigoField.requestFocus();
    }
}