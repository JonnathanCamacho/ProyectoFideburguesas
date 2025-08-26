package com.fideburguesas.pos.dao;

import com.fideburguesas.pos.config.DatabaseConfig;
import com.fideburguesas.pos.model.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoriaDAO {
    private Connection connection;
    
    public CategoriaDAO() {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }
    
    public Categoria save(Categoria categoria) throws SQLException {
        String sql;
        if (categoria.getId() == null) {
            sql = "INSERT INTO categorias (nombre, descripcion, activo) VALUES (?, ?, ?)";
        } else {
            sql = "UPDATE categorias SET nombre = ?, descripcion = ?, activo = ? WHERE id = ?";
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, categoria.getNombre());
            stmt.setString(2, categoria.getDescripcion());
            stmt.setBoolean(3, categoria.isActivo());
            
            if (categoria.getId() != null) {
                stmt.setLong(4, categoria.getId());
            }
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Crear categoría falló, no se insertaron filas.");
            }
            
            if (categoria.getId() == null) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        categoria.setId(generatedKeys.getLong(1));
                    }
                }
            }
        }
        
        return categoria;
    }
    
    public Optional<Categoria> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM categorias WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategoria(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    public List<Categoria> findAll() throws SQLException {
        String sql = "SELECT * FROM categorias ORDER BY nombre";
        List<Categoria> categorias = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                categorias.add(mapResultSetToCategoria(rs));
            }
        }
        
        return categorias;
    }
    
    public List<Categoria> findActivas() throws SQLException {
        String sql = "SELECT * FROM categorias WHERE activo = TRUE ORDER BY nombre";
        List<Categoria> categorias = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                categorias.add(mapResultSetToCategoria(rs));
            }
        }
        
        return categorias;
    }
    
    public void delete(Long id) throws SQLException {
        String sql = "UPDATE categorias SET activo = FALSE WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
    
    private Categoria mapResultSetToCategoria(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria();
        categoria.setId(rs.getLong("id"));
        categoria.setNombre(rs.getString("nombre"));
        categoria.setDescripcion(rs.getString("descripcion"));
        categoria.setActivo(rs.getBoolean("activo"));
        
        return categoria;
    }
}