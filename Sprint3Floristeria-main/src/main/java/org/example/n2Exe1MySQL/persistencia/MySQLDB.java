package org.example.n2Exe1MySQL.persistencia;

import org.example.n2Exe1MySQL.entidad.*;
import org.example.n2Exe1MySQL.excepcion.CantidadExcedida;
import org.example.n2Exe1MySQL.herramienta.Input;
import org.example.n2Exe1MySQL.herramienta.Material;

import java.sql.*;
import java.util.HashMap;
import java.util.Locale;

public class MySQLDB implements InterfaceBaseDeDatos {
    private static MySQLDB instancia;
    private String dbName; // Nombre de la base de datos introducido por el usuario
    private int nextProductoId;
    private int nextTicketId;
    private static Connection conn;

    private MySQLDB(String dbName) {
        this.dbName = dbName;
        if (obtenerConexion()) {
            nextProductoId = generarSiguienteId("producto");
            nextTicketId = generarSiguienteId("ticket");
        } else {
            System.err.println("Error al establecer la conexión.");
        }
    }

    public static MySQLDB instanciar(String dbName) {
        if (instancia == null) {
            instancia = new MySQLDB(dbName);
        }
        return instancia;
    }


    public boolean obtenerConexion() {
        String usuario = Input.inputString("Dime tu usuario MySQL:");
        String password = Input.inputString("Dime tu password MySQL:");

        try {
            // Intenta conectar a la base de datos
            String url = "jdbc:mysql://localhost:3306/?user=" + usuario + "&password=" + password;
            conn = DriverManager.getConnection(url);
            System.out.println("Conexión a MySQL establecida.");

            // Verifica si la base de datos existe
            ResultSet resultSet = conn.getMetaData().getCatalogs();
            boolean dbExists = false;
            while (resultSet.next()) {
                if (resultSet.getString(1).equalsIgnoreCase(dbName)) { // Usa el nombre proporcionado
                    dbExists = true;
                    break;
                }
            }

            // Si no existe, crea la base de datos
            if (!dbExists) {
                System.out.println("Base de datos no encontrada, creando base de datos...");
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE " + dbName);
                    System.out.println("Base de datos creada exitosamente.");
                }
            }

            // Conecta a la base de datos específica
            String dbUrl = "jdbc:mysql://localhost:3306/" + dbName + "?user=" + usuario + "&password=" + password;
            conn = DriverManager.getConnection(dbUrl);

            // Crear tablas si no existen
            crearTablas(conn);

            return true;

        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
            return false;
        }
    }


    private void crearTablas(Connection conn) {
        String createProductoTable = "CREATE TABLE IF NOT EXISTS producto (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +  // Añadido AUTO_INCREMENT
                "nombre VARCHAR(50), " +
                "precio FLOAT, " +
                "tipo VARCHAR(50), " +
                "cantidad INT)";
        String createArbolTable = "CREATE TABLE IF NOT EXISTS arbol (" +
                "id INT PRIMARY KEY, " +
                "altura FLOAT)";
        String createFlorTable = "CREATE TABLE IF NOT EXISTS flor (" +
                "id INT PRIMARY KEY, " +
                "color VARCHAR(50))";
        String createDecoracionTable = "CREATE TABLE IF NOT EXISTS decoracion (" +
                "id INT PRIMARY KEY, " +
                "material VARCHAR(50))";
        String createTicketTable = "CREATE TABLE IF NOT EXISTS ticket (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +  // Añadido AUTO_INCREMENT
                "fecha DATE)";
        String createProductoTicketTable = "CREATE TABLE IF NOT EXISTS producto_ticket (" +
                "ticketId INT, " +
                "productoId INT, " +
                "cantidad INT, " +
                "PRIMARY KEY(ticketId, productoId))";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createProductoTable);
            stmt.executeUpdate(createArbolTable);
            stmt.executeUpdate(createFlorTable);
            stmt.executeUpdate(createDecoracionTable);
            stmt.executeUpdate(createTicketTable);
            stmt.executeUpdate(createProductoTicketTable);
            System.out.println("Tablas creadas o verificadas exitosamente.");
        } catch (SQLException e) {
            System.err.println("Error al crear las tablas: " + e.getMessage());
        }
    }


    @Override
    public void agregarProducto(Producto producto) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(String.format(
                    "SELECT * FROM producto WHERE id = %d", producto.getProductoID()));

            if (rs.next()) {
                int nuevaCantidad = producto.getProductoCantidad() + rs.getInt("cantidad");
                actualizarCantidadProducto(producto.getProductoID(), nuevaCantidad);
            } else {
                String insertarProducto = String.format(Locale.US,
                        "INSERT INTO producto VALUES (%d, '%s', %f, '%s', %d)",
                        producto.getProductoID(), producto.getProductoNombre(),
                        producto.getProductoPrecio(), producto.getProductoTipo(),
                        producto.getProductoCantidad());
                stmt.executeUpdate(insertarProducto);

                String tipo = producto.getProductoTipo().toLowerCase();

                switch (tipo) {
                    case "arbol" -> {
                        String insertarArbol = String.format(Locale.US,
                                "INSERT INTO arbol VALUES (%d, %f)",
                                producto.getProductoID(), ((Producto_Arbol) producto).getArbolAltura());
                        stmt.executeUpdate(insertarArbol);
                    }
                    case "flor" -> {
                        String insertarFlor = String.format(Locale.US,
                                "INSERT INTO flor VALUES (%d, '%s')",
                                producto.getProductoID(), ((Producto_Flor) producto).getFlorColor());
                        stmt.executeUpdate(insertarFlor);
                    }
                    case "decoracion" -> {
                        String insertarDecoracion = String.format("INSERT INTO decoracion VALUES (%d,'%s')",
                                producto.getProductoID(), ((Producto_Decoracion) producto).getDecoracionMaterial());
                        stmt.executeUpdate(insertarDecoracion);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Hubo un error al acceder a los datos. Intenta nuevamente.");
            System.err.println(e.getMessage());
        }
    }

    @Override
    public Ticket agregarTicket(Ticket ticket) {
        PreparedStatement insertTicketOnTicketDB;

        try {
            insertTicketOnTicketDB = conn.prepareStatement(QueriesSQL.AGREGAR_TICKET);
            insertTicketOnTicketDB.setInt(1, ticket.getTicketID());
            insertTicketOnTicketDB.setDate(2, Date.valueOf(ticket.getTicketDate()));
            insertTicketOnTicketDB.execute();

            for (Producto producto : ticket.getProductosVendidos().values()) {
                agregarProductoAlTicket(producto, ticket.getTicketID());
            }

        } catch (SQLException e) {
            System.err.println("Hubo un error al acceder a los datos. Intenta nuevamente.");
            System.err.println(e.getMessage());
        }

        return ticket;
    }

    @Override
    public void actualizarCantidadProducto(int id, int nuevaCantidad) {
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("UPDATE producto SET cantidad = " + nuevaCantidad + " WHERE id = " + id);
        } catch (SQLException e) {
            System.err.println("Hubo un error al acceder a los datos. Intenta nuevamente.");
            System.err.println(e.getMessage());
        }
    }

    private void agregarProductoAlTicket(Producto producto, int ticketID) {
        PreparedStatement insertProductOnProductTicketDB;
        try {
            insertProductOnProductTicketDB = conn.prepareStatement(QueriesSQL.AGREGAR_PRODUCTO_TICKET);
            insertProductOnProductTicketDB.setInt(1, ticketID);
            insertProductOnProductTicketDB.setInt(2, producto.getProductoID());
            insertProductOnProductTicketDB.setInt(3, producto.getProductoCantidad());
            insertProductOnProductTicketDB.execute();
        } catch (SQLException e) {
            System.err.println("Hubo un error al acceder a los datos. Intenta nuevamente.");
            System.err.println(e.getMessage());
        }
    }

    @Override
    public HashMap<Integer, Producto> consultarProductos() {
        HashMap<Integer, Producto> productos = new HashMap<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(QueriesSQL.GET_PRODUCTOS);
            productos = generaMapaProducto(rs);
        } catch (SQLException e) {
            System.err.println("Hubo un error al acceder a los datos. Intenta nuevamente.");
            System.err.println(e.getMessage());
        }
        return productos;
    }

    @Override
    public HashMap<Integer, Ticket> consultarTickets() {
        HashMap<Integer, Ticket> tickets = new HashMap<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(QueriesSQL.LISTAR_TICKETS);
            while (rs.next()) {
                Ticket ticket = new Ticket(rs.getInt("id"), rs.getDate("fecha").toLocalDate());
                tickets.put(ticket.getTicketID(), ticket);
            }
        } catch (SQLException e) {
            System.err.println("Hubo un error al acceder a los datos. Intenta nuevamente.");
            System.err.println(e.getMessage());
        }
        return tickets;
    }

    @Override
    public Producto consultarProducto(int id) {
        // Implementar consulta de producto.
        return null;
    }

    @Override
    public Ticket consultarTicket(int id) {
        // Implementar consulta de ticket.
        return null;
    }

    @Override
    public HashMap<Integer, Producto> consultarProductosFiltrando(String tipo) {
        // Implementar consulta de productos por tipo.
        return null;
    }

    @Override
    public float consultarValorTotalStock() {
        // Implementar consulta del valor total del inventario.
        return 0;
    }

    @Override
    public float consultarValorTotalTickets() {
        // Implementar consulta del valor total de los tickets.
        return 0;
    }

    @Override
    public Producto eliminarProducto(int id, int cantidad) throws CantidadExcedida {
        // Implementar eliminación de producto.
        return null;
    }

    @Override
    public int obtenerSiguienteProductoId() {
        return generarSiguienteId("producto");
    }

    @Override
    public int obtenerSiguienteTicketId() {
        return generarSiguienteId("ticket");
    }

    private HashMap<Integer, Producto> generaMapaProducto(ResultSet rs) throws SQLException {
        HashMap<Integer, Producto> productos = new HashMap<>();
        while (rs.next()) {
            String tipo = rs.getString("tipo").toLowerCase();
            int id = rs.getInt("id");
            Producto producto = switch (tipo) {
                case "arbol" -> new Producto_Arbol(id, rs.getString("nombre"), rs.getFloat("precio"),
                        (float) rs.getInt("cantidad"), (int) rs.getFloat("altura"));
                case "flor" -> new Producto_Flor(id, rs.getString("nombre"), rs.getFloat("precio"),
                        rs.getString("color"), rs.getInt("cantidad"));
                case "decoracion" -> new Producto_Decoracion(id, rs.getString("nombre"), rs.getFloat("precio"),
                        Material.valueOf(rs.getString("material")), rs.getInt("cantidad"));
                default -> throw new IllegalStateException("Unexpected value: " + tipo);
            };
            productos.put(id, producto);
        }
        return productos;
    }

    private int generarSiguienteId(String nombreTabla) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(String.format("SELECT MAX(id) FROM %s", nombreTabla));
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            System.err.println("Error al generar el siguiente ID para " + nombreTabla + ": " + e.getMessage());
        }
        return 1;
    }
}
