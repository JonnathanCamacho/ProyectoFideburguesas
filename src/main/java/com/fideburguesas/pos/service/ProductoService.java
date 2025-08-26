package com.fideburguesas.pos.service;

import com.fideburguesas.pos.dao.ProductoDAO;
import com.fideburguesas.pos.model.Producto;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProductoService {
    private ProductoDAO productoDAO;
    
    public ProductoService() {
        this.productoDAO = new ProductoDAO();
    }
    
    public Producto guardarProducto(Producto producto) throws SQLException {
        validarProducto(producto);
        return productoDAO.save(producto);
    }
    
    public List<Producto> obtenerTodosLosProductos() throws SQLException {
        return productoDAO.findAll();
    }
    
    public List<Producto> obtenerProductosPorCategoria(Long categoriaId) throws SQLException {
        return productoDAO.findByCategoria(categoriaId);
    }
    
    public List<Producto> obtenerProductosBajoStock() throws SQLException {
        return productoDAO.findProductosBajoStock();
    }
    
    public Optional<Producto> buscarPorId(Long id) throws SQLException {
        return productoDAO.findById(id);
    }
    
    public Optional<Producto> buscarPorCodigo(String codigo) throws SQLException {
        return productoDAO.findByCodigo(codigo);
    }
    
    public void actualizarStock(Long productoId, int cantidad) throws SQLException {
        productoDAO.actualizarStock(productoId, cantidad);
    }
    
    public void eliminarProducto(Long id) throws SQLException {
        productoDAO.delete(id);
    }
    
    public boolean verificarStockDisponible(Long productoId, int cantidadRequerida) throws SQLException {
        Optional<Producto> producto = productoDAO.findById(productoId);
        return producto.isPresent() && producto.get().tieneStockSuficiente(cantidadRequerida);
    }
    
    public void reducirStock(Long productoId, int cantidad) throws SQLException {
        if (!verificarStockDisponible(productoId, cantidad)) {
            throw new IllegalArgumentException("Stock insuficiente para el producto");
        }
        actualizarStock(productoId, -cantidad);
    }
    
    private void validarProducto(Producto producto) {
        if (producto.getCodigo() == null || producto.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("El código del producto es requerido");
        }
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es requerido");
        }
        if (producto.getPrecio() == null || producto.getPrecio().doubleValue() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }
        if (producto.getCategoriaId() == null) {
            throw new IllegalArgumentException("La categoría es requerida");
        }
    }
}