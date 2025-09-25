package com.example.stoky

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import android.app.AlertDialog
import android.widget.Button
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class MainActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvUserRole: TextView
    private lateinit var btnLogout: Button
    // Cards
    private lateinit var cardVentas: CardView
    private lateinit var cardInventario: CardView
    private lateinit var cardSalidas: CardView
    private lateinit var cardGastos: CardView
    private lateinit var cardReservas: CardView
    private lateinit var cardGraficos: CardView
    private lateinit var cardReportes: CardView
    private lateinit var cardConfiguracion: CardView

    private var userRole: String = "vendedor" // Default role
    private var userName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar si la sesión sigue siendo válida
        if (!LoginActivity.isSessionActive(this)) {
            // Si no hay sesión activa, redirigir al login
            navigateToLogin()
            return
        }

        setContentView(R.layout.activity_main)

        // Configurar padding para system bars
        setupSystemBars()

        // Obtener datos del intent o de la sesión guardada
        getUserDataFromIntent()

        // Inicializar vistas
        initViews()

        // Configurar interfaz según el rol
        setupUIForRole()

        // Configurar listeners
        setupClickListeners()

        // Configurar botón de logout
        setupLogoutButton()
    }

    private fun setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun getUserDataFromIntent() {
        // Primero intentar obtener del intent
        userRole = intent.getStringExtra("USER_ROLE").toString()
        userName = intent.getStringExtra("USER_NAME").toString()

        // Si no hay datos en el intent, obtener de la sesión guardada
        if (userRole.isNullOrEmpty() || userName.isNullOrEmpty()) {
            val userData = LoginActivity.getUserData(this)
            userData?.let {
                userRole = it.first  // role
                userName = it.second // name
            } ?: run {
                userRole = "vendedor"
                userName = "Usuario"
            }
        }
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvUserRole = findViewById(R.id.tvUserRole)
        btnLogout = findViewById(R.id.btnLogout) // Agregar esta línea

        cardVentas = findViewById(R.id.cardVentas)
        cardInventario = findViewById(R.id.cardInventario)
        cardSalidas = findViewById(R.id.cardSalidas)
        cardGastos = findViewById(R.id.cardGastos)
        cardReservas = findViewById(R.id.cardReservas)
        cardGraficos = findViewById(R.id.cardGraficos)
        cardReportes = findViewById(R.id.cardReportes)
        cardConfiguracion = findViewById(R.id.cardConfiguracion)
    }
    private fun setupLogoutButton() {
        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun performLogout() {
        // Limpiar la sesión guardada en SharedPreferences
        LoginActivity.clearSession(this)

        // Cerrar sesión de Firebase Auth
        FirebaseAuth.getInstance().signOut()

        // Si el usuario se logueó con Google, también cerrar sesión de Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            // Navegar de vuelta al LoginActivity
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupUIForRole() {
        // Configurar texto de bienvenida
        tvWelcome.text = "Bienvenido, $userName"
        tvUserRole.text = "Rol: ${userRole.replaceFirstChar { it.uppercase() }}"

        // Mostrar u ocultar card de configuración según el rol
        if (userRole == "admin") {
            cardConfiguracion.visibility = View.VISIBLE
        } else {
            cardConfiguracion.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        cardVentas.setOnClickListener {
            val intent = Intent(this, VentasActivity::class.java)
            intent.putExtra("USER_ROLE", userRole)
            intent.putExtra("USER_NAME", userName)
            startActivity(intent)
        }

        cardInventario.setOnClickListener {
            // TODO: Navegar a InventarioActivity
            navigateToActivity("InventarioActivity")
        }

        cardSalidas.setOnClickListener {
            // TODO: Navegar a SalidasActivity
            navigateToActivity("SalidasActivity")
        }

        cardGastos.setOnClickListener {
            // TODO: Navegar a GastosActivity
            navigateToActivity("GastosActivity")
        }

        cardReservas.setOnClickListener {
            // TODO: Navegar a ReservasActivity
            navigateToActivity("ReservasActivity")
        }

        cardGraficos.setOnClickListener {
            // TODO: Navegar a GraficosActivity
            navigateToActivity("GraficosActivity")
        }

        cardReportes.setOnClickListener {
            // TODO: Navegar a ReportesActivity
            navigateToActivity("ReportesActivity")
        }

        cardConfiguracion.setOnClickListener {
            // TODO: Navegar a ConfiguracionActivity (solo para admin)
            if (userRole == "admin") {
                navigateToActivity("ConfiguracionActivity")
            }
        }
    }

    private fun navigateToActivity(activityName: String) {
        // Por ahora solo mostramos un toast, después puedes implementar la navegación real
        android.widget.Toast.makeText(this, "Navegando a $activityName", android.widget.Toast.LENGTH_SHORT).show()

        // Ejemplo de cómo implementar la navegación real:
        /*
        when (activityName) {
            "InventarioActivity" -> startActivity(Intent(this, InventarioActivity::class.java))
            "SalidasActivity" -> startActivity(Intent(this, SalidasActivity::class.java))
            "GastosActivity" -> startActivity(Intent(this, GastosActivity::class.java))
            "ReservasActivity" -> startActivity(Intent(this, ReservasActivity::class.java))
            "GraficosActivity" -> startActivity(Intent(this, GraficosActivity::class.java))
            "ReportesActivity" -> startActivity(Intent(this, ReportesActivity::class.java))
            "ConfiguracionActivity" -> startActivity(Intent(this, ConfiguracionActivity::class.java))
        }
        */
    }

    // Método para cerrar sesión
    private fun logout() {
        showLogoutDialog()
    }

    override fun onBackPressed() {
        // Opcional: mostrar dialog de confirmación para salir
        super.onBackPressed()
    }
}