package ViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import Models.LoginResult
import Repository.AuthRepository
import Repository.FirestoreRepository

class LoginViewModel : ViewModel() {

    private val authRepository = AuthRepository(FirestoreRepository())

    private val _loginResult = MutableLiveData<LoginResult?>()
    val loginResult: MutableLiveData<LoginResult?> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: MutableLiveData<Boolean> = _isLoading

    private val _validationError = MutableLiveData<String?>()
    val validationError: MutableLiveData<String?> = _validationError

    /**
     * Login tradicional con email y contraseña
     */
    fun loginWithEmail(correo: String, password: String) {
        // Validaciones
        if (!isValidEmail(correo)) {
            _validationError.value = "Por favor ingresa un correo válido"
            return
        }

        if (password.length < 6) {
            _validationError.value = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        _isLoading.value = true
        _validationError.value = null

        viewModelScope.launch {
            try {
                val result = authRepository.loginWithEmail(correo, password)
                _loginResult.value = result
            } catch (e: Exception) {
                _loginResult.value = LoginResult(false, null, "Error inesperado: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Login con Google
     */
    fun loginWithGoogle(idToken: String) {
        _isLoading.value = true
        _validationError.value = null

        viewModelScope.launch {
            try {
                val result = authRepository.loginWithGoogle(idToken)
                _loginResult.value = result
            } catch (e: Exception) {
                _loginResult.value = LoginResult(false, null, "Error en login con Google: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Registro de nuevo usuario
     */
    fun registerUser(correo: String, password: String, nombre: String, rol: String = "vendedor") {
        // Validaciones
        if (!isValidEmail(correo)) {
            _validationError.value = "Por favor ingresa un correo válido"
            return
        }

        if (password.length < 6) {
            _validationError.value = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        if (nombre.isBlank()) {
            _validationError.value = "Por favor ingresa tu nombre"
            return
        }

        _isLoading.value = true
        _validationError.value = null

        viewModelScope.launch {
            try {
                val nuevoUsuario = Models.Usuario(
                    correo = correo,
                    nombre = nombre,
                    pwd = password,
                    rol = rol,
                    tipoLogin = "email"
                )

                val result = authRepository.createUser(nuevoUsuario)
                if (result.isSuccess) {
                    // Después de crear, hacer login automático
                    loginWithEmail(correo, password)
                } else {
                    _loginResult.value = LoginResult(false, null, "Error al registrar usuario: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _loginResult.value = LoginResult(false, null, "Error inesperado: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Limpiar resultado del login
     */
    fun clearLoginResult() {
        _loginResult.value = null
    }

    /**
     * Limpiar error de validación
     */
    fun clearValidationError() {
        _validationError.value = null
    }

    /**
     * Logout
     */
    fun logout() {
        authRepository.logout()
    }

    /**
     * Validar formato de email
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}