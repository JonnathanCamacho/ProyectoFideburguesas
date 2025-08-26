package com.fideburguesas.pos.service;

import com.fideburguesas.pos.dao.UsuarioDAO;
import com.fideburguesas.pos.model.Usuario;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UsuarioService {
    private UsuarioDAO usuarioDAO;
    private Usuario usuarioActual;
    
    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }
    
    public boolean autenticar(String username, String password) {
        try {
            if (usuarioDAO.authenticate(username, password)) {
                Optional<Usuario> usuario = usuarioDAO.findByUsername(username);
                if (usuario.isPresent()) {
                    this.usuarioActual = usuario.get();
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
    
    public void cerrarSesion() {
        this.usuarioActual = null;
    }
    
    public Usuario guardarUsuario(Usuario usuario) throws SQLException {
        validarUsuario(usuario);
        return usuarioDAO.save(usuario);
    }
    
    public List<Usuario> obtenerTodosLosUsuarios() throws SQLException {
        return usuarioDAO.findAll();
    }
    
    public List<Usuario> obtenerUsuariosPorTipo(Usuario.TipoUsuario tipo) throws SQLException {
        return usuarioDAO.findByTipo(tipo);
    }
    
    public Optional<Usuario> buscarPorId(Long id) throws SQLException {
        return usuarioDAO.findById(id);
    }
    
    public Optional<Usuario> buscarPorUsername(String username) throws SQLException {
        return usuarioDAO.findByUsername(username);
    }
    
    public void eliminarUsuario(Long id) throws SQLException {
        usuarioDAO.delete(id);
    }
    
    public boolean esAdministrador() {
        return usuarioActual != null && 
               usuarioActual.getTipoUsuario() == Usuario.TipoUsuario.ADMIN;
    }
    
    public boolean esCajero() {
        return usuarioActual != null && 
               usuarioActual.getTipoUsuario() == Usuario.TipoUsuario.CAJERO;
    }
    
    public boolean esCocina() {
        return usuarioActual != null && 
               usuarioActual.getTipoUsuario() == Usuario.TipoUsuario.COCINA;
    }
    
    private void validarUsuario(Usuario usuario) {
        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es requerido");
        }
        if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es requerida");
        }
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es requerido");
        }
        if (usuario.getApellido() == null || usuario.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es requerido");
        }
        if (usuario.getTipoUsuario() == null) {
            throw new IllegalArgumentException("El tipo de usuario es requerido");
        }
    }
}