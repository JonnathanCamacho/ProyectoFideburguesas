package com.fideburguesas.pos.model;

import java.math.BigDecimal;

public class Producto {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Long categoriaId;
    private String categoria;
    private Integer stock;
    private Integer stockMinimo;
    private boolean activo;
    private String imagen;

    public Producto() {
        this.activo = true;
        this.stock = 0;
        this.stockMinimo = 5;
    }

    public Producto(String codigo, String nombre, String descripcion, 
                   BigDecimal precio, Long categoriaId) {
        this();
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoriaId = categoriaId;
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

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public boolean tieneStockSuficiente(int cantidad) {
        return stock >= cantidad;
    }

    public boolean necesitaReposicion() {
        return stock <= stockMinimo;
    }

    @Override
    public String toString() {
        return nombre + " - " + com.fideburguesas.pos.util.CurrencyUtil.formatCurrency(precio);
    }
}