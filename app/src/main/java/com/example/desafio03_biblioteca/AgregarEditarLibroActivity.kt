package com.example.desafio03_biblioteca

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AgregarEditarLibroActivity : AppCompatActivity() {

    private lateinit var tilTitulo: TextInputLayout
    private lateinit var tilAutor: TextInputLayout
    private lateinit var tilGenero: TextInputLayout
    private lateinit var tilTotalPaginas: TextInputLayout
    private lateinit var tilPaginasLeidas: TextInputLayout
    private lateinit var tilEstado: TextInputLayout
    private lateinit var etTitulo: TextInputEditText
    private lateinit var etAutor: TextInputEditText
    private lateinit var etTotalPaginas: TextInputEditText
    private lateinit var etPaginasLeidas: TextInputEditText
    private lateinit var actGenero: AutoCompleteTextView
    private lateinit var actEstado: AutoCompleteTextView
    private lateinit var contenedorCalificacion: View
    private lateinit var rbCalificacion: RatingBar
    private lateinit var tvErrorCalificacion: TextView

    private var libroEditado: Libro? = null // null = libro nuevo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_editar_libro)
        Utilidades.ajustarBordes(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        tilTitulo = findViewById(R.id.tilTitulo)
        tilAutor = findViewById(R.id.tilAutor)
        tilGenero = findViewById(R.id.tilGenero)
        tilTotalPaginas = findViewById(R.id.tilTotalPaginas)
        tilPaginasLeidas = findViewById(R.id.tilPaginasLeidas)
        tilEstado = findViewById(R.id.tilEstado)
        etTitulo = findViewById(R.id.etTitulo)
        etAutor = findViewById(R.id.etAutor)
        etTotalPaginas = findViewById(R.id.etTotalPaginas)
        etPaginasLeidas = findViewById(R.id.etPaginasLeidas)
        actGenero = findViewById(R.id.actGenero)
        actEstado = findViewById(R.id.actEstado)
        contenedorCalificacion = findViewById(R.id.contenedorCalificacion)
        rbCalificacion = findViewById(R.id.rbCalificacion)
        tvErrorCalificacion = findViewById(R.id.tvErrorCalificacion)

        actGenero.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, Utilidades.GENEROS)
        )
        actEstado.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, Utilidades.ESTADOS)
        )
        actEstado.setText(Utilidades.ESTADO_POR_LEER, false)
        etPaginasLeidas.setText("0")
        actEstado.setOnItemClickListener { _, _, _, _ -> actualizarCalificacionVisible() }

        libroEditado = IntentCompat.getSerializableExtra(
            intent, Utilidades.EXTRA_LIBRO, Libro::class.java
        )
        val editado = libroEditado
        if (editado != null) {
            toolbar.title = "Editar libro"
            etTitulo.setText(editado.titulo)
            etAutor.setText(editado.autor)
            actGenero.setText(editado.genero, false)
            etTotalPaginas.setText(editado.totalPaginas.toString())
            etPaginasLeidas.setText(editado.paginasLeidas.toString())
            actEstado.setText(editado.estado, false)
            actualizarCalificacionVisible()
            rbCalificacion.rating = editado.calificacion.toFloat()
        } else {
            toolbar.title = "Agregar libro"
            actualizarCalificacionVisible()
        }

        findViewById<View>(R.id.btnGuardar).setOnClickListener { guardar() }
        findViewById<View>(R.id.btnCancelar).setOnClickListener { finish() }
    }

    private fun actualizarCalificacionVisible() {
        val terminado = actEstado.text.toString() == Utilidades.ESTADO_TERMINADO
        contenedorCalificacion.visibility = if (terminado) View.VISIBLE else View.GONE
        if (!terminado) rbCalificacion.rating = 0f
        tvErrorCalificacion.visibility = View.GONE
    }

    /** Muestra el error en el campo y devuelve true si había error. */
    private fun mostrar(til: TextInputLayout, error: String?): Boolean {
        til.error = error
        return error != null
    }

    private fun guardar() {
        val titulo = etTitulo.text.toString().trim()
        val autor = etAutor.text.toString().trim()
        val genero = actGenero.text.toString().trim()
        val totalTxt = etTotalPaginas.text.toString()
        val leidasTxt = etPaginasLeidas.text.toString()
        val estado = actEstado.text.toString().trim()
        val calificacion = Math.round(rbCalificacion.rating)
        val total = Validador.entero(totalTxt)

        var hayError = false
        hayError = mostrar(tilTitulo, Validador.validarTextoLibro(titulo, "título")) or hayError
        hayError = mostrar(tilAutor, Validador.validarTextoLibro(autor, "autor")) or hayError
        hayError = mostrar(tilGenero, Validador.validarGenero(genero)) or hayError
        hayError = mostrar(tilTotalPaginas, Validador.validarTotalPaginas(totalTxt)) or hayError
        hayError = mostrar(
            tilPaginasLeidas, Validador.validarPaginasLeidas(leidasTxt, total)
        ) or hayError
        hayError = mostrar(
            tilEstado, if (estado.isEmpty()) "Selecciona un estado." else null
        ) or hayError

        val errCal = Validador.validarCalificacion(estado, calificacion)
        if (errCal != null) {
            tvErrorCalificacion.text = errCal
            tvErrorCalificacion.visibility = View.VISIBLE
            hayError = true
        } else {
            tvErrorCalificacion.visibility = View.GONE
        }

        if (hayError || total == null) {
            Toast.makeText(this, "Corrige los campos marcados en rojo.", Toast.LENGTH_SHORT).show()
            return
        }

        val terminado = estado == Utilidades.ESTADO_TERMINADO
        val leidas = leidasTxt.trim().toInt()
        val anterior = libroEditado

        val id = anterior?.id ?: AyudanteFirebase.libros().push().key ?: return

        // Fecha de término: se guarda al marcar como Terminado (y se conserva si ya lo estaba)
        var fecha = 0L
        if (terminado) {
            val yaEstabaTerminado = anterior != null && anterior.esTerminado()
                    && anterior.fechaTerminado > 0
            fecha = if (yaEstabaTerminado) anterior!!.fechaTerminado else System.currentTimeMillis()
        }

        val libro = Libro(
            id = id,
            titulo = titulo,
            autor = autor,
            genero = genero,
            estado = estado,
            totalPaginas = total,
            // Un libro Terminado tiene todas sus páginas leídas
            paginasLeidas = if (terminado) total else leidas,
            calificacion = if (terminado) calificacion else 0,
            fechaTerminado = fecha
        )

        AyudanteFirebase.libros().child(id).setValue(libro)
            .addOnFailureListener { e ->
                Toast.makeText(
                    applicationContext, "No se pudo guardar: ${e.message}", Toast.LENGTH_LONG
                ).show()
            }

        Toast.makeText(this, "Libro guardado", Toast.LENGTH_SHORT).show()
        finish()
    }
}