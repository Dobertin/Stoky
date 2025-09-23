package models

import java.io.Serializable

data class CarritoItem(
    val producto: Producto,
    var cantidad: Int = 1,
    val precioUnitario: Double = producto.precio
) : Serializable {

    fun getSubtotal(): Double = precioUnitario * cantidad

    fun incrementarCantidad() {
        if (producto.canSell(cantidad + 1)) {
            cantidad++
        }
    }

    fun decrementarCantidad() {
        if (cantidad > 1) {
            cantidad--
        }
    }

    fun actualizarCantidad(nuevaCantidad: Int): Boolean {
        return if (producto.canSell(nuevaCantidad) && nuevaCantidad > 0) {
            cantidad = nuevaCantidad
            true
        } else {
            false
        }
    }
}