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

class LoginActivity : AppCompatActivity() {

    private lateinit var tilCorreo: TextInputLayout
    private lateinit var tilClave: TextInputLayout
    private lateinit var etCorreo: TextInputEditText
    private lateinit var etClave: TextInputEditText
    private lateinit var btnIniciarSesion: MaterialButton
    private lateinit var progreso: ProgressBar
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Utilidades.ajustarBordes(this)

        auth = FirebaseAuth.getInstance()
        tilCorreo = findViewById(R.id.tilCorreo)
        tilClave = findViewById(R.id.tilClave)
        etCorreo = findViewById(R.id.etCorreo)
        etClave = findViewById(R.id.etClave)
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion)
        progreso = findViewById(R.id.progreso)

        btnIniciarSesion.setOnClickListener { iniciarSesion() }
        findViewById<View>(R.id.tvIrRegistro).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        // Si ya hay sesión guardada, entra directo (la sesión no se pierde al cerrar la app)
        if (auth.currentUser != null) {
            irAPrincipal()
        }
    }

    private fun iniciarSesion() {
        val correo = etCorreo.text.toString().trim()
        val clave = etClave.text.toString()

        val errCorreo = Validador.validarCorreo(correo)
        val errClave = Validador.validarClave(clave)
        tilCorreo.error = errCorreo
        tilClave.error = errClave
        if (errCorreo != null || errClave != null) return

        mostrarCargando(true)
        auth.signInWithEmailAndPassword(correo, clave).addOnCompleteListener { tarea ->
            mostrarCargando(false)
            if (tarea.isSuccessful) {
                irAPrincipal()
            } else {
                Toast.makeText(
                    this, Utilidades.mensajeErrorAuth(tarea.exception), Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun mostrarCargando(cargando: Boolean) {
        progreso.visibility = if (cargando) View.VISIBLE else View.GONE
        btnIniciarSesion.isEnabled = !cargando
    }

    private fun irAPrincipal() {
        val i = Intent(this, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(i)
        finish()
    }
}
