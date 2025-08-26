package com.fideburguesas.pos.model;

import java.math.BigDecimal;

public class OrdenDetalle {
    private Long id;
    private Long ordenId;
    private Long productoId;
    private Long comboId;
    private String itemNombre;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private String observaciones;

    public OrdenDetalle() {}

    public OrdenDetalle(Long ordenId, Long productoId, String itemNombre, 
                       int cantidad, BigDecimal precioUnitario) {
        this.ordenId = ordenId;
        this.productoId = productoId;
        this.itemNombre = itemNombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    // Constructor para combos
    public OrdenDetalle(Long ordenId, Long comboId, String itemNombre, 
                       int cantidad, BigDecimal precioUnitario, boolean esCombo) {
        this.ordenId = ordenId;
        this.comboId = comboId;
        this.itemNombre = itemNombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrdenId() { return ordenId; }
    public void setOrdenId(Long ordenId) { this.ordenId = ordenId; }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public Long getComboId() { return comboId; }
    public void setComboId(Long comboId) { this.comboId = comboId; }

    public String getItemNombre() { return itemNombre; }
    public void setItemNombre(String itemNombre) { this.itemNombre = itemNombre; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { 
        this.cantidad = cantidad;
        // NO calcular subtotal aquí automáticamente
        // this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }


    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { 
        this.precioUnitario = precioUnitario;
        // NO calcular subtotal aquí automáticamente  
        // this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
    public void calcularSubtotal() {
        if (precioUnitario != null && cantidad > 0) {
            this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public boolean esCombo() {
        return comboId != null;
    }
}