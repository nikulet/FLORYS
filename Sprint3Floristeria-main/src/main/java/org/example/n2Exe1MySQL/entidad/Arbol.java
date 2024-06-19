package org.example.n2Exe1MySQL.entidad;


public class Arbol extends Producto {

	private static final long serialVersionUID = 1L;
	private float arbolAltura;

	public Arbol(int productoID, String productoNombre, float productoPrecio, float arbolAltura, int productoCantidad) {
		super(productoID, productoNombre, productoPrecio, productoCantidad);
		this.arbolAltura = arbolAltura;
		super.setProductoTipo("Árbol");

	}

	public float getArbolAltura() {
		return arbolAltura;
	}

	public void setArbolAltura(float arbolAltura) {
		this.arbolAltura = arbolAltura;
	}

	@Override
	public String toString() {
		return super.toString() + ", Altura=" + arbolAltura + "]";
	}

}
