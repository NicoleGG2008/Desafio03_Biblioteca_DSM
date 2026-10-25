package com.example.desafio03_biblioteca

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class EstadisticasActivity : AppCompatActivity() {

    private lateinit var tvTituloYear: TextView
    private lateinit var tvTotalLibros: TextView
    private lateinit var tvPaginasLeidas: TextView
    private lateinit var tvTituloGeneros: TextView
    private lateinit var tvGeneroMasLeido: TextView
    private lateinit var tvGeneroDetalle: TextView
    private lateinit var tvMejorLibro: TextView
    private lateinit var tvMejorEstrellas: TextView
    private lateinit var tvMejorDetalle: TextView
    private lateinit var tvConteos: Array<TextView>
    private lateinit var barras: Array<LinearProgressIndicator>

    private var refLibros: DatabaseReference? = null
    private var escucha: ValueEventListener? = null
    private val year = Utilidades.yearActual()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estadisticas)
        Utilidades.ajustarBordes(this)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        tvTituloYear = findViewById(R.id.tvTituloYear)
        tvTotalLibros = findViewById(R.id.tvTotalLibros)
        tvPaginasLeidas = findViewById(R.id.tvPaginasLeidas)
        tvTituloGeneros = findViewById(R.id.tvTituloGeneros)
        tvGeneroMasLeido = findViewById(R.id.tvGeneroMasLeido)
        tvGeneroDetalle = findViewById(R.id.tvGeneroDetalle)
        tvMejorLibro = findViewById(R.id.tvMejorLibro)
        tvMejorEstrellas = findViewById(R.id.tvMejorEstrellas)
        tvMejorDetalle = findViewById(R.id.tvMejorDetalle)

        // Mismo orden que Utilidades.GENEROS
        tvConteos = arrayOf(
            findViewById(R.id.tvConteoNovela),
            findViewById(R.id.tvConteoCienciaFiccion),
            findViewById(R.id.tvConteoHistoria),
            findViewById(R.id.tvConteoDesarrollo)
        )
        barras = arrayOf(
            findViewById(R.id.progresoNovela),
            findViewById(R.id.progresoCienciaFiccion),
            findViewById(R.id.progresoHistoria),
            findViewById(R.id.progresoDesarrollo)
        )

        tvTituloYear.text = "Tu año de lectura $year"
        tvTituloGeneros.text = "Libros terminados por género"
    }

    override fun onStart() {
        super.onStart()
        if (AyudanteFirebase.usuario() == null) return

        val ref = AyudanteFirebase.libros()
        refLibros = ref
        escucha = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val libros = mutableListOf<Libro>()
                for (hijo in snapshot.children) {
                    val l = hijo.getValue(Libro::class.java)
                    if (l != null) {
                        l.id = hijo.key ?: ""
                        libros.add(l)
                    }
                }
                calcular(libros)
            }

            override fun onCancelled(error: DatabaseError) {
                if (AyudanteFirebase.usuario() != null)
                    Toast.makeText(
                        this@EstadisticasActivity, "Error: ${error.message}", Toast.LENGTH_LONG
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

    private fun calcular(libros: List<Libro>) {
        val conteo = IntArray(Utilidades.GENEROS.size)
        var paginas = 0
        var totalTerminados = 0
        var mejor: Libro? = null

        for (l in libros) {
            if (!l.esTerminado()) continue
            if (Utilidades.yearDe(l.fechaTerminado) != year) continue

            totalTerminados++
            paginas += l.paginasLeidas
            for (i in Utilidades.GENEROS.indices) {
                if (Utilidades.GENEROS[i] == l.genero) conteo[i]++
            }
            // Mejor calificado; en empate, el que se terminó primero
            val m = mejor
            if (m == null
                || l.calificacion > m.calificacion
                || (l.calificacion == m.calificacion && l.fechaTerminado < m.fechaTerminado)
            ) {
                mejor = l
            }
        }

        // Tarjeta principal
        tvTotalLibros.text = totalTerminados.toString()
        tvPaginasLeidas.text = paginas.toString()

        // Conteo y barras por género (la barra es el % del total de terminados)
        var max = 0
        for (i in conteo.indices) {
            tvConteos[i].text = "${conteo[i]} " + (if (conteo[i] == 1) "libro" else "libros")
            barras[i].progress =
                if (totalTerminados == 0) 0 else conteo[i] * 100 / totalTerminados
            max = maxOf(max, conteo[i])
        }

        // Género más leído
        if (max == 0) {
            tvGeneroMasLeido.text = "Sin datos aún"
            tvGeneroDetalle.text = "Termina un libro para ver tu género favorito."
        } else {
            val nombres = conteo.indices
                .filter { conteo[it] == max }
                .joinToString(" / ") { Utilidades.GENEROS[it] }
            tvGeneroMasLeido.text = nombres
            tvGeneroDetalle.text = "$max " + (if (max == 1) "libro terminado" else "libros terminados")
        }

        // Mejor calificado
        val ganador = mejor
        if (ganador == null) {
            tvMejorLibro.text = "Aún no hay libros calificados"
            tvMejorEstrellas.text = ""
            tvMejorDetalle.text = "Termina y califica un libro para verlo aquí."
        } else {
            tvMejorLibro.text = "“${ganador.titulo}”"
            tvMejorEstrellas.text =
                Utilidades.estrellas(ganador.calificacion) + "  ${ganador.calificacion}/5"
            tvMejorDetalle.text = "de ${ganador.autor}\n" +
                    "Terminado el " + Utilidades.formatearFecha(ganador.fechaTerminado)
        }
    }
}