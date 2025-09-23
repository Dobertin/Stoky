package models

data class LoginResult(
    val isSuccess: Boolean,
    val usuario: Usuario? = null,
    val errorMessage: String? = null,
    val tipoLogin: String = "" // "email", "google", "both"
) {
    // Constructor alternativo para mantener compatibilidad
    constructor(isSuccess: Boolean, usuario: Usuario?, message: String) :
            this(isSuccess, usuario, if (isSuccess) null else message, if (isSuccess && usuario != null) usuario.tipoLogin else "")
}