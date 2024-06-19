package org.example.n2Exe1MySQL.cliente;

import MySQL.Entrada.Input;
import MySQL.Entrada.Material;
import MySQL.Excepciones.CantidadExcedida;
import MySQL.Excepciones.ProductoNoExiste;
import MySQL.Model.Decoracion;
import MySQL.Model.Floristeria;
import MySQL.Model.Producto;
import MySQL.Model.Flor;
import MySQL.Model.Arbol;
import MySQL.Model.Ticket;

import java.time.LocalDate;
import java.util.HashMap;

public class AplicacionFloristeria {

    private static Floristeria floristeria;
    public static void start (){

        floristeria = Floristeria.getInstancia();
        Menu.ejecutarMenu();
    }

    public static void agregarCantidadProducto (){
        int idProducto = Input.inputInt("Id del producto:");
        int cantidad = Input.inputInt("Cantidad a añadir:");
        Producto producto = floristeria.consultarProducto(idProducto);

        if(producto == null){
            System.out.println("El producto no existe.");
        } else {
            floristeria.agregarCantidadProducto(idProducto, producto.getProductoCantidad() + cantidad);
        }
    }

    public static Arbol crearArbol() {
        String nombre = Input.inputString("Dime el nombre del árbol:");
        float precio = Input.inputFloat("Dime el precio:");
        float altura = Input.inputFloat("Dime la altura:");
        int cantidad = Input.inputInt("Dime la cantidad:");
        return new Arbol(floristeria.consultarSiguienteProductoID(), nombre, precio, altura, cantidad);
    }

    public static Flor crearFlor() {
        String nombre = Input.inputString("Dime el nombre de la flor:");
        float precio = Input.inputFloat("Dime el precio:");
        String color = Input.inputString("Dime el color:");
        int cantidad = Input.inputInt("Dime la cantidad:");
        return new Flor(floristeria.consultarSiguienteProductoID(), nombre, precio, color, cantidad);
    }

    public static Decoracion crearDecoracion() {
        String nombre = Input.inputString("Dime el tipo de decoración:");
        float precio = Input.inputFloat("Dime el precio:");
        Material material = Input.inputEnum("Dime el material (madera o plastico)");
        int cantidad = Input.inputInt("Dime la cantidad:");
        return new Decoracion(floristeria.consultarSiguienteProductoID(), nombre, precio, material, cantidad);
    }

    public static void eliminarProducto (){
        int id = Input.inputInt("ID de producto: ");
        int cantidad = Input.inputInt("Cantidad a retirar: ");
        try {
            floristeria.eliminarProducto(id, cantidad);
        } catch (CantidadExcedida | ProductoNoExiste e) {
            System.out.println(e.getMessage());
        }
    }
    public static void consultarProductos(){
        System.out.println("\nStock por tipo de producto:");
        consultarArbol(floristeria.consultarListaProductosPorTipo("arbol"));
        consultarFlor(floristeria.consultarListaProductosPorTipo("flor"));
        consultarDecoracion(floristeria.consultarListaProductosPorTipo("decoracion"));
    }
    private static void consultarArbol (HashMap<Integer, Producto> stockArbol){
        System.out.println("***ARBOL***:\n");
        stockArbol.values().forEach(producto -> {
            Arbol productoArbol = (Arbol) producto;
            System.out.println("ID: " + productoArbol.getProductoID()
                    + " | Cantidad: " + productoArbol.getProductoCantidad()
                    + " | Nombre: " + productoArbol.getProductoNombre()
                    + " | Altura: " + productoArbol.getArbolAltura()
                    + " | Precio: " + productoArbol.getProductoPrecio());

        });
    }
    private static void consultarFlor (HashMap<Integer, Producto> stockFlor){
        System.out.println("\n***FLOR***:\n");
        stockFlor.values().forEach(producto -> {
            Flor productoFlor = (Flor) producto;
            System.out.println("ID: " + productoFlor.getProductoID()
                    + " | Cantidad: " + productoFlor.getProductoCantidad()
                    + " | Nombre: " + productoFlor.getProductoNombre()
                    + " | Color: " + productoFlor.getFlorColor()
                    + " | Precio: " + productoFlor.getProductoPrecio());
        });
    }
    private static void consultarDecoracion (HashMap<Integer,Producto> stockDecoracion){
        System.out.println("\n***DECORACION***:\n");
        stockDecoracion.values().forEach(producto -> {
            Decoracion productoDecoracion = (Decoracion) producto;
            System.out.println("ID: " + productoDecoracion.getProductoID()
                    + " | Cantidad: " + productoDecoracion.getProductoCantidad()
                    + " | Nombre: " + productoDecoracion.getProductoNombre()
                    + " | Material: " + productoDecoracion.getDecoracionMaterial()
                    + " | Precio: " + productoDecoracion.getProductoPrecio());

        });
    }
    public static void consultarValorTotalStock(){
        float valorTotal = floristeria.consultarValorTotalInventario();
        String formattedValue = String.format("%.2f", valorTotal);
        System.out.println("El valor total del stock es de " + formattedValue + " Euros.");
    }

    public static void crearTicket() {
        Ticket ticket = new Ticket(floristeria.consultarSiguienteTicketID(), LocalDate.now());
        agregarProductosTicket(ticket);
        floristeria.agregarTicket(ticket);
    }

    private static void agregarProductosTicket(Ticket ticket) {
        int productoID;
        int cantidadProductoEnTicket;
        boolean si;
        do {
            productoID = Input.inputInt("Id Producto para agregar: ");
            cantidadProductoEnTicket = Input.inputInt("Cantidad: ");
            try {
                if (floristeria.existeProducto(productoID, cantidadProductoEnTicket)) {
                    Producto productoAAgregar = floristeria.consultarProducto(productoID).clonar();
                    productoAAgregar.setProductoCantidad(cantidadProductoEnTicket);
                    ticket.agregarProductoAlTicket(productoAAgregar);
                    try {
                        floristeria.eliminarProducto(productoID, cantidadProductoEnTicket);
                    } catch (CantidadExcedida e) {
                        System.out.println(e.getMessage());
                    }
                } else {
                    System.err.println("No existe el producto, o no hay suficiente en stock.");
                }
            } catch (ProductoNoExiste e) {
                System.out.println(e.getMessage());
            }
            si = Input.inputSiNo("Deseas agregar otro producto/ o cambiar cantidad? s/n");
        } while (si);
    }

    public static void consultarHistorialTickets() {
        floristeria.consultarListaTickets().entrySet().forEach(System.out::println);
    }

    public static void imprimirValorTotalDeVentas() {
        System.out.println("El valor total del ventas es de " + floristeria.consultarValorTotalVentas());
    }

}