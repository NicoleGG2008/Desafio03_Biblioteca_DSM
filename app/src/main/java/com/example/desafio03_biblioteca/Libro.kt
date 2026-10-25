package com.example.desafio03_biblioteca

import java.io.Serializable

// Todos los campos tienen valor por defecto: Firebase necesita un constructor vacío.
data class Libro(
    var id: String = "",
    var titulo: String = "",
    var autor: String = "",
    var genero: String = "",
    var estado: String = "",
    var totalPaginas: Int = 0,
    var paginasLeidas: Int = 0,
    var calificacion: Int = 0,        // 0 = sin calificar, 1..5
    var fechaTerminado: Long = 0      // milisegundos; 0 = no terminado
) : Serializable {

    // No empiezan con "get", por eso Firebase no las guarda en la base de datos
    fun porcentaje(): Int {
        if (totalPaginas <= 0) return 0
        return minOf(100, Math.round(paginasLeidas * 100f / totalPaginas))
    }

    fun esTerminado(): Boolean = estado == Utilidades.ESTADO_TERMINADO
}