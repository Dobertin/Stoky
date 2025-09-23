package adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.stoky.R
import models.Producto

class ProductosAdapter(
    private val productos: List<Producto>,
    private val onProductoClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductosAdapter.ProductoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        holder.bind(producto, onProductoClick)
    }

    override fun getItemCount(): Int = productos.size

    class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCodigo: TextView = itemView.findViewById(R.id.tvCodigo)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
        private val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoria)
        private val tvStock: TextView = itemView.findViewById(R.id.tvStock)
        private val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecio)
        private val tvMedida: TextView = itemView.findViewById(R.id.tvMedida)
        private val btnAgregar: Button = itemView.findViewById(R.id.btnAgregar)

        fun bind(producto: Producto, onProductoClick: (Producto) -> Unit) {
            tvCodigo.text = "Código: ${producto.codigo}"
            tvDescripcion.text = producto.descripcion
            tvCategoria.text = "Categoría: ${producto.categoria}"
            tvStock.text = "Stock: ${producto.stock_actual}"
            tvPrecio.text = "Precio: S/. ${"%.2f".format(producto.precio)}"
            tvMedida.text = "Medida: ${producto.medida}"

            // Cambiar color del stock según disponibilidad
            when {
                producto.stock_actual <= 0 -> {
                    tvStock.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                    btnAgregar.isEnabled = false
                    btnAgregar.text = "Sin Stock"
                }
                producto.stock_actual <= 5 -> {
                    tvStock.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
                    btnAgregar.isEnabled = true
                    btnAgregar.text = "Agregar"
                }
                else -> {
                    tvStock.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                    btnAgregar.isEnabled = true
                    btnAgregar.text = "Agregar"
                }
            }

            btnAgregar.setOnClickListener {
                if (producto.hasStock()) {
                    onProductoClick(producto)
                }
            }

            // Click en toda la card para ver detalles
            itemView.setOnClickListener {
                onProductoClick(producto)
            }
        }
    }
}