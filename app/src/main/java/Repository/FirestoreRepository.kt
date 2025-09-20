package Repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirestoreRepository {
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
}