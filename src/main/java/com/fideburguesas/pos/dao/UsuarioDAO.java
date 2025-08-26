package com.fideburguesas.pos.dao;

import com.fideburguesas.pos.config.DatabaseConfig;
import com.fideburguesas.pos.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDAO {
    private Connection connection;
    
    public UsuarioDAO() {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }
    
    public Usuario save(Usuario usuario) throws SQLException {
        String sql;
        if (usuario.getId() == null) {
            sql = "INSERT INTO usuarios (username, password, nombre, apellido, email, " +
                  "telefono, tipo_usuario, activo) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE usuarios SET username = ?, password = ?, nombre = ?, " +
                  "apellido = ?, email = ?, telefono = ?, " +
                  "tipo_usuario = ?, activo = ? " +
                  "WHERE id = ?";
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, usuario.getUsername());
            // Encriptar password si es nuevo usuario o si cambió
            if (usuario.getId() == null || !usuario.getPassword().startsWith("$2a$")) {
                stmt.setString(2, BCrypt.hashpw(usuario.getPassword(), BCrypt.gensalt()));
            } else {
                stmt.setString(2, usuario.getPassword());
            }
            stmt.setString(3, usuario.getNombre());
            stmt.setString(4, usuario.getApellido());
            stmt.setString(5, usuario.getEmail());
            stmt.setString(6, usuario.getTelefono());
            stmt.setString(7, usuario.getTipoUsuario().name());
            stmt.setBoolean(8, usuario.isActivo());
            
            if (usuario.getId() != null) {
                stmt.setLong(9, usuario.getId());
            }
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Crear usuario falló, no se insertaron filas.");
            }
            
            if (usuario.getId() == null) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        usuario.setId(generatedKeys.getLong(1));
                    }
                }
            }
        }
        
        return usuario;
    }
    
    public Optional<Usuario> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    public Optional<Usuario> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE username = ? AND activo = TRUE";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    public List<Usuario> findAll() throws SQLException {
        String sql = "SELECT * FROM usuarios ORDER BY nombre, apellido";
        List<Usuario> usuarios = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                usuarios.add(mapResultSetToUsuario(rs));
            }
        }
        
        return usuarios;
    }
    
    public List<Usuario> findByTipo(Usuario.TipoUsuario tipo) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE tipo_usuario = ? AND activo = TRUE ORDER BY nombre, apellido";
        List<Usuario> usuarios = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tipo.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapResultSetToUsuario(rs));
                }
            }
        }
        
        return usuarios;
    }
    
    public boolean authenticate(String username, String password) throws SQLException {
        Optional<Usuario> usuario = findByUsername(username);
        if (usuario.isPresent()) {
            boolean valid = BCrypt.checkpw(password, usuario.get().getPassword());
            if (valid) {
                // Actualizar último login
                updateUltimoLogin(usuario.get().getId());
            }
            return valid;
        }
        return false;
    }
    
    private void updateUltimoLogin(Long userId) throws SQLException {
        String sql = "UPDATE usuarios SET ultimo_login = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        }
    }
    
    public void delete(Long id) throws SQLException {
        String sql = "UPDATE usuarios SET activo = FALSE WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
    
    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("id"));
        usuario.setUsername(rs.getString("username"));
        usuario.setPassword(rs.getString("password"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        usuario.setEmail(rs.getString("email"));
        usuario.setTelefono(rs.getString("telefono"));
        usuario.setTipoUsuario(Usuario.TipoUsuario.valueOf(rs.getString("tipo_usuario")));
        usuario.setActivo(rs.getBoolean("activo"));
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            usuario.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        Timestamp ultimoLogin = rs.getTimestamp("ultimo_login");
        if (ultimoLogin != null) {
            usuario.setUltimoLogin(ultimoLogin.toLocalDateTime());
        }
        
        return usuario;
    }
}