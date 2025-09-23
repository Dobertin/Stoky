package repository

import models.Producto
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductoRepository : FirestoreRepository() {

    companion object {
        private const val COLLECTION_NAME = "productos"
    }

    /**
     * Obtener todos los productos
     */
    suspend fun getAllProductos(): Result<List<Producto>> = withContext(Dispatchers.IO) {
        try {
            val result = getCollection(COLLECTION_NAME)
            result.fold(
                onSuccess = { documents ->
                    val productos = documents.mapNotNull { doc ->
                        documentToProducto(doc)
                    }
                    Result.success(productos)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Buscar producto por código
     */
    suspend fun getProductoByCodigo(codigo: String): Result<Producto?> = withContext(Dispatchers.IO) {
        try {
            val result = getCollectionWithQuery(COLLECTION_NAME, "codigo", codigo)
            result.fold(
                onSuccess = { documents ->
                    val producto = documents.firstOrNull()?.let { documentToProducto(it) }
                    Result.success(producto)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Buscar productos por descripción (búsqueda parcial)
     */
    suspend fun searchProductosByDescripcion(query: String): Result<List<Producto>> = withContext(Dispatchers.IO) {
        try {
            val result = getAllProductos()
            result.fold(
                onSuccess = { productos ->
                    val filteredProductos = productos.filter { producto ->
                        producto.descripcion.contains(query, ignoreCase = true) ||
                                producto.categoria.contains(query, ignoreCase = true) ||
                                producto.codigo.contains(query, ignoreCase = true)
                    }
                    Result.success(filteredProductos)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener productos por categoría
     */
    suspend fun getProductosByCategoria(categoria: String): Result<List<Producto>> = withContext(Dispatchers.IO) {
        try {
            val result = getCollectionWithQuery(COLLECTION_NAME, "categoria", categoria)
            result.fold(
                onSuccess = { documents ->
                    val productos = documents.mapNotNull { doc ->
                        documentToProducto(doc)
                    }
                    Result.success(productos)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener productos con stock disponible
     */
    suspend fun getProductosConStock(): Result<List<Producto>> = withContext(Dispatchers.IO) {
        try {
            val result = getAllProductos()
            result.fold(
                onSuccess = { productos ->
                    val productosConStock = productos.filter { it.hasStock() }
                    Result.success(productosConStock)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualizar stock de un producto
     */
    suspend fun updateProductoStock(codigo: String, nuevoStock: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val productoResult = getProductoByCodigo(codigo)
            productoResult.fold(
                onSuccess = { producto ->
                    if (producto != null) {
                        val fields = mapOf(
                            "stock_actual" to nuevoStock,
                            "valor_stock_actual" to (nuevoStock * producto.costo)
                        )
                        updateDocumentFields(COLLECTION_NAME, codigo, fields)
                    } else {
                        Result.failure(Exception("Producto no encontrado"))
                    }
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Crear nuevo producto
     */
    suspend fun createProducto(producto: Producto): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Usar el código como ID del documento
            setDocument(COLLECTION_NAME, producto.codigo, producto).fold(
                onSuccess = {
                    Result.success(producto.codigo)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualizar producto existente
     */
    suspend fun updateProducto(producto: Producto): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            setDocument(COLLECTION_NAME, producto.codigo, producto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Eliminar producto
     */
    suspend fun deleteProducto(codigo: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            deleteDocument(COLLECTION_NAME, codigo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Convertir DocumentSnapshot a Producto
     */
    private fun documentToProducto(document: DocumentSnapshot): Producto? {
        return try {
            Producto(
                codigo = document.getString("codigo") ?: document.id,
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

    /**
     * Obtener todas las categorías disponibles
     */
    suspend fun getAllCategorias(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val result = getAllProductos()
            result.fold(
                onSuccess = { productos ->
                    val categorias = productos.map { it.categoria }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .sorted()
                    Result.success(categorias)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}