package com.example.desafio3

import com.example.desafio3.datos.CAMPO_AUTOR
import com.example.desafio3.datos.CAMPO_CALIFICACION
import com.example.desafio3.datos.CAMPO_LEIDAS
import com.example.desafio3.datos.CAMPO_TITULO
import com.example.desafio3.datos.CAMPO_TOTAL
import com.example.desafio3.datos.LEYENDO
import com.example.desafio3.datos.Libro
import com.example.desafio3.datos.POR_LEER
import com.example.desafio3.datos.TERMINADO
import com.example.desafio3.datos.calcularResumen
import com.example.desafio3.datos.validarLibro
import com.example.desafio3.datos.validarMeta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class LibrosTest {

    private fun fecha(anio: Int, mes: Int, dia: Int): Long {
        val calendario = Calendar.getInstance()
        calendario.set(anio, mes - 1, dia, 12, 0, 0)
        return calendario.timeInMillis
    }

    @Test
    fun libroValidoNoTieneErrores() {
        val errores = validarLibro("Dune", "Frank Herbert", "600", "120", LEYENDO, 0)
        assertTrue(errores.isEmpty())
    }

    @Test
    fun tituloYAutorDebenTenerEntre2y60Caracteres() {
        val errores = validarLibro("A", "x".repeat(61), "100", "0", POR_LEER, 0)
        assertNotNull(errores[CAMPO_TITULO])
        assertNotNull(errores[CAMPO_AUTOR])
    }

    @Test
    fun paginasInvalidas() {
        assertNotNull(validarLibro("Dune", "Herbert", "0", "0", POR_LEER, 0)[CAMPO_TOTAL])
        assertNotNull(validarLibro("Dune", "Herbert", "abc", "0", POR_LEER, 0)[CAMPO_TOTAL])
        assertNotNull(validarLibro("Dune", "Herbert", "100", "150", LEYENDO, 0)[CAMPO_LEIDAS])
        assertNotNull(validarLibro("Dune", "Herbert", "100", "-1", LEYENDO, 0)[CAMPO_LEIDAS])
    }

    @Test
    fun calificacionSoloParaTerminados() {
        assertNotNull(validarLibro("Dune", "Herbert", "100", "50", LEYENDO, 4)[CAMPO_CALIFICACION])
        assertNotNull(validarLibro("Dune", "Herbert", "100", "100", TERMINADO, 0)[CAMPO_CALIFICACION])
        assertTrue(validarLibro("Dune", "Herbert", "100", "100", TERMINADO, 5).isEmpty())
    }

    @Test
    fun metaDebeSerEnteroMayorQueCero() {
        assertNotNull(validarMeta("0"))
        assertNotNull(validarMeta("-3"))
        assertNotNull(validarMeta("2.5"))
        assertNull(validarMeta("12"))
    }

    @Test
    fun resumenDelAnio() {
        val libros = listOf(
            Libro(id = "1", titulo = "Dune", genero = "Ciencia ficción", totalPaginas = 600, paginasLeidas = 600,
                estado = TERMINADO, calificacion = 5, fechaTerminado = fecha(2026, 5, 10)),
            Libro(id = "2", titulo = "Fundación", genero = "Ciencia ficción", totalPaginas = 250, paginasLeidas = 250,
                estado = TERMINADO, calificacion = 5, fechaTerminado = fecha(2026, 2, 1)),
            Libro(id = "3", titulo = "Sapiens", genero = "Historia", totalPaginas = 400, paginasLeidas = 400,
                estado = TERMINADO, calificacion = 4, fechaTerminado = fecha(2026, 3, 3)),
            Libro(id = "4", titulo = "Viejo", genero = "Novela", totalPaginas = 100, paginasLeidas = 100,
                estado = TERMINADO, calificacion = 5, fechaTerminado = fecha(2025, 1, 1)),
            Libro(id = "5", titulo = "Hábitos", genero = "Desarrollo personal", totalPaginas = 300, paginasLeidas = 80,
                estado = LEYENDO)
        )

        val resumen = calcularResumen(libros, 2026)

        assertEquals(2, resumen.terminadosPorGenero["Ciencia ficción"])
        assertEquals(1, resumen.terminadosPorGenero["Historia"])
        assertEquals(0, resumen.terminadosPorGenero["Novela"])
        assertEquals(3, resumen.totalTerminados)
        assertEquals(listOf("Ciencia ficción"), resumen.generosMasLeidos)
        assertEquals(1250, resumen.paginasLeidas)
        // Empate en 5 estrellas: gana el que se terminó primero
        assertEquals("Fundación", resumen.mejorCalificado?.titulo)
    }

    @Test
    fun porcentajeConMuchasPaginas() {
        val libro = Libro(totalPaginas = 30_000_000, paginasLeidas = 15_000_000)
        assertEquals(50, libro.porcentajeAvance())
        assertEquals(0, Libro(totalPaginas = 0, paginasLeidas = 0).porcentajeAvance())
    }

    @Test
    fun resumenVacio() {
        val resumen = calcularResumen(emptyList(), 2026)
        assertEquals(0, resumen.totalTerminados)
        assertTrue(resumen.generosMasLeidos.isEmpty())
        assertNull(resumen.mejorCalificado)
    }
}
