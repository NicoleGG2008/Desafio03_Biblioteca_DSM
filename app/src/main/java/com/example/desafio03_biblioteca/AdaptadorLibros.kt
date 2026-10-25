package com.example.desafio03_biblioteca

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator

class AdaptadorLibros(
    private val libros: List<Libro>,
    private val escucha: AlInteractuar
) : RecyclerView.Adapter<AdaptadorLibros.VistaLibro>() {

    interface AlInteractuar {
        fun alTocar(libro: Libro)      // editar
        fun alMantener(libro: Libro)   // eliminar
    }

    override fun onCreateViewHolder(padre: ViewGroup, tipo: Int): VistaLibro {
        val vista = LayoutInflater.from(padre.context).inflate(R.layout.item_libro, padre, false)
        return VistaLibro(vista)
    }

    override fun onBindViewHolder(h: VistaLibro, posicion: Int) {
        val l = libros[posicion]
        h.tvTitulo.text = l.titulo
        h.tvAutor.text = "de ${l.autor}"
        h.tvGeneroEstado.text = "${l.genero}  •  ${l.estado}"
        h.progresoLibro.progress = l.porcentaje()
        h.tvPorcentaje.text = "${l.porcentaje()}%  (${l.paginasLeidas}/${l.totalPaginas})"

        if (l.esTerminado()) {
            h.tvCalificacion.visibility = View.VISIBLE
            h.tvCalificacion.text = "${Utilidades.estrellas(l.calificacion)}  ${l.calificacion}/5"
        } else {
            h.tvCalificacion.visibility = View.GONE
        }

        h.itemView.setOnClickListener { escucha.alTocar(l) }
        h.itemView.setOnLongClickListener {
            escucha.alMantener(l)
            true
        }
    }

    override fun getItemCount(): Int = libros.size

    class VistaLibro(vista: View) : RecyclerView.ViewHolder(vista) {
        val tvTitulo: TextView = vista.findViewById(R.id.tvTitulo)
        val tvAutor: TextView = vista.findViewById(R.id.tvAutor)
        val tvGeneroEstado: TextView = vista.findViewById(R.id.tvGeneroEstado)
        val tvPorcentaje: TextView = vista.findViewById(R.id.tvPorcentaje)
        val tvCalificacion: TextView = vista.findViewById(R.id.tvCalificacion)
        val progresoLibro: LinearProgressIndicator = vista.findViewById(R.id.progresoLibro)
    }
}