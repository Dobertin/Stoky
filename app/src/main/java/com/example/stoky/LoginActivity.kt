package com.example.stoky

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import ViewModels.LoginViewModel
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.example.stoky.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    // Google Sign-In
    private lateinit var googleSignInClient: GoogleSignInClient

    // Views
    private lateinit var etCorreo: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogleLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var tvRegister: TextView
    private lateinit var progressBar: ProgressBar

    // Activity Result Launcher para Google Sign-In
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { idToken ->
                    viewModel.loginWithGoogle(idToken)
                } ?: run {
                    Toast.makeText(this, "Error al obtener token de Google", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Error en Google Sign-In: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initGoogleSignIn()
        initViews()
        setupObservers()
        setupClickListeners()
    }

    private fun initGoogleSignIn() {
        // Configurar Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Necesitas agregar esto en strings.xml
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun initViews() {
        etCorreo = findViewById(R.id.etCorreo)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin)
        btnRegister = findViewById(R.id.btnRegister)
        tvRegister = findViewById(R.id.tvRegister)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(this, Observer { result ->
            result?.let {
                if (it.isSuccess && it.usuario != null) {
                    // Login exitoso
                    val mensaje = when (it.tipoLogin) {
                        "google" -> "Bienvenido ${it.usuario.nombre} (Google)"
                        else -> "Bienvenido ${it.usuario.nombre}"
                    }
                    Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()

                    // Navegar según el rol
                    when (it.usuario.rol) {
                        "admin" -> {
                            // startActivity(Intent(this, AdminActivity::class.java))
                        }
                        "vendedor" -> {
                            // startActivity(Intent(this, VendedorActivity::class.java))
                        }
                    }
                    finish()
                } else {
                    // Login fallido
                    Toast.makeText(this, it.errorMessage ?: "Error de login", Toast.LENGTH_LONG).show()
                }
                viewModel.clearLoginResult()
            }
        })

        viewModel.isLoading.observe(this, Observer { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnLogin.isEnabled = !isLoading
            btnGoogleLogin.isEnabled = !isLoading
            btnRegister.isEnabled = !isLoading
            etCorreo.isEnabled = !isLoading
            etPassword.isEnabled = !isLoading
        })

        viewModel.validationError.observe(this, Observer { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearValidationError()
            }
        })
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val password = etPassword.text.toString()

            viewModel.loginWithEmail(correo, password)
        }

        btnGoogleLogin.setOnClickListener {
            signInWithGoogle()
        }

        btnRegister.setOnClickListener {
            // Ir a pantalla de registro o mostrar dialog
            showRegisterDialog()
        }

        tvRegister.setOnClickListener {
            // Ir a pantalla de registro
            showRegisterDialog()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun showRegisterDialog() {
        // Aquí puedes abrir una nueva Activity o mostrar un Dialog para registro
        // Por ahora, mostraré un Toast indicando que se debe implementar
        Toast.makeText(this, "Pantalla de registro por implementar", Toast.LENGTH_SHORT).show()

        // Ejemplo de registro directo (puedes crear una Activity separada)
        // val intent = Intent(this, RegisterActivity::class.java)
        // startActivity(intent)
    }
}