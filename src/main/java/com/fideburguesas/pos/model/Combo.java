package com.fideburguesas.pos.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Combo {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal descuento;
    private boolean activo;
    private List<ComboDetalle> productos;

    public Combo() {
        this.activo = true;
        this.productos = new ArrayList<>();
        this.descuento = BigDecimal.ZERO;
    }

    public Combo(String codigo, String nombre, String descripcion, BigDecimal precio) {
        this();
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public List<ComboDetalle> getProductos() { return productos; }
    public void setProductos(List<ComboDetalle> productos) { this.productos = productos; }

    public void agregarProducto(Long productoId, int cantidad) {
        productos.add(new ComboDetalle(id, productoId, cantidad));
    }

    public BigDecimal getPrecioConDescuento() {
        return precio.subtract(descuento);
    }

    @Override
    public String toString() {
        return "COMBO: " + nombre + " - " + com.fideburguesas.pos.util.CurrencyUtil.formatCurrency(getPrecioConDescuento());
    }
}