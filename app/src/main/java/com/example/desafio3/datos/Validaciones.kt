package com.example.desafio3.datos

const val CAMPO_TITULO = "titulo"
const val CAMPO_AUTOR = "autor"
const val CAMPO_TOTAL = "total"
const val CAMPO_LEIDAS = "leidas"
const val CAMPO_CALIFICACION = "calificacion"

private fun validarTexto(nombre: String, valor: String): String? {
    val largo = valor.trim().length
    return when {
        largo == 0 -> "El $nombre es obligatorio"
        largo < 2 || largo > 60 -> "El $nombre debe tener entre 2 y 60 caracteres (tiene $largo)"
        else -> null
    }
}

// Devuelve un mapa campo -> mensaje. Si está vacío el libro se puede guardar
fun validarLibro(
    titulo: String,
    autor: String,
    totalTexto: String,
    leidasTexto: String,
    estado: String,
    calificacion: Int
): Map<String, String> {
    val errores = mutableMapOf<String, String>()

    validarTexto("título", titulo)?.let { errores[CAMPO_TITULO] = it }
    validarTexto("autor", autor)?.let { errores[CAMPO_AUTOR] = it }

    val total = totalTexto.trim().toIntOrNull()
    when {
        totalTexto.isBlank() -> errores[CAMPO_TOTAL] = "El total de páginas es obligatorio"
        total == null -> errores[CAMPO_TOTAL] = "El total de páginas debe ser un número entero"
        total <= 0 -> errores[CAMPO_TOTAL] = "El total de páginas debe ser mayor que cero"
    }

    val leidas = leidasTexto.trim().toIntOrNull()
    when {
        leidasTexto.isBlank() -> errores[CAMPO_LEIDAS] = "Las páginas leídas son obligatorias (puede ser 0)"
        leidas == null -> errores[CAMPO_LEIDAS] = "Las páginas leídas deben ser un número entero"
        leidas < 0 -> errores[CAMPO_LEIDAS] = "Las páginas leídas no pueden ser negativas"
        total != null && total > 0 && leidas > total ->
            errores[CAMPO_LEIDAS] = "Las páginas leídas no pueden ser más que el total ($total)"
        estado == TERMINADO && total != null && leidas != total ->
            errores[CAMPO_LEIDAS] = "Si el libro está terminado, las páginas leídas deben ser $total"
    }

    if (estado == TERMINADO) {
        if (calificacion !in 1..5) {
            errores[CAMPO_CALIFICACION] = "La calificación es obligatoria para un libro terminado (1 a 5 estrellas)"
        }
    } else if (calificacion != 0) {
        errores[CAMPO_CALIFICACION] = "Solo se puede calificar un libro terminado"
    }

    return errores
}

fun validarMeta(texto: String): String? {
    val meta = texto.trim().toIntOrNull()
    return when {
        texto.isBlank() -> "Escribe cuántos libros quieres leer"
        meta == null -> "La meta debe ser un número entero"
        meta <= 0 -> "La meta debe ser mayor que cero"
        else -> null
    }
}
