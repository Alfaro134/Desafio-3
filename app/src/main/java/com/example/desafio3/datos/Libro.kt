package com.example.desafio3.datos

val GENEROS = listOf("Novela", "Ciencia ficción", "Historia", "Desarrollo personal")

const val POR_LEER = "Por leer"
const val LEYENDO = "Leyendo"
const val TERMINADO = "Terminado"
val ESTADOS = listOf(POR_LEER, LEYENDO, TERMINADO)

// Firebase necesita valores por defecto para poder crear el objeto al leerlo
data class Libro(
    var id: String = "",
    var titulo: String = "",
    var autor: String = "",
    var genero: String = GENEROS[0],
    var totalPaginas: Int = 0,
    var paginasLeidas: Int = 0,
    var estado: String = POR_LEER,
    var calificacion: Int = 0,
    var fechaTerminado: Long = 0
) {
    fun porcentajeAvance(): Int {
        if (totalPaginas <= 0) return 0
        return (paginasLeidas.toLong() * 100 / totalPaginas).toInt().coerceIn(0, 100)
    }

    fun terminado(): Boolean = estado == TERMINADO
}
