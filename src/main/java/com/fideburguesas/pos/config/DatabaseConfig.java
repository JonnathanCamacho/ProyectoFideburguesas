package com.fideburguesas.pos.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static final String DB_URL = "jdbc:h2:~/fideburguesas_pos;AUTO_SERVER=TRUE";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    
    private static DatabaseConfig instance;
    private Connection connection;
    
    private DatabaseConfig() {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            initializeDatabase();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error al conectar con la base de datos", e);
        }
    }
    
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }
    
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            }
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener la conexión", e);
        }
    }
    
    private void initializeDatabase() {
        try (Statement stmt = connection.createStatement()) {
            // Tabla usuarios
            stmt.execute("CREATE TABLE IF NOT EXISTS usuarios (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) UNIQUE NOT NULL," +
                "password VARCHAR(255) NOT NULL," +
                "nombre VARCHAR(100) NOT NULL," +
                "apellido VARCHAR(100) NOT NULL," +
                "email VARCHAR(150)," +
                "telefono VARCHAR(20)," +
                "tipo_usuario VARCHAR(20) NOT NULL," +
                "activo BOOLEAN DEFAULT TRUE," +
                "fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "ultimo_login TIMESTAMP" +
            ")");

            // Tabla categorias
            stmt.execute("CREATE TABLE IF NOT EXISTS categorias (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "nombre VARCHAR(100) NOT NULL," +
                "descripcion TEXT," +
                "activo BOOLEAN DEFAULT TRUE" +
            ")");

            // Tabla productos
            stmt.execute("CREATE TABLE IF NOT EXISTS productos (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "codigo VARCHAR(50) UNIQUE NOT NULL," +
                "nombre VARCHAR(150) NOT NULL," +
                "descripcion TEXT," +
                "precio DECIMAL(10,2) NOT NULL," +
                "categoria_id BIGINT," +
                "stock INTEGER DEFAULT 0," +
                "stock_minimo INTEGER DEFAULT 5," +
                "activo BOOLEAN DEFAULT TRUE," +
                "imagen VARCHAR(255)," +
                "FOREIGN KEY (categoria_id) REFERENCES categorias(id)" +
            ")");

            // Tabla combos
            stmt.execute("CREATE TABLE IF NOT EXISTS combos (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "codigo VARCHAR(50) UNIQUE NOT NULL," +
                "nombre VARCHAR(150) NOT NULL," +
                "descripcion TEXT," +
                "precio DECIMAL(10,2) NOT NULL," +
                "descuento DECIMAL(10,2) DEFAULT 0," +
                "activo BOOLEAN DEFAULT TRUE" +
            ")");

            // Tabla combo_detalles
            stmt.execute("CREATE TABLE IF NOT EXISTS combo_detalles (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "combo_id BIGINT NOT NULL," +
                "producto_id BIGINT NOT NULL," +
                "cantidad INTEGER NOT NULL," +
                "FOREIGN KEY (combo_id) REFERENCES combos(id)," +
                "FOREIGN KEY (producto_id) REFERENCES productos(id)" +
            ")");

            // Tabla ordenes
            stmt.execute("CREATE TABLE IF NOT EXISTS ordenes (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "numero VARCHAR(20) UNIQUE NOT NULL," +
                "fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "cajero_id BIGINT NOT NULL," +
                "estado VARCHAR(20) DEFAULT 'PENDIENTE'," +
                "subtotal DECIMAL(10,2) DEFAULT 0," +
                "impuestos DECIMAL(10,2) DEFAULT 0," +
                "total DECIMAL(10,2) DEFAULT 0," +
                "observaciones TEXT," +
                "FOREIGN KEY (cajero_id) REFERENCES usuarios(id)" +
            ")");

            // Tabla orden_detalles
            stmt.execute("CREATE TABLE IF NOT EXISTS orden_detalles (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "orden_id BIGINT NOT NULL," +
                "producto_id BIGINT," +
                "combo_id BIGINT," +
                "item_nombre VARCHAR(150) NOT NULL," +
                "cantidad INTEGER NOT NULL," +
                "precio_unitario DECIMAL(10,2) NOT NULL," +
                "subtotal DECIMAL(10,2) NOT NULL," +
                "observaciones TEXT," +
                "FOREIGN KEY (orden_id) REFERENCES ordenes(id)," +
                "FOREIGN KEY (producto_id) REFERENCES productos(id)," +
                "FOREIGN KEY (combo_id) REFERENCES combos(id)" +
            ")");

            // Insertar datos iniciales
            insertarDatosIniciales();

        } catch (SQLException e) {
            throw new RuntimeException("Error al inicializar la base de datos", e);
        }
    }
    
    
    public void verificarUsuarios() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            var rs = stmt.executeQuery("SELECT username, password FROM usuarios WHERE username = 'admin'");
            if (rs.next()) {
                System.out.println("Usuario encontrado: " + rs.getString("username"));
                System.out.println("Hash en BD: " + rs.getString("password"));
            } else {
                System.out.println("Usuario admin NO encontrado en la base de datos");
            }
        }
    }
    
    private void insertarDatosIniciales() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Verificar si ya existe el usuario admin
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM usuarios WHERE username = 'admin'");
            rs.next();
            if (rs.getInt(1) == 0) {
                // Usuario administrador por defecto (password: "password")
                stmt.execute("INSERT INTO usuarios " +
                    "(username, password, nombre, apellido, email, tipo_usuario) " +
                    "VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye7Iy/SfMphrIW0Y.xQK4WqXsV1dWXWQ.', " +
                    "'Administrador', 'Sistema', 'admin@fideburguesas.com', 'ADMIN')");
            }

            // Verificar y agregar categorías
            rs = stmt.executeQuery("SELECT COUNT(*) FROM categorias");
            rs.next();
            if (rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO categorias (nombre, descripcion) VALUES " +
                    "('Hamburguesas', 'Deliciosas hamburguesas artesanales')," +
                    "('Bebidas', 'Refrescos, jugos y bebidas calientes')," +
                    "('Acompañamientos', 'Papas fritas, aros de cebolla, etc.')," +
                    "('Postres', 'Helados, pasteles y dulces')," +
                    "('Ensaladas', 'Ensaladas frescas y saludables')");
            }

            // Verificar y agregar productos
            rs = stmt.executeQuery("SELECT COUNT(*) FROM productos");
            rs.next();
            if (rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO productos " +
                    "(codigo, nombre, descripcion, precio, categoria_id, stock) VALUES " +
                    "('HAM001', 'Hamburguesa Clásica', 'Carne, lechuga, tomate, queso', 8500.00, 1, 50)," +
                    "('HAM002', 'Hamburguesa BBQ', 'Carne, salsa BBQ, cebolla caramelizada', 9500.00, 1, 30)," +
                    "('BEB001', 'Coca Cola', 'Refresco de cola 500ml', 1500.00, 2, 100)," +
                    "('BEB002', 'Agua', 'Agua natural 500ml', 1000.00, 2, 80)," +
                    "('PAP001', 'Papas Fritas', 'Papas fritas crujientes', 3000.00, 3, 60)");
            }
        }
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}