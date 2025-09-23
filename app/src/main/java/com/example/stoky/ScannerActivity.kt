package com.example.stoky

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class ScannerActivity : AppCompatActivity() {

    private var hasPermission = false

    // Registramos el lanzador para abrir el escáner
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val scannedCode = result.contents

            // Devolver el resultado a la Activity que llamó
            val resultIntent = Intent()
            resultIntent.putExtra("CODIGO_ESCANEADO", scannedCode)
            setResult(RESULT_OK, resultIntent)

            Toast.makeText(this, "Código escaneado: $scannedCode", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "No se pudo leer el código", Toast.LENGTH_SHORT).show()
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar permisos antes de abrir la cámara
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            hasPermission = true
            startScanner()
        } else {
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun startScanner() {
        if (hasPermission) {
            val options = ScanOptions().apply {
                setPrompt("Escanea un código QR o de barras")
                setBeepEnabled(true)
                setOrientationLocked(false)
                setCameraId(0) // 0 = cámara trasera
            }
            barcodeLauncher.launch(options)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasPermission = true
                    startScanner()
                } else {
                    hasPermission = false
                    Toast.makeText(
                        this,
                        "Permiso de cámara requerido para escanear códigos",
                        Toast.LENGTH_LONG
                    ).show()
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1002
    }
}
