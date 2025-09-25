package models

import java.io.Serializable

data class Producto(
    val codigo: Int = 0,
    val descripcion: String = "",
    val categoria: String = "",
    val medida: String = "",
    val stock_actual: Int = 0,
    val costo: Double = 0.0,
    val precio: Double = 0.0,
    val valor_stock_actual: Double = 0.0,
    val valor_stock_custom: Double = 0.0,
    val valor_vendido: Double = 0.0,
    val cantidad_vendida: Int = 0,
    val cantidad_salidas: Int = 0,
    val ganancia: Double = 0.0
) : Serializable {

    fun hasStock(): Boolean = stock_actual > 0

    fun canSell(quantity: Int): Boolean = stock_actual >= quantity

    fun getTotalPrice(quantity: Int): Double = precio * quantity
}