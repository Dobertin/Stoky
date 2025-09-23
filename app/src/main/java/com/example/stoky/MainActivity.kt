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

class MainActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvUserRole: TextView

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
        setContentView(R.layout.activity_main)

        // Configurar padding para system bars
        setupSystemBars()

        // Obtener datos del intent
        getUserDataFromIntent()

        // Inicializar vistas
        initViews()

        // Configurar interfaz según el rol
        setupUIForRole()

        // Configurar listeners
        setupClickListeners()
    }

    private fun setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun getUserDataFromIntent() {
        userRole = intent.getStringExtra("USER_ROLE") ?: "vendedor"
        userName = intent.getStringExtra("USER_NAME") ?: "Usuario"
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvUserRole = findViewById(R.id.tvUserRole)

        cardVentas = findViewById(R.id.cardVentas)
        cardInventario = findViewById(R.id.cardInventario)
        cardSalidas = findViewById(R.id.cardSalidas)
        cardGastos = findViewById(R.id.cardGastos)
        cardReservas = findViewById(R.id.cardReservas)
        cardGraficos = findViewById(R.id.cardGraficos)
        cardReportes = findViewById(R.id.cardReportes)
        cardConfiguracion = findViewById(R.id.cardConfiguracion)
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
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Opcional: mostrar dialog de confirmación para salir
        super.onBackPressed()
    }
}