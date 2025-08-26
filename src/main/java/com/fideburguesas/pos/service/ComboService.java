package com.fideburguesas.pos.service;

import com.fideburguesas.pos.dao.ComboDAO;
import com.fideburguesas.pos.model.Combo;
import com.fideburguesas.pos.model.ComboDetalle;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ComboService {
    private ComboDAO comboDAO;
    private ProductoService productoService;
    
    public ComboService() {
        this.comboDAO = new ComboDAO();
        this.productoService = new ProductoService();
    }
    
    public Combo guardarCombo(Combo combo) throws SQLException {
        validarCombo(combo);
        return comboDAO.save(combo);
    }
    
    public List<Combo> obtenerTodosLosCombos() throws SQLException {
        return comboDAO.findAll();
    }
    
    public List<Combo> obtenerCombosActivos() throws SQLException {
        return comboDAO.findActivos();
    }
    
    public Optional<Combo> buscarPorId(Long id) throws SQLException {
        return comboDAO.findById(id);
    }
    
    public Optional<Combo> buscarPorCodigo(String codigo) throws SQLException {
        return comboDAO.findByCodigo(codigo);
    }
    
    public void eliminarCombo(Long id) throws SQLException {
        comboDAO.delete(id);
    }
    
    public boolean verificarStockDisponibleCombo(Long comboId, int cantidadRequerida) throws SQLException {
        Optional<Combo> combo = comboDAO.findById(comboId);
        if (combo.isPresent()) {
            for (ComboDetalle detalle : combo.get().getProductos()) {
                int cantidadNecesaria = detalle.getCantidad() * cantidadRequerida;
                if (!productoService.verificarStockDisponible(detalle.getProductoId(), cantidadNecesaria)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    public void reducirStockCombo(Long comboId, int cantidad) throws SQLException {
        if (!verificarStockDisponibleCombo(comboId, cantidad)) {
            throw new IllegalArgumentException("Stock insuficiente para el combo");
        }
        
        Optional<Combo> combo = comboDAO.findById(comboId);
        if (combo.isPresent()) {
            for (ComboDetalle detalle : combo.get().getProductos()) {
                int cantidadNecesaria = detalle.getCantidad() * cantidad;
                productoService.reducirStock(detalle.getProductoId(), cantidadNecesaria);
            }
        }
    }
    
    private void validarCombo(Combo combo) {
        if (combo.getCodigo() == null || combo.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("El código del combo es requerido");
        }
        if (combo.getNombre() == null || combo.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del combo es requerido");
        }
        if (combo.getPrecio() == null || combo.getPrecio().doubleValue() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }
        if (combo.getProductos() == null || combo.getProductos().isEmpty()) {
            throw new IllegalArgumentException("El combo debe tener al menos un producto");
        }
    }
}