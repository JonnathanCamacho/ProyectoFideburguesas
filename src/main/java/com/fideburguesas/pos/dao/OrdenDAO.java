package com.fideburguesas.pos.dao;

import com.fideburguesas.pos.config.DatabaseConfig;
import com.fideburguesas.pos.model.Orden;
import com.fideburguesas.pos.model.OrdenDetalle;

import java.sql.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrdenDAO {
    private Connection connection;
    
    public OrdenDAO() {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }
    
    public Orden save(Orden orden) throws SQLException {
        connection.setAutoCommit(false);
        try {
            String sql;
            if (orden.getId() == null) {
                sql = "INSERT INTO ordenes (numero, fecha, cajero_id, estado, subtotal, " +
                "impuestos, total, observaciones) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            } else {
                sql = "UPDATE ordenes SET numero = ?, fecha = ?, cajero_id = ?, estado = ?, " +
                    "subtotal = ?, impuestos = ?, total = ?, observaciones = ? " +
                    "WHERE id = ?";
            }
            
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, orden.getNumero());
                stmt.setTimestamp(2, Timestamp.valueOf(orden.getFecha()));
                stmt.setLong(3, orden.getCajeroId());
                stmt.setString(4, orden.getEstado().name());
                stmt.setBigDecimal(5, orden.getSubtotal());
                stmt.setBigDecimal(6, orden.getImpuestos());
                stmt.setBigDecimal(7, orden.getTotal());
                stmt.setString(8, orden.getObservaciones());
                
                if (orden.getId() != null) {
                    stmt.setLong(9, orden.getId());
                }
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new SQLException("Crear orden falló, no se insertaron filas.");
                }
                
                if (orden.getId() == null) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            orden.setId(generatedKeys.getLong(1));
                        }
                    }
                }
            }
            
            // Guardar detalles de la orden
            if (orden.getId() != null && orden.getDetalles() != null) {
                // Eliminar detalles existentes
                deleteOrdenDetalles(orden.getId());
                
                // Insertar nuevos detalles
                for (OrdenDetalle detalle : orden.getDetalles()) {
                    saveOrdenDetalle(orden.getId(), detalle);
                }
            }
            
            connection.commit();
            return orden;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
    
    private void saveOrdenDetalle(Long ordenId, OrdenDetalle detalle) throws SQLException {
        String sql = "INSERT INTO orden_detalles (orden_id, producto_id, combo_id, item_nombre, " +
            "cantidad, precio_unitario, subtotal, observaciones) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, ordenId);
            stmt.setObject(2, detalle.getProductoId());
            stmt.setObject(3, detalle.getComboId());
            stmt.setString(4, detalle.getItemNombre());
            stmt.setInt(5, detalle.getCantidad());
            stmt.setBigDecimal(6, detalle.getPrecioUnitario());
            stmt.setBigDecimal(7, detalle.getSubtotal());
            stmt.setString(8, detalle.getObservaciones());
            stmt.executeUpdate();
        }
    }
    
    private void deleteOrdenDetalles(Long ordenId) throws SQLException {
        String sql = "DELETE FROM orden_detalles WHERE orden_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, ordenId);
            stmt.executeUpdate();
        }
    }
    
    public Optional<Orden> findById(Long id) throws SQLException {
        String sql = "SELECT o.*, u.nombre || ' ' || u.apellido as cajero_nombre " +
            "FROM ordenes o " +
            "JOIN usuarios u ON o.cajero_id = u.id " +
            "WHERE o.id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Orden orden = mapResultSetToOrden(rs);
                    orden.setDetalles(findOrdenDetalles(id));
                    return Optional.of(orden);
                }
            }
        }
        
        return Optional.empty();
    }
    
    public Optional<Orden> findByNumero(String numero) throws SQLException {
        String sql = "SELECT o.*, u.nombre || ' ' || u.apellido as cajero_nombre " +
            "FROM ordenes o " +
            "JOIN usuarios u ON o.cajero_id = u.id " +
            "WHERE o.numero = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, numero);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Orden orden = mapResultSetToOrden(rs);
                    orden.setDetalles(findOrdenDetalles(orden.getId()));
                    return Optional.of(orden);
                }
            }
        }
        
        return Optional.empty();
    }
    
    public List<Orden> findAll() throws SQLException {
        String sql = "SELECT o.*, u.nombre || ' ' || u.apellido as cajero_nombre " +
            "FROM ordenes o " +
            "JOIN usuarios u ON o.cajero_id = u.id " +
            "ORDER BY o.fecha DESC";
        
        List<Orden> ordenes = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Orden orden = mapResultSetToOrden(rs);
                orden.setDetalles(findOrdenDetalles(orden.getId()));
                ordenes.add(orden);
            }
        }
        
        return ordenes;
    }
    
    public List<Orden> findByEstado(Orden.EstadoOrden estado) throws SQLException {
        String sql = "SELECT o.*, u.nombre || ' ' || u.apellido as cajero_nombre " +
            "FROM ordenes o " +
            "JOIN usuarios u ON o.cajero_id = u.id " +
            "WHERE o.estado = ? " +
            "ORDER BY o.fecha ASC";
        List<Orden> ordenes = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, estado.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Orden orden = mapResultSetToOrden(rs);
                    orden.setDetalles(findOrdenDetalles(orden.getId()));
                    ordenes.add(orden);
                }
            }
        }
        
        return ordenes;
    }
    
    public List<Orden> findByFecha(LocalDate fecha) throws SQLException {
        String sql = "SELECT o.*, u.nombre || ' ' || u.apellido as cajero_nombre " +
            "FROM ordenes o " +
            "JOIN usuarios u ON o.cajero_id = u.id " +
            "WHERE o.fecha >= ? AND o.fecha < ? " +  // CAMBIO AQUÍ
            "ORDER BY o.fecha DESC";

        List<Orden> ordenes = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Convertir LocalDate a rango de timestamps
            Timestamp inicioDelDia = Timestamp.valueOf(fecha.atStartOfDay());
            Timestamp finDelDia = Timestamp.valueOf(fecha.plusDays(1).atStartOfDay());

            stmt.setTimestamp(1, inicioDelDia);    // CAMBIO AQUÍ
            stmt.setTimestamp(2, finDelDia);       // CAMBIO AQUÍ

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Orden orden = mapResultSetToOrden(rs);
                    orden.setDetalles(findOrdenDetalles(orden.getId()));
                    ordenes.add(orden);
                }
            }
        }

        return ordenes;
    }
    
    public List<Orden> findOrdenesPendientesCocina() throws SQLException {
        String sql = "SELECT o.*, u.nombre || ' ' || u.apellido as cajero_nombre " +
            "FROM ordenes o " +
            "JOIN usuarios u ON o.cajero_id = u.id " +
            "WHERE o.estado IN ('PENDIENTE', 'EN_PREPARACION') " +
            "ORDER BY o.fecha ASC";
        List<Orden> ordenes = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Orden orden = mapResultSetToOrden(rs);
                orden.setDetalles(findOrdenDetalles(orden.getId()));
                ordenes.add(orden);
            }
        }
        
        return ordenes;
    }
    
    private List<OrdenDetalle> findOrdenDetalles(Long ordenId) throws SQLException {
        String sql = "SELECT * FROM orden_detalles WHERE orden_id = ?";
        List<OrdenDetalle> detalles = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, ordenId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrdenDetalle detalle = new OrdenDetalle();
                    detalle.setId(rs.getLong("id"));
                    detalle.setOrdenId(rs.getLong("orden_id"));
                    detalle.setProductoId((Long) rs.getObject("producto_id"));
                    detalle.setComboId((Long) rs.getObject("combo_id"));
                    detalle.setItemNombre(rs.getString("item_nombre"));
                    detalle.setCantidad(rs.getInt("cantidad"));
                    detalle.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    detalle.setSubtotal(rs.getBigDecimal("subtotal"));
                    detalle.setObservaciones(rs.getString("observaciones"));
                    detalles.add(detalle);
                }
            }
        }
        
        return detalles;
    }
    
    public void updateEstado(Long ordenId, Orden.EstadoOrden nuevoEstado) throws SQLException {
        String sql = "UPDATE ordenes SET estado = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nuevoEstado.name());
            stmt.setLong(2, ordenId);
            stmt.executeUpdate();
        }
    }
    
    public String generateNumeroOrden() throws SQLException {
        String sql = "SELECT COUNT(*) + 1 as siguiente FROM ordenes " +
                    "WHERE fecha >= ? AND fecha < ?";  // SIN el alias "o"

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            LocalDate hoy = LocalDate.now();
            Timestamp inicioDelDia = Timestamp.valueOf(hoy.atStartOfDay());
            Timestamp finDelDia = Timestamp.valueOf(hoy.plusDays(1).atStartOfDay());

            stmt.setTimestamp(1, inicioDelDia);
            stmt.setTimestamp(2, finDelDia);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int siguiente = rs.getInt("siguiente");
                    return String.format("ORD-%s-%04d", 
                        LocalDate.now().toString().replace("-", ""), siguiente);
                }
            }
        }

        return "ORD-" + System.currentTimeMillis();
    }
    
    private Orden mapResultSetToOrden(ResultSet rs) throws SQLException {
        Orden orden = new Orden();
        orden.setId(rs.getLong("id"));
        orden.setNumero(rs.getString("numero"));
        
        Timestamp fecha = rs.getTimestamp("fecha");
        if (fecha != null) {
            orden.setFecha(fecha.toLocalDateTime());
        }
        
        orden.setCajeroId(rs.getLong("cajero_id"));
        orden.setCajeroNombre(rs.getString("cajero_nombre"));
        orden.setEstado(Orden.EstadoOrden.valueOf(rs.getString("estado")));
        orden.setSubtotal(rs.getBigDecimal("subtotal"));
        orden.setImpuestos(rs.getBigDecimal("impuestos"));
        orden.setTotal(rs.getBigDecimal("total"));
        orden.setObservaciones(rs.getString("observaciones"));
        
        return orden;
    }
}