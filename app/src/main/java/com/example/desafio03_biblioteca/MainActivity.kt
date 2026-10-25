package com.example.desafio03_biblioteca

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity(), AdaptadorLibros.AlInteractuar {

    private lateinit var tvSaludo: TextView
    private lateinit var tvTituloMeta: TextView
    private lateinit var tvMeta: TextView
    private lateinit var tvTerminados: TextView
    private lateinit var tvFaltan: TextView
    private lateinit var tvSinLeyendo: TextView
    private lateinit var progresoMeta: LinearProgressIndicator
    private lateinit var tvPorcentajeMeta: TextView
    private lateinit var tvMensajeMeta: TextView
    private lateinit var btnDefinirMeta: MaterialButton
    private lateinit var adaptador: AdaptadorLibros

    private val todosLosLibros = mutableListOf<Libro>()
    private val librosLeyendo = mutableListOf<Libro>()
    private var metaAnual = 0
    private val year = Utilidades.yearActual()

    private var refLibros: DatabaseReference? = null
    private var refMeta: DatabaseReference? = null
    private var escuchaLibros: ValueEventListener? = null
    private var escuchaMeta: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val usuario = AyudanteFirebase.usuario()
        if (usuario == null) {
            irALogin()
            return
        }
        setContentView(R.layout.activity_main)
        Utilidades.ajustarBordes(this)

        tvSaludo = findViewById(R.id.tvSaludo)
        tvTituloMeta = findViewById(R.id.tvTituloMeta)
        tvMeta = findViewById(R.id.tvMeta)
        tvTerminados = findViewById(R.id.tvTerminados)
        tvFaltan = findViewById(R.id.tvFaltan)
        tvSinLeyendo = findViewById(R.id.tvSinLeyendo)
        progresoMeta = findViewById(R.id.progresoMeta)
        tvPorcentajeMeta = findViewById(R.id.tvPorcentajeMeta)
        tvMensajeMeta = findViewById(R.id.tvMensajeMeta)
        btnDefinirMeta = findViewById(R.id.btnDefinirMeta)

        val nombre = usuario.displayName?.takeIf { it.isNotBlank() }
            ?: usuario.email
            ?: "lector"
        tvSaludo.text = "¡Hola, $nombre!"
        tvTituloMeta.text = "Meta anual $year"

        val rvLeyendo = findViewById<RecyclerView>(R.id.rvLeyendo)
        rvLeyendo.layoutManager = LinearLayoutManager(this)
        rvLeyendo.isNestedScrollingEnabled = false
        adaptador = AdaptadorLibros(librosLeyendo, this)
        rvLeyendo.adapter = adaptador

        btnDefinirMeta.setOnClickListener { mostrarDialogoMeta() }
        findViewById<View>(R.id.btnAgregarLibro).setOnClickListener {
            startActivity(Intent(this, AgregarEditarLibroActivity::class.java))
        }
        findViewById<View>(R.id.btnBiblioteca).setOnClickListener {
            startActivity(Intent(this, BibliotecaActivity::class.java))
        }
        findViewById<View>(R.id.btnEstadisticas).setOnClickListener {
            startActivity(Intent(this, EstadisticasActivity::class.java))
        }
        findViewById<View>(R.id.btnCerrarSesion).setOnClickListener { confirmarCerrarSesion() }
    }

    override fun onStart() {
        super.onStart()
        if (AyudanteFirebase.usuario() == null) return

        val rLibros = AyudanteFirebase.libros()
        val rMeta = AyudanteFirebase.metas().child(year.toString())
        rLibros.keepSynced(true)
        rMeta.keepSynced(true)
        refLibros = rLibros
        refMeta = rMeta

        escuchaLibros = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                todosLosLibros.clear()
                for (hijo in snapshot.children) {
                    val l = hijo.getValue(Libro::class.java)
                    if (l != null) {
                        l.id = hijo.key ?: ""
                        todosLosLibros.add(l)
                    }
                }
                actualizarPantalla()
            }

            override fun onCancelled(error: DatabaseError) {
                if (AyudanteFirebase.usuario() != null)
                    Toast.makeText(
                        this@MainActivity,
                        "Error al leer libros: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
            }
        }
        rLibros.addValueEventListener(escuchaLibros!!)

        escuchaMeta = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val valor = snapshot.getValue(Int::class.java)
                metaAnual = valor ?: 0
                actualizarPantalla()
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        rMeta.addValueEventListener(escuchaMeta!!)
    }

    override fun onStop() {
        super.onStop()
        quitarEscuchas()
    }

    private fun quitarEscuchas() {
        escuchaLibros?.let { refLibros?.removeEventListener(it) }
        escuchaMeta?.let { refMeta?.removeEventListener(it) }
        escuchaLibros = null
        escuchaMeta = null
    }

    private fun actualizarPantalla() {
        // Libros terminados en el year actual
        var terminados = 0
        librosLeyendo.clear()
        for (l in todosLosLibros) {
            if (l.esTerminado() && Utilidades.yearDe(l.fechaTerminado) == year) terminados++
            if (l.estado == Utilidades.ESTADO_LEYENDO) librosLeyendo.add(l)
        }
        adaptador.notifyDataSetChanged()
        tvSinLeyendo.visibility = if (librosLeyendo.isEmpty()) View.VISIBLE else View.GONE

        if (metaAnual <= 0) {
            tvMeta.text = "—"
            tvTerminados.text = terminados.toString()
            tvFaltan.text = "—"
            progresoMeta.progress = 0
            tvPorcentajeMeta.text = "0 %"
            tvMensajeMeta.text =
                "Aún no defines tu meta. ¡Toca el botón y elige cuántos libros quieres leer!"
            btnDefinirMeta.text = "Definir meta"
        } else {
            val faltan = maxOf(0, metaAnual - terminados)
            val porcentaje = minOf(100, terminados * 100 / metaAnual)
            tvMeta.text = metaAnual.toString()
            tvTerminados.text = terminados.toString()
            tvFaltan.text = faltan.toString()
            progresoMeta.progress = porcentaje
            tvPorcentajeMeta.text = "$porcentaje %"
            tvMensajeMeta.text = if (faltan == 0) {
                "🎉 ¡Meta cumplida! Felicidades."
            } else {
                "Te " + (if (faltan == 1) "falta 1 libro" else "faltan $faltan libros") +
                        " para cumplir tu meta."
            }
            btnDefinirMeta.text = "Modificar meta"
        }
    }

    private fun mostrarDialogoMeta() {
        val vista = layoutInflater.inflate(R.layout.dialogo_meta, null)
        val tilMeta = vista.findViewById<TextInputLayout>(R.id.tilMeta)
        val etMeta = vista.findViewById<TextInputEditText>(R.id.etMeta)
        if (metaAnual > 0) etMeta.setText(metaAnual.toString())

        val dialogo = MaterialAlertDialogBuilder(this)
            .setTitle("Meta anual $year")
            .setView(vista)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialogo.setOnShowListener {
            dialogo.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val texto = etMeta.text.toString()
                val error = Validador.validarMeta(texto)
                if (error != null) {
                    tilMeta.error = error
                    return@setOnClickListener
                }
                AyudanteFirebase.metas().child(year.toString())
                    .setValue(texto.trim().toInt())
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            applicationContext,
                            "No se pudo guardar la meta: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                Toast.makeText(this, "Meta guardada", Toast.LENGTH_SHORT).show()
                dialogo.dismiss()
            }
        }
        dialogo.show()
    }

    private fun confirmarCerrarSesion() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Cerrar sesión")
            .setMessage(
                "¿Seguro que deseas cerrar sesión?\n\n" +
                        "Tus libros y tu meta quedan guardados. Los verás al volver a iniciar sesión."
            )
            .setPositiveButton("Cerrar sesión") { _, _ -> cerrarSesion() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cerrarSesion() {
        quitarEscuchas()
        FirebaseAuth.getInstance().signOut()
        irALogin()
    }

    private fun irALogin() {
        val i = Intent(this, LoginActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(i)
        finish()
    }

    // ----- AdaptadorLibros.AlInteractuar -----
    override fun alTocar(libro: Libro) {
        val i = Intent(this, AgregarEditarLibroActivity::class.java)
        i.putExtra(Utilidades.EXTRA_LIBRO, libro)
        startActivity(i)
    }

    override fun alMantener(libro: Libro) {
        Utilidades.confirmarEliminar(this, libro)
    }
}