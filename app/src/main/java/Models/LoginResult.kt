package Models

data class LoginResult(
    val isSuccess: Boolean,
    val usuario: Usuario? = null,
    val errorMessage: String? = null,
    val tipoLogin: String = "email" // "email" o "google"
)