package com.fideburguesas.pos.dao;

import com.fideburguesas.pos.config.DatabaseConfig;
import com.fideburguesas.pos.model.Combo;
import com.fideburguesas.pos.model.ComboDetalle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComboDAO {
    private Connection connection;
    
    public ComboDAO() {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }
    
    public Combo save(Combo combo) throws SQLException {
        connection.setAutoCommit(false);
        try {
            String sql;
            if (combo.getId() == null) {
                sql = "INSERT INTO combos (codigo, nombre, descripcion, precio, descuento, activo) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
            } else {
                sql = "UPDATE combos SET codigo = ?, nombre = ?, descripcion = ?, " +
                      "precio = ?, descuento = ?, activo = ? " +
                      "WHERE id = ?";
            }
            
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, combo.getCodigo());
                stmt.setString(2, combo.getNombre());
                stmt.setString(3, combo.getDescripcion());
                stmt.setBigDecimal(4, combo.getPrecio());
                stmt.setBigDecimal(5, combo.getDescuento());
                stmt.setBoolean(6, combo.isActivo());
                
                if (combo.getId() != null) {
                    stmt.setLong(7, combo.getId());
                }
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new SQLException("Crear combo falló, no se insertaron filas.");
                }
                
                if (combo.getId() == null) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            combo.setId(generatedKeys.getLong(1));
                        }
                    }
                }
            }
            
            // Guardar detalles del combo
            if (combo.getId() != null) {
                // Eliminar detalles existentes
                deleteComboDetalles(combo.getId());
                
                // Insertar nuevos detalles
                for (ComboDetalle detalle : combo.getProductos()) {
                    saveComboDetalle(combo.getId(), detalle);
                }
            }
            
            connection.commit();
            return combo;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
    
    private void saveComboDetalle(Long comboId, ComboDetalle detalle) throws SQLException {
        String sql = "INSERT INTO combo_detalles (combo_id, producto_id, cantidad) " +
                     "VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, comboId);
            stmt.setLong(2, detalle.getProductoId());
            stmt.setInt(3, detalle.getCantidad());
            stmt.executeUpdate();
        }
    }
    
    private void deleteComboDetalles(Long comboId) throws SQLException {
        String sql = "DELETE FROM combo_detalles WHERE combo_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, comboId);
            stmt.executeUpdate();
        }
    }
    
    public Optional<Combo> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM combos WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Combo combo = mapResultSetToCombo(rs);
                    combo.setProductos(findComboDetalles(id));
                    return Optional.of(combo);
                }
            }
        }
        
        return Optional.empty();
    }
    
    public Optional<Combo> findByCodigo(String codigo) throws SQLException {
        String sql = "SELECT * FROM combos WHERE codigo = ? AND activo = TRUE";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Combo combo = mapResultSetToCombo(rs);
                    combo.setProductos(findComboDetalles(combo.getId()));
                    return Optional.of(combo);
                }
            }
        }
        
        return Optional.empty();
    }
    
    public List<Combo> findAll() throws SQLException {
        String sql = "SELECT * FROM combos ORDER BY nombre";
        List<Combo> combos = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Combo combo = mapResultSetToCombo(rs);
                combo.setProductos(findComboDetalles(combo.getId()));
                combos.add(combo);
            }
        }
        
        return combos;
    }
    
    public List<Combo> findActivos() throws SQLException {
        String sql = "SELECT * FROM combos WHERE activo = TRUE ORDER BY nombre";
        List<Combo> combos = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Combo combo = mapResultSetToCombo(rs);
                combo.setProductos(findComboDetalles(combo.getId()));
                combos.add(combo);
            }
        }
        
        return combos;
    }
    
    private List<ComboDetalle> findComboDetalles(Long comboId) throws SQLException {
        String sql = "SELECT cd.*, p.nombre as producto_nombre " +
                    "FROM combo_detalles cd " +
                    "JOIN productos p ON cd.producto_id = p.id " +
                    "WHERE cd.combo_id = ?";
        List<ComboDetalle> detalles = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, comboId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ComboDetalle detalle = new ComboDetalle();
                    detalle.setId(rs.getLong("id"));
                    detalle.setComboId(rs.getLong("combo_id"));
                    detalle.setProductoId(rs.getLong("producto_id"));
                    detalle.setProductoNombre(rs.getString("producto_nombre"));
                    detalle.setCantidad(rs.getInt("cantidad"));
                    detalles.add(detalle);
                }
            }
        }
        
        return detalles;
    }
    
    public void delete(Long id) throws SQLException {
        String sql = "UPDATE combos SET activo = FALSE WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
    
    private Combo mapResultSetToCombo(ResultSet rs) throws SQLException {
        Combo combo = new Combo();
        combo.setId(rs.getLong("id"));
        combo.setCodigo(rs.getString("codigo"));
        combo.setNombre(rs.getString("nombre"));
        combo.setDescripcion(rs.getString("descripcion"));
        combo.setPrecio(rs.getBigDecimal("precio"));
        combo.setDescuento(rs.getBigDecimal("descuento"));
        combo.setActivo(rs.getBoolean("activo"));
        
        return combo;
    }
}