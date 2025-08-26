package com.fideburguesas.pos.dao;

import com.fideburguesas.pos.config.DatabaseConfig;
import com.fideburguesas.pos.model.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoDAO {
    private Connection connection;
    
    public ProductoDAO() {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }
    
    public Producto save(Producto producto) throws SQLException {
        String sql;
        if (producto.getId() == null) {
            sql = "INSERT INTO productos (codigo, nombre, descripcion, precio, categoria_id, " +
                  "stock, stock_minimo, activo, imagen) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE productos SET codigo = ?, nombre = ?, descripcion = ?, " +
                  "precio = ?, categoria_id = ?, stock = ?, " +
                  "stock_minimo = ?, activo = ?, imagen = ? " +
                  "WHERE id = ?";
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, producto.getCodigo());
            stmt.setString(2, producto.getNombre());
            stmt.setString(3, producto.getDescripcion());
            stmt.setBigDecimal(4, producto.getPrecio());
            stmt.setLong(5, producto.getCategoriaId());
            stmt.setInt(6, producto.getStock());
            stmt.setInt(7, producto.getStockMinimo());
            stmt.setBoolean(8, producto.isActivo());
            stmt.setString(9, producto.getImagen());
            
            if (producto.getId() != null) {
                stmt.setLong(10, producto.getId());
            }
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Crear producto falló, no se insertaron filas.");
            }
            
            if (producto.getId() == null) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        producto.setId(generatedKeys.getLong(1));
                    }
                }
            }
        }
        
        return producto;
    }
    
    public Optional<Producto> findById(Long id) throws SQLException {
        String sql = "SELECT p.*, c.nombre as categoria_nombre " +
                    "FROM productos p " +
                    "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                    "WHERE p.id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProducto(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    public Optional<Producto> findByCodigo(String codigo) throws SQLException {
        String sql = "SELECT p.*, c.nombre as categoria_nombre " +
                    "FROM productos p " +
                    "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                    "WHERE p.codigo = ? AND p.activo = TRUE";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProducto(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    public List<Producto> findAll() throws SQLException {
        String sql = "SELECT p.*, c.nombre as categoria_nombre " +
                    "FROM productos p " +
                    "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                    "ORDER BY p.nombre";
        List<Producto> productos = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                productos.add(mapResultSetToProducto(rs));
            }
        }
        
        return productos;
    }
    
    public List<Producto> findByCategoria(Long categoriaId) throws SQLException {
        String sql = "SELECT p.*, c.nombre as categoria_nombre " +
                    "FROM productos p " +
                    "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                    "WHERE p.categoria_id = ? AND p.activo = TRUE " +
                    "ORDER BY p.nombre";
        List<Producto> productos = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, categoriaId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapResultSetToProducto(rs));
                }
            }
        }
        
        return productos;
    }
    
    public List<Producto> findProductosBajoStock() throws SQLException {
        String sql = "SELECT p.*, c.nombre as categoria_nombre " +
                    "FROM productos p " +
                    "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                    "WHERE p.stock <= p.stock_minimo AND p.activo = TRUE " +
                    "ORDER BY p.stock ASC";
        List<Producto> productos = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                productos.add(mapResultSetToProducto(rs));
            }
        }
        
        return productos;
    }
    
    public void actualizarStock(Long productoId, int cantidad) throws SQLException {
        String sql = "UPDATE productos SET stock = stock + ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cantidad);
            stmt.setLong(2, productoId);
            stmt.executeUpdate();
        }
    }
    
    public void delete(Long id) throws SQLException {
        String sql = "UPDATE productos SET activo = FALSE WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
    
    private Producto mapResultSetToProducto(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setId(rs.getLong("id"));
        producto.setCodigo(rs.getString("codigo"));
        producto.setNombre(rs.getString("nombre"));
        producto.setDescripcion(rs.getString("descripcion"));
        producto.setPrecio(rs.getBigDecimal("precio"));
        producto.setCategoriaId(rs.getLong("categoria_id"));
        producto.setCategoria(rs.getString("categoria_nombre"));
        producto.setStock(rs.getInt("stock"));
        producto.setStockMinimo(rs.getInt("stock_minimo"));
        producto.setActivo(rs.getBoolean("activo"));
        producto.setImagen(rs.getString("imagen"));
        
        return producto;
    }
}