package repository

import models.LoginResult
import models.Usuario
import utils.CryptoUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository(private val firestoreRepository: FirestoreRepository) {

    private val firebaseAuth = FirebaseAuth.getInstance()

    /**
     * Login con Google mejorado que maneja cuentas existentes
     */
    suspend fun loginWithGoogleImproved(idToken: String): LoginResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            // Intentar login con Google
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val email = firebaseUser.email ?: ""

                // Verificar si ya existe un usuario con este email
                val existingUser = firestoreRepository.getUserByEmail(email)

                if (existingUser != null) {
                    when (existingUser.tipoLogin) {
                        "email" -> {
                            // Usuario existe con email/password, vincularlo automáticamente
                            val updatedUser = existingUser.copy(
                                tipoLogin = "both",
                                googleId = firebaseUser.uid,
                                ultimoLogin = System.currentTimeMillis()
                            )

                            // Actualizar en Firestore
                            val updateResult = firestoreRepository.updateUser(updatedUser)
                            if (updateResult.isSuccess) {
                                LoginResult(true, updatedUser, "Cuenta vinculada exitosamente")
                            } else {
                                LoginResult(false, null, "Error vinculando cuenta")
                            }
                        }
                        "google", "both" -> {
                            // Usuario ya tiene Google habilitado, login normal
                            val updatedUser = existingUser.copy(
                                ultimoLogin = System.currentTimeMillis()
                            )
                            firestoreRepository.updateUser(updatedUser)
                            LoginResult(true, updatedUser, "Login exitoso")
                        }
                        else -> {
                            LoginResult(false, null, "Tipo de login no reconocido")
                        }
                    }
                } else {
                    // No existe usuario con este email, crear nuevo usuario Google
                    val newUser = Usuario(
                        id = firebaseUser.uid,
                        correo = email,
                        nombre = firebaseUser.displayName ?: "Usuario Google",
                        rol = "vendedor",
                        tipoLogin = "google",
                        googleId = firebaseUser.uid,
                        fechaCreacion = System.currentTimeMillis(),
                        ultimoLogin = System.currentTimeMillis(),
                        activo = true
                    )

                    val createResult = firestoreRepository.createUser(newUser)
                    if (createResult.isSuccess) {
                        LoginResult(true, newUser, "Usuario creado exitosamente")
                    } else {
                        LoginResult(false, null, "Error creando usuario")
                    }
                }
            } else {
                LoginResult(false, null, "Error obteniendo datos de Google")
            }
        } catch (e: Exception) {
            when (e) {
                is FirebaseAuthUserCollisionException -> {
                    LoginResult(false, null, "Ya existe una cuenta con este email")
                }
                else -> {
                    LoginResult(false, null, "Error en login con Google: ${e.message}")
                }
            }
        }
    }

    /**
     * Vincular cuenta de Google a usuario existente
     */
    suspend fun linkGoogleAccount(existingUser: Usuario): LoginResult {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val updatedUser = existingUser.copy(
                    tipoLogin = "both",
                    googleId = currentUser.uid,
                    ultimoLogin = System.currentTimeMillis()
                )

                val updateResult = firestoreRepository.updateUser(updatedUser)
                if (updateResult.isSuccess) {
                    LoginResult(true, updatedUser, "Cuentas vinculadas exitosamente")
                } else {
                    LoginResult(false, null, "Error vinculando cuentas")
                }
            } else {
                LoginResult(false, null, "No hay usuario de Google autenticado")
            }
        } catch (e: Exception) {
            LoginResult(false, null, "Error vinculando cuentas: ${e.message}")
        }
    }

    /**
     * Login tradicional con email y contraseña
     */
    suspend fun loginWithEmail(correo: String, password: String): LoginResult {
        return try {
            // Buscar usuario por correo en la nueva colección "usuarios"
            val result = firestoreRepository.getCollection("usuarios")

            if (result.isSuccess) {
                val documents = result.getOrNull() ?: emptyList()

                for (document in documents) {
                    val usuario = Usuario(
                        id = document.id,
                        correo = document.getString("correo") ?: "",
                        nombre = document.getString("nombre") ?: "",
                        pwd = document.getString("pwd") ?: "",
                        rol = document.getString("rol") ?: "vendedor",
                        tipoLogin = document.getString("tipoLogin") ?: "email",
                        googleId = document.getString("googleId") ?: "",
                        fechaCreacion = document.getLong("fechaCreacion") ?: System.currentTimeMillis(),
                        activo = document.getBoolean("activo") ?: true
                    )

                    if (usuario.correo == correo && usuario.activo) {
                        // Verificar si es usuario de login tradicional
                        if (usuario.tipoLogin == "email") {
                            if (usuario.pwd.isNotEmpty() &&
                                CryptoUtils.verifyPassword(password, usuario.pwd)) {
                                return LoginResult(true, usuario, tipoLogin = "email")
                            } else {
                                return LoginResult(false, null, "Contraseña incorrecta")
                            }
                        } else {
                            return LoginResult(false, null, "Este usuario debe iniciar sesión con Google")
                        }
                    }
                }

                LoginResult(false, null, "Usuario no encontrado o inactivo")
            } else {
                LoginResult(false, null, "Error al conectar con la base de datos")
            }
        } catch (e: Exception) {
            LoginResult(false, null, "Error inesperado: ${e.message}")
        }
    }

    /**
     * Login con Google
     */
    suspend fun loginWithGoogle(idToken: String): LoginResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // Buscar si el usuario ya existe en Firestore
                val result = firestoreRepository.getCollection("usuarios")

                if (result.isSuccess) {
                    val documents = result.getOrNull() ?: emptyList()

                    // Buscar por correo o googleId
                    for (document in documents) {
                        val usuario = Usuario(
                            id = document.id,
                            correo = document.getString("correo") ?: "",
                            nombre = document.getString("nombre") ?: "",
                            pwd = document.getString("pwd") ?: "",
                            rol = document.getString("rol") ?: "vendedor",
                            tipoLogin = document.getString("tipoLogin") ?: "email",
                            googleId = document.getString("googleId") ?: "",
                            fechaCreacion = document.getLong("fechaCreacion") ?: System.currentTimeMillis(),
                            activo = document.getBoolean("activo") ?: true
                        )

                        if ((usuario.correo == firebaseUser.email ||
                                    usuario.googleId == firebaseUser.uid) && usuario.activo) {
                            return LoginResult(true, usuario, tipoLogin = "google")
                        }
                    }

                    // Si no existe, crear nuevo usuario
                    val nuevoUsuario = Usuario(
                        correo = firebaseUser.email ?: "",
                        nombre = firebaseUser.displayName ?: "Usuario",
                        rol = "vendedor", // Rol por defecto
                        tipoLogin = "google",
                        googleId = firebaseUser.uid,
                        activo = true
                    )

                    val createResult = createUser(nuevoUsuario)
                    if (createResult.isSuccess) {
                        val userId = createResult.getOrNull()
                        return LoginResult(true, nuevoUsuario.copy(id = userId ?: ""), tipoLogin = "google")
                    } else {
                        return LoginResult(false, null, "Error al crear el usuario")
                    }
                }

                LoginResult(false, null, "Error al acceder a la base de datos")
            } else {
                LoginResult(false, null, "Error en la autenticación con Google")
            }
        } catch (e: Exception) {
            LoginResult(false, null, "Error en login con Google: ${e.message}")
        }
    }

    /**
     * Crear nuevo usuario
     */
    suspend fun createUser(usuario: Usuario): Result<String> {
        return try {
            // Verificar que no exista ya un usuario con ese correo
            val existingUser = getUserByEmail(usuario.correo)
            if (existingUser != null) {
                return Result.failure(Exception("Ya existe un usuario con ese correo"))
            }

            // Encriptar contraseña si es login tradicional
            val usuarioToSave = if (usuario.tipoLogin == "email" && usuario.pwd.isNotEmpty()) {
                usuario.copy(pwd = CryptoUtils.encryptPassword(usuario.pwd))
            } else {
                usuario
            }

            val userMap = mapOf(
                "correo" to usuarioToSave.correo,
                "nombre" to usuarioToSave.nombre,
                "pwd" to usuarioToSave.pwd,
                "rol" to usuarioToSave.rol,
                "tipoLogin" to usuarioToSave.tipoLogin,
                "googleId" to usuarioToSave.googleId,
                "fechaCreacion" to usuarioToSave.fechaCreacion,
                "activo" to usuarioToSave.activo
            )

            firestoreRepository.addDocument("usuarios", userMap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener usuario por email
     */
    private suspend fun getUserByEmail(correo: String): Usuario? {
        return try {
            val result = firestoreRepository.getCollection("usuarios")
            if (result.isSuccess) {
                val documents = result.getOrNull() ?: emptyList()
                for (document in documents) {
                    val email = document.getString("correo") ?: ""
                    if (email == correo) {
                        return Usuario(
                            id = document.id,
                            correo = email,
                            nombre = document.getString("nombre") ?: "",
                            pwd = document.getString("pwd") ?: "",
                            rol = document.getString("rol") ?: "vendedor",
                            tipoLogin = document.getString("tipoLogin") ?: "email",
                            googleId = document.getString("googleId") ?: "",
                            fechaCreacion = document.getLong("fechaCreacion") ?: System.currentTimeMillis(),
                            activo = document.getBoolean("activo") ?: true
                        )
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Actualizar contraseña de usuario
     */
    suspend fun updatePassword(userId: String, newPassword: String): Result<Unit> {
        val encryptedPassword = CryptoUtils.encryptPassword(newPassword)
        return firestoreRepository.updateDocument(
            "usuarios",
            userId,
            mapOf("pwd" to encryptedPassword)
        )
    }

    /**
     * Logout
     */
    fun logout() {
        firebaseAuth.signOut()
    }

    /**
     * Obtener usuario actual de Firebase Auth
     */
    fun getCurrentFirebaseUser() = firebaseAuth.currentUser
}