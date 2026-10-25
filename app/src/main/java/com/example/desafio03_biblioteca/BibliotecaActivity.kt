package com.example.desafio03_biblioteca

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class BibliotecaActivity : AppCompatActivity(), AdaptadorLibros.AlInteractuar {

    private val libros = mutableListOf<Libro>()
    private lateinit var adaptador: AdaptadorLibros
    private lateinit var rvLibros: RecyclerView
    private lateinit var tvVacio: TextView

    private var refLibros: DatabaseReference? = null
    private var escucha: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biblioteca)
        Utilidades.ajustarBordes(this)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        rvLibros = findViewById(R.id.rvLibros)
        tvVacio = findViewById(R.id.tvVacio)
        rvLibros.layoutManager = LinearLayoutManager(this)
        adaptador = AdaptadorLibros(libros, this)
        rvLibros.adapter = adaptador

        findViewById<View>(R.id.fabAgregar).setOnClickListener {
            startActivity(Intent(this, AgregarEditarLibroActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        if (AyudanteFirebase.usuario() == null) return

        val ref = AyudanteFirebase.libros()
        refLibros = ref
        escucha = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                libros.clear()
                for (hijo in snapshot.children) {
                    val l = hijo.getValue(Libro::class.java)
                    if (l != null) {
                        l.id = hijo.key ?: ""
                        libros.add(l)
                    }
                }
                libros.sortWith { a, b -> a.titulo.compareTo(b.titulo, ignoreCase = true) }
                adaptador.notifyDataSetChanged()

                val vacio = libros.isEmpty()
                tvVacio.visibility = if (vacio) View.VISIBLE else View.GONE
                rvLibros.visibility = if (vacio) View.GONE else View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                if (AyudanteFirebase.usuario() != null)
                    Toast.makeText(
                        this@BibliotecaActivity, "Error: ${error.message}", Toast.LENGTH_LONG
                    ).show()
            }
        }
        ref.addValueEventListener(escucha!!)
    }

    override fun onStop() {
        super.onStop()
        escucha?.let { refLibros?.removeEventListener(it) }
        escucha = null
    }

    override fun alTocar(libro: Libro) {
        val i = Intent(this, AgregarEditarLibroActivity::class.java)
        i.putExtra(Utilidades.EXTRA_LIBRO, libro)
        startActivity(i)
    }

    override fun alMantener(libro: Libro) {
        Utilidades.confirmarEliminar(this, libro)
    }
}