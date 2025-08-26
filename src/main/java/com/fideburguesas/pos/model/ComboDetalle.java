package com.fideburguesas.pos.model;

public class ComboDetalle {
    private Long id;
    private Long comboId;
    private Long productoId;
    private String productoNombre;
    private int cantidad;

    public ComboDetalle() {}

    public ComboDetalle(Long comboId, Long productoId, int cantidad) {
        this.comboId = comboId;
        this.productoId = productoId;
        this.cantidad = cantidad;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getComboId() { return comboId; }
    public void setComboId(Long comboId) { this.comboId = comboId; }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}