package com.example.stoky

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import viewModels.LoginViewModel

class RegisterActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    // Views
    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var spinnerRol: Spinner
    private lateinit var btnRegister: Button
    private lateinit var btnBack: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initViews()
        setupSpinner()
        setupObservers()
        setupClickListeners()
    }

    private fun initViews() {
        etNombre = findViewById(R.id.etNombre)
        etCorreo = findViewById(R.id.etCorreo)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        spinnerRol = findViewById(R.id.spinnerRol)
        btnRegister = findViewById(R.id.btnRegister)
        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupSpinner() {
        val roles = arrayOf("vendedor", "admin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRol.adapter = adapter
        spinnerRol.setSelection(0) // Por defecto "vendedor"
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(this, Observer { result ->
            result?.let {
                if (it.isSuccess && it.usuario != null) {
                    // Registro exitoso
                    Toast.makeText(this, "Cuenta creada exitosamente. Bienvenido ${it.usuario.nombre}", Toast.LENGTH_SHORT).show()

                    // Volver al login o navegar según el rol
                    finish() // Volver al login

                } else {
                    // Registro fallido
                    Toast.makeText(this, it.errorMessage ?: "Error al crear la cuenta", Toast.LENGTH_LONG).show()
                }
                viewModel.clearLoginResult()
            }
        })

        viewModel.isLoading.observe(this, Observer { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnRegister.isEnabled = !isLoading
            etNombre.isEnabled = !isLoading
            etCorreo.isEnabled = !isLoading
            etPassword.isEnabled = !isLoading
            etConfirmPassword.isEnabled = !isLoading
            spinnerRol.isEnabled = !isLoading
        })

        viewModel.validationError.observe(this, Observer { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearValidationError()
            }
        })
    }

    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()
            val rol = spinnerRol.selectedItem.toString()

            // Validaciones adicionales
            if (nombre.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa tu nombre", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.registerUser(correo, password, nombre, rol)
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}