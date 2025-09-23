package repository

import models.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class FirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun <T> getDocument(
        collection: String,
        documentId: String,
        clazz: Class<T>
    ): Result<T?> = withContext(Dispatchers.IO) {
        try {
            val document = firestore.collection(collection)
                .document(documentId)
                .get()
                .await()

            if (document.exists()) {
                val data = document.toObject(clazz)
                Result.success(data)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCollection(collection: String): Result<List<DocumentSnapshot>> =
        withContext(Dispatchers.IO) {
            try {
                val documents = firestore.collection(collection)
                    .get()
                    .await()
                Result.success(documents.documents)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getCollectionWithQuery(
        collection: String,
        field: String,
        value: Any
    ): Result<List<DocumentSnapshot>> = withContext(Dispatchers.IO) {
        try {
            val documents = firestore.collection(collection)
                .whereEqualTo(field, value)
                .get()
                .await()
            Result.success(documents.documents)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun <T> addDocument(
        collection: String,
        data: T
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val docRef = firestore.collection(collection)
                .add(data!!)
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun <T> setDocument(
        collection: String,
        documentId: String,
        data: T
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(collection)
                .document(documentId)
                .set(data!!)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun <T> updateDocument(
        collection: String,
        documentId: String,
        data: T
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(collection)
                .document(documentId)
                .set(data!!)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDocumentFields(
        collection: String,
        documentId: String,
        fields: Map<String, Any>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(collection)
                .document(documentId)
                .update(fields)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDocument(
        collection: String,
        documentId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(collection)
                .document(documentId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCollectionOrderBy(
        collection: String,
        orderByField: String,
        direction: Query.Direction = Query.Direction.ASCENDING
    ): Result<List<DocumentSnapshot>> = withContext(Dispatchers.IO) {
        try {
            val documents = firestore.collection(collection)
                .orderBy(orderByField, direction)
                .get()
                .await()
            Result.success(documents.documents)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCollectionWithLimit(
        collection: String,
        limit: Long
    ): Result<List<DocumentSnapshot>> = withContext(Dispatchers.IO) {
        try {
            val documents = firestore.collection(collection)
                .limit(limit)
                .get()
                .await()
            Result.success(documents.documents)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener usuario por email
     */
    suspend fun getUserByEmail(email: String): Usuario? = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = firestore.collection("usuarios")
                .whereEqualTo("correo", email)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents.first()
                document.toObject(Usuario::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtener usuario por ID
     */
    suspend fun getUserById(userId: String): Usuario? = withContext(Dispatchers.IO) {
        try {
            val document = firestore.collection("usuarios")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                document.toObject(Usuario::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Actualizar usuario existente
     */
    suspend fun updateUser(usuario: Usuario): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("usuarios")
                .document(usuario.id)
                .set(usuario)
                .await()

            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Crear nuevo usuario
     */
    suspend fun createUser(usuario: Usuario): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val docRef = if (usuario.id.isNotEmpty()) {
                // Si tiene ID (como en Google), usar ese ID
                firestore.collection("usuarios").document(usuario.id)
            } else {
                // Si no tiene ID, generar uno nuevo
                firestore.collection("usuarios").document()
            }

            val userWithId = usuario.copy(id = docRef.id)

            docRef.set(userWithId).await()
            Result.success(userWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verificar si existe un usuario con el email dado
     */
    suspend fun existsUserWithEmail(email: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = firestore.collection("usuarios")
                .whereEqualTo("correo", email)
                .limit(1)
                .get()
                .await()

            !querySnapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Actualizar última fecha de login del usuario
     */
    suspend fun updateLastLogin(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("usuarios")
                .document(userId)
                .update("ultimoLogin", System.currentTimeMillis())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Método genérico para obtener todos los usuarios (útil para administración)
     */
    suspend fun getAllUsers(): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            val documents = firestore.collection("usuarios")
                .get()
                .await()

            val usuarios = documents.documents.mapNotNull { document ->
                document.toObject(Usuario::class.java)?.copy(id = document.id)
            }

            Result.success(usuarios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener usuarios por rol
     */
    suspend fun getUsersByRole(rol: String): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            val documents = firestore.collection("usuarios")
                .whereEqualTo("rol", rol)
                .get()
                .await()

            val usuarios = documents.documents.mapNotNull { document ->
                document.toObject(Usuario::class.java)?.copy(id = document.id)
            }

            Result.success(usuarios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}