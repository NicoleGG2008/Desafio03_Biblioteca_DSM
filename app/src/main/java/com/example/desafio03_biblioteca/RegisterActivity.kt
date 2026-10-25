package com.example.desafio03_biblioteca

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var tilNombre: TextInputLayout
    private lateinit var tilCorreo: TextInputLayout
    private lateinit var tilClave: TextInputLayout
    private lateinit var tilConfirmar: TextInputLayout
    private lateinit var etNombre: TextInputEditText
    private lateinit var etCorreo: TextInputEditText
    private lateinit var etClave: TextInputEditText
    private lateinit var etConfirmar: TextInputEditText
    private lateinit var btnRegistrar: MaterialButton
    private lateinit var progreso: ProgressBar
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        Utilidades.ajustarBordes(this)

        auth = FirebaseAuth.getInstance()
        tilNombre = findViewById(R.id.tilNombre)
        tilCorreo = findViewById(R.id.tilCorreo)
        tilClave = findViewById(R.id.tilClave)
        tilConfirmar = findViewById(R.id.tilConfirmar)
        etNombre = findViewById(R.id.etNombre)
        etCorreo = findViewById(R.id.etCorreo)
        etClave = findViewById(R.id.etClave)
        etConfirmar = findViewById(R.id.etConfirmar)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        progreso = findViewById(R.id.progreso)

        btnRegistrar.setOnClickListener { registrar() }
        findViewById<View>(R.id.tvIrLogin).setOnClickListener { finish() }
    }

    private fun registrar() {
        val nombre = etNombre.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val clave = etClave.text.toString()
        val confirmar = etConfirmar.text.toString()

        val errNombre = Validador.validarNombre(nombre)
        val errCorreo = Validador.validarCorreo(correo)
        val errClave = Validador.validarClave(clave)
        val errConfirmar = Validador.validarConfirmacion(clave, confirmar)
        tilNombre.error = errNombre
        tilCorreo.error = errCorreo
        tilClave.error = errClave
        tilConfirmar.error = errConfirmar
        if (errNombre != null || errCorreo != null || errClave != null || errConfirmar != null) return

        mostrarCargando(true)
        auth.createUserWithEmailAndPassword(correo, clave).addOnCompleteListener { tarea ->
            if (!tarea.isSuccessful) {
                mostrarCargando(false)
                Toast.makeText(
                    this, Utilidades.mensajeErrorAuth(tarea.exception), Toast.LENGTH_LONG
                ).show()
                return@addOnCompleteListener
            }

            // Guardar el nombre en el perfil del usuario y esperar a que termine
            // antes de abrir la pantalla principal, para que el saludo ya lo tenga.
            val cambio = UserProfileChangeRequest.Builder()
                .setDisplayName(nombre)
                .build()
            val usuario = tarea.result?.user
            if (usuario == null) {
                irAPrincipal()
                return@addOnCompleteListener
            }
            usuario.updateProfile(cambio).addOnCompleteListener {
                mostrarCargando(false)
                Toast.makeText(this, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show()
                irAPrincipal()
            }
        }
    }

    private fun irAPrincipal() {
        val i = Intent(this, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(i)
        finish()
    }

    private fun mostrarCargando(cargando: Boolean) {
        progreso.visibility = if (cargando) View.VISIBLE else View.GONE
        btnRegistrar.isEnabled = !cargando
    }
}