package com.fideburguesas.pos.service;

import com.fideburguesas.pos.dao.OrdenDAO;
import com.fideburguesas.pos.model.Orden;
import com.fideburguesas.pos.model.OrdenDetalle;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class OrdenService {
    private OrdenDAO ordenDAO;
    private ProductoService productoService;
    
    public OrdenService() {
        this.ordenDAO = new OrdenDAO();
        this.productoService = new ProductoService();
    }
    
    public Orden crearOrden(Long cajeroId) throws SQLException {
        String numeroOrden = ordenDAO.generateNumeroOrden();
        Orden orden = new Orden(numeroOrden, cajeroId);
        return orden;
    }
    
    
    public String generateNumeroOrden() throws SQLException {
        return ordenDAO.generateNumeroOrden();
    }
    
    public Orden guardarOrden(Orden orden) throws SQLException {
        validarOrden(orden);
        
        // Calcular totales
        orden.calcularTotales();
        
        // Guardar la orden
        Orden ordenGuardada = ordenDAO.save(orden);
        
        // Reducir stock de productos
        for (OrdenDetalle detalle : orden.getDetalles()) {
            if (detalle.getProductoId() != null) {
                productoService.reducirStock(detalle.getProductoId(), detalle.getCantidad());
            }
        }
        
        return ordenGuardada;
    }
    
    public List<Orden> obtenerTodasLasOrdenes() throws SQLException {
        return ordenDAO.findAll();
    }
    
    public List<Orden> obtenerOrdenesPorEstado(Orden.EstadoOrden estado) throws SQLException {
        return ordenDAO.findByEstado(estado);
    }
    
    public List<Orden> obtenerOrdenesPorFecha(LocalDate fecha) throws SQLException {
        return ordenDAO.findByFecha(fecha);
    }
    
    public List<Orden> obtenerOrdenesPendientesCocina() throws SQLException {
        return ordenDAO.findOrdenesPendientesCocina();
    }
    
    public Optional<Orden> buscarPorId(Long id) throws SQLException {
        return ordenDAO.findById(id);
    }
    
    public Optional<Orden> buscarPorNumero(String numero) throws SQLException {
        return ordenDAO.findByNumero(numero);
    }
    
    public void cambiarEstadoOrden(Long ordenId, Orden.EstadoOrden nuevoEstado) throws SQLException {
        ordenDAO.updateEstado(ordenId, nuevoEstado);
    }
    
    public void marcarOrdenEnPreparacion(Long ordenId) throws SQLException {
        cambiarEstadoOrden(ordenId, Orden.EstadoOrden.EN_PREPARACION);
    }
    
    public void marcarOrdenLista(Long ordenId) throws SQLException {
        cambiarEstadoOrden(ordenId, Orden.EstadoOrden.LISTA);
    }
    
    public void marcarOrdenEntregada(Long ordenId) throws SQLException {
        cambiarEstadoOrden(ordenId, Orden.EstadoOrden.ENTREGADA);
    }
    
    public void cancelarOrden(Long ordenId) throws SQLException {
        // Devolver stock antes de cancelar
        Optional<Orden> orden = ordenDAO.findById(ordenId);
        if (orden.isPresent()) {
            for (OrdenDetalle detalle : orden.get().getDetalles()) {
                if (detalle.getProductoId() != null) {
                    productoService.actualizarStock(detalle.getProductoId(), detalle.getCantidad());
                }
            }
        }
        
        cambiarEstadoOrden(ordenId, Orden.EstadoOrden.CANCELADA);
    }
    
    public BigDecimal calcularVentasDelDia(LocalDate fecha) throws SQLException {
        List<Orden> ordenes = obtenerOrdenesPorFecha(fecha);
        return ordenes.stream()
                .filter(o -> o.getEstado() == Orden.EstadoOrden.ENTREGADA)
                .map(Orden::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private void validarOrden(Orden orden) {
        if (orden.getCajeroId() == null) {
            throw new IllegalArgumentException("El cajero es requerido");
        }
        if (orden.getDetalles() == null || orden.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La orden debe tener al menos un producto");
        }
        
        for (OrdenDetalle detalle : orden.getDetalles()) {
            if (detalle.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
            }
            if (detalle.getPrecioUnitario() == null || detalle.getPrecioUnitario().doubleValue() <= 0) {
                throw new IllegalArgumentException("El precio unitario debe ser mayor a cero");
            }
        }
    }
}