package com.example.desafio3.datos

import java.util.Calendar

data class ResumenAnual(
    val terminadosPorGenero: Map<String, Int>,
    val generosMasLeidos: List<String>,
    val paginasLeidas: Int,
    val mejorCalificado: Libro?
) {
    val totalTerminados: Int get() = terminadosPorGenero.values.sum()
}

fun anioDe(fecha: Long): Int {
    val calendario = Calendar.getInstance()
    calendario.timeInMillis = fecha
    return calendario.get(Calendar.YEAR)
}

fun anioActual(): Int = Calendar.getInstance().get(Calendar.YEAR)

fun calcularResumen(libros: List<Libro>, anio: Int): ResumenAnual {
    val terminados = libros.filter { it.terminado() && it.fechaTerminado > 0 && anioDe(it.fechaTerminado) == anio }

    val porGenero = GENEROS.associateWith { genero -> terminados.count { it.genero == genero } }

    val maximo = porGenero.values.maxOrNull() ?: 0
    val masLeidos = if (maximo == 0) emptyList() else porGenero.filterValues { it == maximo }.keys.toList()

    // Empate en calificación: gana el que se terminó primero
    val mejor = terminados
        .sortedWith(compareByDescending<Libro> { it.calificacion }.thenBy { it.fechaTerminado })
        .firstOrNull()

    return ResumenAnual(
        terminadosPorGenero = porGenero,
        generosMasLeidos = masLeidos,
        paginasLeidas = terminados.sumOf { it.paginasLeidas },
        mejorCalificado = mejor
    )
}
