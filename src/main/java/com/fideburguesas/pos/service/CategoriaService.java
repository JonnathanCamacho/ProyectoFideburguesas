package com.fideburguesas.pos.service;

import com.fideburguesas.pos.dao.CategoriaDAO;
import com.fideburguesas.pos.model.Categoria;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CategoriaService {
    private CategoriaDAO categoriaDAO;
    
    public CategoriaService() {
        this.categoriaDAO = new CategoriaDAO();
    }
    
    public Categoria guardarCategoria(Categoria categoria) throws SQLException {
        validarCategoria(categoria);
        return categoriaDAO.save(categoria);
    }
    
    public List<Categoria> obtenerTodasLasCategorias() throws SQLException {
        return categoriaDAO.findAll();
    }
    
    public List<Categoria> obtenerCategoriasActivas() throws SQLException {
        return categoriaDAO.findActivas();
    }
    
    public Optional<Categoria> buscarPorId(Long id) throws SQLException {
        return categoriaDAO.findById(id);
    }
    
    public void eliminarCategoria(Long id) throws SQLException {
        categoriaDAO.delete(id);
    }
    
    private void validarCategoria(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría es requerido");
        }
    }
}