package com.fideburguesas.pos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Orden {
    private Long id;
    private String numero;
    private LocalDateTime fecha;
    private Long cajeroId;
    private String cajeroNombre;
    private EstadoOrden estado;
    private BigDecimal subtotal;
    private BigDecimal impuestos;
    private BigDecimal total;
    private String observaciones;
    private List<OrdenDetalle> detalles;

    public enum EstadoOrden {
        PENDIENTE, EN_PREPARACION, LISTA, ENTREGADA, CANCELADA
    }

    public Orden() {
        this.fecha = LocalDateTime.now();
        this.estado = EstadoOrden.PENDIENTE;
        this.detalles = new ArrayList<>();
        this.subtotal = BigDecimal.ZERO;
        this.impuestos = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
    }

    public Orden(String numero, Long cajeroId) {
        this();
        this.numero = numero;
        this.cajeroId = cajeroId;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Long getCajeroId() { return cajeroId; }
    public void setCajeroId(Long cajeroId) { this.cajeroId = cajeroId; }

    public String getCajeroNombre() { return cajeroNombre; }
    public void setCajeroNombre(String cajeroNombre) { this.cajeroNombre = cajeroNombre; }

    public EstadoOrden getEstado() { return estado; }
    public void setEstado(EstadoOrden estado) { this.estado = estado; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getImpuestos() { return impuestos; }
    public void setImpuestos(BigDecimal impuestos) { this.impuestos = impuestos; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public List<OrdenDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<OrdenDetalle> detalles) { this.detalles = detalles; }

    public void calcularTotales() {
        subtotal = BigDecimal.ZERO;

        for (OrdenDetalle detalle : detalles) {
            if (detalle.getSubtotal() == null) {
                detalle.calcularSubtotal(); // Asegurar que esté calculado
            }
            if (detalle.getSubtotal() != null) {
                subtotal = subtotal.add(detalle.getSubtotal());
            }
        }

        impuestos = subtotal.multiply(BigDecimal.valueOf(0.13)); // 13% IVA
        total = subtotal.add(impuestos);
    }

    @Override
    public String toString() {
        return "Orden #" + numero + " - " + estado + " - $" + total;
    }
}