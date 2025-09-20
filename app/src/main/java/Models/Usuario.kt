package Models

data class Usuario(
    val id: String = "",
    val correo: String = "",
    val nombre: String = "",
    val pwd: String = "", // Solo para usuarios con login tradicional
    val rol: String = "vendedor", // Por defecto vendedor
    val tipoLogin: String = "email", // "email" o "google"
    val googleId: String = "", // ID de Google si usa login con Google
    val fechaCreacion: Long = System.currentTimeMillis(),
    val activo: Boolean = true
)