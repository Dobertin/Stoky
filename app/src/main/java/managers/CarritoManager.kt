package managers

import models.Producto
import models.CarritoItem

object CarritoManager {
    private val items = mutableListOf<CarritoItem>()

    fun agregarProducto(producto: Producto, cantidad: Int = 1): Boolean {
        // Verificar si hay stock suficiente
        if (!producto.canSell(cantidad)) {
            return false
        }

        // Buscar si el producto ya está en el carrito
        val existingItem = items.find { it.producto.codigo == producto.codigo }

        if (existingItem != null) {
            // Si ya existe, intentar aumentar la cantidad
            val nuevaCantidad = existingItem.cantidad + cantidad
            if (producto.canSell(nuevaCantidad)) {
                existingItem.cantidad = nuevaCantidad
                return true
            } else {
                return false
            }
        } else {
            // Si no existe, crear nuevo item
            items.add(CarritoItem(producto, cantidad))
            return true
        }
    }

    fun removerProducto(codigo: String) {
        items.removeAll { it.producto.codigo == codigo }
    }

    fun actualizarCantidad(codigo: String, nuevaCantidad: Int): Boolean {
        val item = items.find { it.producto.codigo == codigo }
        return item?.actualizarCantidad(nuevaCantidad) ?: false
    }

    fun incrementarCantidad(codigo: String): Boolean {
        val item = items.find { it.producto.codigo == codigo }
        if (item != null) {
            item.incrementarCantidad()
            return true
        }
        return false
    }

    fun decrementarCantidad(codigo: String): Boolean {
        val item = items.find { it.producto.codigo == codigo }
        if (item != null) {
            item.decrementarCantidad()
            return true
        }
        return false
    }

    fun obtenerItems(): List<CarritoItem> = items.toList()

    fun obtenerCantidadTotal(): Int = items.sumOf { it.cantidad }

    fun obtenerTotal(): Double = items.sumOf { it.getSubtotal() }

    fun limpiarCarrito() {
        items.clear()
    }

    fun estaVacio(): Boolean = items.isEmpty()

    fun obtenerItem(codigo: String): CarritoItem? {
        return items.find { it.producto.codigo == codigo }
    }

    fun tieneProducto(codigo: String): Boolean {
        return items.any { it.producto.codigo == codigo }
    }
}