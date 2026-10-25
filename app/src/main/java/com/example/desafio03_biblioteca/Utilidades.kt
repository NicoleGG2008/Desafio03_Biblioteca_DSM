package com.example.desafio03_biblioteca

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Utilidades {

    val GENEROS = arrayOf("Novela", "Ciencia ficción", "Historia", "Desarrollo personal")

    const val ESTADO_POR_LEER = "Por leer"
    const val ESTADO_LEYENDO = "Leyendo"
    const val ESTADO_TERMINADO = "Terminado"
    val ESTADOS = arrayOf(ESTADO_POR_LEER, ESTADO_LEYENDO, ESTADO_TERMINADO)

    const val EXTRA_LIBRO = "libro"

    /**
     * Android 15+ dibuja las apps de borde a borde (debajo de la barra de estado y de navegación).
     * Esto añade el margen necesario para que el contenido no quede tapado.
     */
    fun ajustarBordes(activity: Activity) {
        val raiz = activity.findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(raiz) { v, insets ->
            val barras = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
                        or WindowInsetsCompat.Type.ime()
            )
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }
    }

    fun yearActual(): Int = Calendar.getInstance().get(Calendar.YEAR)

    fun yearDe(milisegundos: Long): Int {
        val c = Calendar.getInstance()
        c.timeInMillis = milisegundos
        return c.get(Calendar.YEAR)
    }

    fun formatearFecha(milisegundos: Long): String =
        SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-SV")).format(Date(milisegundos))

    fun estrellas(cantidad: Int): String {
        val sb = StringBuilder()
        for (i in 1..5) {
            sb.append(if (i <= cantidad) "★" else "☆")
        }
        return sb.toString()
    }

    fun mensajeErrorAuth(e: Exception?): String = when (e) {
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta registrada con ese correo."
        is FirebaseAuthWeakPasswordException -> "La contraseña es demasiado débil. Usa al menos 6 caracteres."
        is FirebaseAuthInvalidUserException -> "No existe una cuenta con ese correo."
        is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos."
        is FirebaseNetworkException -> "Sin conexión a internet. Verifica tu red e inténtalo de nuevo."
        else -> "Ocurrió un error: " + (e?.message ?: "desconocido")
    }

    fun confirmarEliminar(contexto: Context, libro: Libro) {
        MaterialAlertDialogBuilder(contexto)
            .setTitle("Eliminar libro")
            .setMessage("¿Seguro que deseas eliminar \"${libro.titulo}\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                AyudanteFirebase.libros().child(libro.id).removeValue()
                Toast.makeText(contexto, "Libro eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}