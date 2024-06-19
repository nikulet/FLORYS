package org.example.n2Exe1MySQL.entidad;

public class Flor extends Producto {

	private static final long serialVersionUID = 1L;
	private String florColor;

	public Flor(int productoID, String productoNombre, float productoPrecio, String florColor, int productoCantidad) {
		super(productoID, productoNombre, productoPrecio, productoCantidad);
		this.florColor = florColor;
		super.setProductoTipo("Flor");
	}

	public String getFlorColor() {
		return florColor;
	}

	public void setFlorColor(String florColor) {
		this.florColor = florColor;
	}

	@Override
	public String toString() {
		return super.toString() + ", Color=" + florColor + "]";
	}

}
