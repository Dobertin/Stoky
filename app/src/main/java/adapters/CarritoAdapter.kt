package adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.stoky.R
import models.CarritoItem

class CarritoAdapter(
    private val onIncrementar: (String) -> Unit,
    private val onDecrementar: (String) -> Unit,
    private val onRemover: (String) -> Unit
) : RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder>() {

    private var items = mutableListOf<CarritoItem>()

    fun actualizarItems(nuevosItems: List<CarritoItem>) {
        items.clear()
        items.addAll(nuevosItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrito, parent, false)
        return CarritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, onIncrementar, onDecrementar, onRemover)
    }

    override fun getItemCount(): Int = items.size

    class CarritoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombreProducto: TextView = itemView.findViewById(R.id.tvNombreProducto)
        private val tvCodigoProducto: TextView = itemView.findViewById(R.id.tvCodigoProducto)
        private val tvPrecioUnitario: TextView = itemView.findViewById(R.id.tvPrecioUnitario)
        private val tvCantidad: TextView = itemView.findViewById(R.id.tvCantidad)
        private val tvSubtotal: TextView = itemView.findViewById(R.id.tvSubtotal)
        private val btnDecrementar: ImageButton = itemView.findViewById(R.id.btnDecrementar)
        private val btnIncrementar: ImageButton = itemView.findViewById(R.id.btnIncrementar)
        private val btnRemover: Button = itemView.findViewById(R.id.btnRemover)

        fun bind(
            item: CarritoItem,
            onIncrementar: (String) -> Unit,
            onDecrementar: (String) -> Unit,
            onRemover: (String) -> Unit
        ) {
            tvNombreProducto.text = item.producto.descripcion
            tvCodigoProducto.text = "Código: ${item.producto.codigo}"
            tvPrecioUnitario.text = "S/. ${"%.2f".format(item.precioUnitario)}"
            tvCantidad.text = item.cantidad.toString()
            tvSubtotal.text = "S/. ${"%.2f".format(item.getSubtotal())}"

            // Configurar botones
            btnDecrementar.setOnClickListener {
                onDecrementar(item.producto.codigo)
            }

            btnIncrementar.setOnClickListener {
                onIncrementar(item.producto.codigo)
            }

            btnRemover.setOnClickListener {
                onRemover(item.producto.codigo)
            }

            // Deshabilitar decrementar si la cantidad es 1
            btnDecrementar.isEnabled = item.cantidad > 1

            // Verificar si se puede incrementar más
            val stockDisponible = item.producto.stock_actual
            btnIncrementar.isEnabled = item.cantidad < stockDisponible

            // Mostrar advertencia si está cerca del límite de stock
            if (item.cantidad >= stockDisponible) {
                tvCantidad.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
            } else if (item.cantidad >= stockDisponible * 0.8) {
                tvCantidad.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
            } else {
                tvCantidad.setTextColor(itemView.context.getColor(android.R.color.primary_text_light))
            }
        }
    }
}