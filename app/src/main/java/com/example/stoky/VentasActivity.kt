package com.example.stoky

import adapters.CarritoAdapter
import adapters.ProductosAdapter
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import managers.CarritoManager
import models.Producto
import repository.FirestoreRepository
import kotlinx.coroutines.*
import com.google.firebase.firestore.DocumentSnapshot
import kotlin.math.log

class VentasActivity : AppCompatActivity() {

    private lateinit var etBuscar: EditText
    private lateinit var rvProductos: RecyclerView
    private lateinit var rvCarrito: RecyclerView
    private lateinit var btnBuscarPorCodigo: Button
    private lateinit var tvTotalCarrito: TextView
    private lateinit var tvCantidadCarrito: TextView
    private lateinit var fabProcesarVenta: FloatingActionButton
    private lateinit var btnLimpiarCarrito: Button

    private lateinit var productosAdapter: ProductosAdapter
    private lateinit var carritoAdapter: CarritoAdapter

    private val repository = FirestoreRepository()
    private val productosList = mutableListOf<Producto>()
    private val productosFilteredList = mutableListOf<Producto>()

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ventas)

        initViews()
        setupRecyclerViews()
        setupListeners()
        loadProducts()
        updateCarritoUI()
    }

    private fun initViews() {
        etBuscar = findViewById(R.id.etBuscar)
        rvProductos = findViewById(R.id.rvProductos)
        rvCarrito = findViewById(R.id.rvCarrito)
        btnBuscarPorCodigo = findViewById(R.id.btnBuscarPorCodigo)
        tvTotalCarrito = findViewById(R.id.tvTotalCarrito)
        tvCantidadCarrito = findViewById(R.id.tvCantidadCarrito)
        fabProcesarVenta = findViewById(R.id.fabProcesarVenta)
        btnLimpiarCarrito = findViewById(R.id.btnLimpiarCarrito)
    }

    private fun setupRecyclerViews() {
        // RecyclerView para productos
        productosAdapter = ProductosAdapter(productosFilteredList) { producto ->
            agregarProductoAlCarrito(producto)
        }
        rvProductos.apply {
            layoutManager = LinearLayoutManager(this@VentasActivity)
            adapter = productosAdapter
        }

        // RecyclerView para carrito
        carritoAdapter = CarritoAdapter(
            onIncrementar = { codigo ->
                CarritoManager.incrementarCantidad(codigo)
                updateCarritoUI()
            },
            onDecrementar = { codigo ->
                CarritoManager.decrementarCantidad(codigo)
                updateCarritoUI()
            },
            onRemover = { codigo ->
                CarritoManager.removerProducto(codigo)
                updateCarritoUI()
            }
        )
        rvCarrito.apply {
            layoutManager = LinearLayoutManager(this@VentasActivity)
            adapter = carritoAdapter
        }
    }

    private fun setupListeners() {
        // Búsqueda en tiempo real
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filtrarProductos(s?.toString() ?: "")
            }
        })

        // Botón para buscar por código (scanner)
        btnBuscarPorCodigo.setOnClickListener {
            abrirScanner()
        }

        // Botón para limpiar carrito
        btnLimpiarCarrito.setOnClickListener {
            confirmarLimpiarCarrito()
        }

        // FAB para procesar venta
        fabProcesarVenta.setOnClickListener {
            if (!CarritoManager.estaVacio()) {
                procesarVenta()
            } else {
                Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadProducts() {
        coroutineScope.launch {
            try {
                Log.d("VentasActivity", "Iniciando carga de productos...")
                val result = repository.getCollection("productos")

                result.onSuccess { documents ->
                    Log.d("VentasActivity", "Productos obtenidos: ${documents.size}")

                    productosList.clear()
                    for (doc in documents) {
                        Log.d("VentasActivity", "Procesando documento ID=${doc.id}")
                        val producto = documentToProducto(doc)
                        if (producto != null) {
                            productosList.add(producto)
                            Log.d("VentasActivity", "Producto agregado: ${producto.codigo} - ${producto.descripcion}")
                        } else {
                            Log.w("VentasActivity", "Documento inválido o incompleto: ${doc.id}")
                        }
                    }

                    productosFilteredList.clear()
                    productosFilteredList.addAll(productosList)
                    productosAdapter.notifyDataSetChanged()
                    Log.d("VentasActivity", "Adaptador actualizado con ${productosList.size} productos")

                }.onFailure { exception ->
                    Log.e("VentasActivity", "Error al cargar productos", exception)
                    Toast.makeText(
                        this@VentasActivity,
                        "Error al cargar productos: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("VentasActivity", "Error inesperado", e)
                Toast.makeText(
                    this@VentasActivity,
                    "Error inesperado: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    private fun documentToProducto(document: DocumentSnapshot): Producto? {
        return try {
            Producto(
                codigo = document.getLong("codigo")?.toInt() ?: 0,
                descripcion = document.getString("descripcion") ?: "",
                categoria = document.getString("categoria") ?: "",
                medida = document.getString("medida") ?: "",
                stock_actual = document.getLong("stock_actual")?.toInt() ?: 0,
                costo = document.getDouble("costo") ?: 0.0,
                precio = document.getDouble("precio") ?: 0.0,
                valor_stock_actual = document.getDouble("valor_stock_actual") ?: 0.0,
                valor_stock_custom = document.getDouble("valor_stock_custom") ?: 0.0,
                valor_vendido = document.getDouble("valor_vendido") ?: 0.0,
                cantidad_vendida = document.getLong("cantidad_vendida")?.toInt() ?: 0,
                cantidad_salidas = document.getLong("cantidad_salidas")?.toInt() ?: 0,
                ganancia = document.getDouble("ganancia") ?: 0.0
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun filtrarProductos(query: String) {
        productosFilteredList.clear()
        if (query.isEmpty()) {
            productosFilteredList.addAll(productosList)
        } else {
            val filtered = productosList.filter { producto ->
                producto.descripcion.contains(query, ignoreCase = true) ||
                        producto.categoria.contains(query, ignoreCase = true)
            }
            productosFilteredList.addAll(filtered)
        }
        productosAdapter.notifyDataSetChanged()
    }

    private fun buscarProductoPorCodigo(codigo: String) {
        val producto = productosList.find { it.codigo == codigo.toInt() }
        if (producto != null) {
            agregarProductoAlCarrito(producto)
        } else {
            Toast.makeText(this, "Producto no encontrado con código: $codigo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun agregarProductoAlCarrito(producto: Producto) {
        if (CarritoManager.agregarProducto(producto)) {
            Toast.makeText(this, "${producto.descripcion} agregado al carrito", Toast.LENGTH_SHORT).show()
            updateCarritoUI()
        } else {
            Toast.makeText(this, "Stock insuficiente para ${producto.descripcion}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCarritoUI() {
        carritoAdapter.actualizarItems(CarritoManager.obtenerItems())
        tvCantidadCarrito.text = "Items: ${CarritoManager.obtenerCantidadTotal()}"
        tvTotalCarrito.text = "Total: S/. ${"%.2f".format(CarritoManager.obtenerTotal())}"

        // Mostrar/ocultar FAB según si hay items en el carrito
        if (CarritoManager.estaVacio()) {
            fabProcesarVenta.hide()
        } else {
            fabProcesarVenta.show()
        }
    }

    private fun abrirScanner() {
        val intent = Intent(this, ScannerActivity::class.java)
        startActivityForResult(intent, SCANNER_REQUEST_CODE)
    }

    private fun confirmarLimpiarCarrito() {
        if (!CarritoManager.estaVacio()) {
            AlertDialog.Builder(this)
                .setTitle("Limpiar Carrito")
                .setMessage("¿Está seguro de que desea limpiar todo el carrito?")
                .setPositiveButton("Sí") { _, _ ->
                    CarritoManager.limpiarCarrito()
                    updateCarritoUI()
                    Toast.makeText(this, "Carrito limpiado", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun procesarVenta() {
        // Aquí implementarías la lógica para procesar la venta
        // Por ahora solo mostramos un resumen
        val items = CarritoManager.obtenerItems()
        val total = CarritoManager.obtenerTotal()

        val mensaje = StringBuilder()
        mensaje.append("Resumen de Venta:\n\n")
        items.forEach { item ->
            mensaje.append("${item.producto.descripcion}\n")
            mensaje.append("Cantidad: ${item.cantidad}\n")
            mensaje.append("Precio: S/. ${"%.2f".format(item.precioUnitario)}\n")
            mensaje.append("Subtotal: S/. ${"%.2f".format(item.getSubtotal())}\n\n")
        }
        mensaje.append("TOTAL: S/. ${"%.2f".format(total)}")

        AlertDialog.Builder(this)
            .setTitle("Confirmar Venta")
            .setMessage(mensaje.toString())
            .setPositiveButton("Confirmar") { _, _ ->
                // Aquí implementarías la lógica para guardar la venta
                Toast.makeText(this, "Venta procesada exitosamente", Toast.LENGTH_LONG).show()
                CarritoManager.limpiarCarrito()
                updateCarritoUI()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SCANNER_REQUEST_CODE && resultCode == RESULT_OK) {
            val codigoEscaneado = data?.getStringExtra("CODIGO_ESCANEADO")
            if (!codigoEscaneado.isNullOrEmpty()) {
                buscarProductoPorCodigo(codigoEscaneado)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    companion object {
        private const val SCANNER_REQUEST_CODE = 1001
    }
}