package com.example.desafio03_biblioteca

import android.util.Patterns

object Validador {

    /** Devuelve el entero o null si no es un entero válido. */
    fun entero(texto: String): Int? = texto.trim().toIntOrNull()

    // ---------- Autenticación ----------

    fun validarNombre(nombre: String): String? {
        val n = nombre.trim()
        if (n.isEmpty()) return "El nombre es obligatorio."
        if (n.length < 2 || n.length > 60)
            return "El nombre debe tener entre 2 y 60 caracteres (actualmente tiene ${n.length})."
        return null
    }
    fun validarCorreo(correo: String): String? {
        if (correo.isEmpty()) return "El correo es obligatorio."
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches())
            return "El formato del correo no es válido (ej: nombre@dominio.com)."
        return null
    }

    fun validarClave(clave: String): String? {
        if (clave.isEmpty()) return "La contraseña es obligatoria."
        if (clave.length < 6) return "La contraseña debe tener al menos 6 caracteres."
        return null
    }

    fun validarConfirmacion(clave: String, confirmacion: String): String? {
        if (confirmacion.isEmpty()) return "Debes confirmar la contraseña."
        if (clave != confirmacion) return "Las contraseñas no coinciden."
        return null
    }

    // ---------- Meta ----------
    fun validarMeta(texto: String): String? {
        val t = texto.trim()
        if (t.isEmpty()) return "Ingresa la meta anual de libros."
        val n = entero(t) ?: return "La meta debe ser un número entero (sin decimales ni letras)."
        if (n <= 0) return "La meta debe ser un número entero mayor que cero."
        return null
    }

    // ---------- Libros ----------
    /** campo: "título" o "autor" */
    fun validarTextoLibro(valor: String, campo: String): String? {
        val v = valor.trim()
        if (v.isEmpty()) return "El $campo es obligatorio."
        if (v.length < 2 || v.length > 60)
            return "El $campo debe tener entre 2 y 60 caracteres (actualmente tiene ${v.length})."
        return null
    }

    fun validarGenero(genero: String): String? {
        if (genero.isEmpty()) return "Selecciona un género de la lista."
        return null
    }

    fun validarTotalPaginas(texto: String): String? {
        val t = texto.trim()
        if (t.isEmpty()) return "El total de páginas es obligatorio."
        val n = entero(t) ?: return "El total de páginas debe ser un número entero (sin decimales ni letras)."
        if (n <= 0) return "El total de páginas debe ser mayor que cero."
        return null
    }

    fun validarPaginasLeidas(texto: String, total: Int?): String? {
        val t = texto.trim()
        if (t.isEmpty()) return "Las páginas leídas son obligatorias (usa 0 si aún no empiezas)."
        val n = entero(t) ?: return "Las páginas leídas deben ser un número entero (sin decimales ni letras)."
        if (n < 0) return "Las páginas leídas no pueden ser negativas (mínimo 0)."
        if (total != null && total > 0 && n > total)
            return "Las páginas leídas ($n) no pueden superar el total de páginas ($total)."
        return null
    }

    fun validarCalificacion(estado: String, calificacion: Int): String? {
        val terminado = estado == Utilidades.ESTADO_TERMINADO
        if (terminado && (calificacion < 1 || calificacion > 5))
            return "La calificación es obligatoria cuando el libro está Terminado (de 1 a 5 estrellas)."
        if (!terminado && calificacion > 0)
            return "Solo se puede calificar un libro cuyo estado sea Terminado."
        return null
    }
}