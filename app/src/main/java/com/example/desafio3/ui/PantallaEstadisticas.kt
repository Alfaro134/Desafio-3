package com.example.desafio3.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.desafio3.datos.ResumenAnual

@Composable
fun PantallaEstadisticas(anio: Int, resumen: ResumenAnual, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Resumen de $anio", style = MaterialTheme.typography.titleLarge)

        TarjetaSeccion("Libros terminados por género") {
            val maximo = resumen.terminadosPorGenero.values.maxOrNull()?.coerceAtLeast(1) ?: 1
            resumen.terminadosPorGenero.forEach { (genero, cantidad) ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(genero, modifier = Modifier.weight(1f))
                    Text("$cantidad", fontWeight = FontWeight.SemiBold)
                }
                LinearProgressIndicator(
                    progress = { cantidad.toFloat() / maximo },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                )
            }
            Text("Total: ${resumen.totalTerminados}", fontWeight = FontWeight.SemiBold)
        }

        TarjetaSeccion("Género más leído") {
            Text(
                if (resumen.generosMasLeidos.isEmpty()) "Aún no terminas libros este año"
                else unirConY(resumen.generosMasLeidos),
                style = MaterialTheme.typography.headlineSmall
            )
            if (resumen.generosMasLeidos.size > 1) {
                Text("Empate entre géneros", style = MaterialTheme.typography.bodySmall)
            }
        }

        TarjetaSeccion("Páginas leídas en el año") {
            Text("${resumen.paginasLeidas}", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Suma de las páginas de los libros terminados en $anio",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TarjetaSeccion("Libro mejor calificado") {
            val mejor = resumen.mejorCalificado
            if (mejor == null) {
                Text("Todavía no hay libros terminados en $anio")
            } else {
                Text(mejor.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("de ${mejor.autor} · ${mejor.genero}")
                Estrellas(mejor.calificacion)
                Text("Terminado el ${formatearFecha(mejor.fechaTerminado)}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// "Novela, Historia y Ciencia ficción"
private fun unirConY(palabras: List<String>): String =
    if (palabras.size <= 1) palabras.joinToString()
    else palabras.dropLast(1).joinToString(", ") + " y " + palabras.last()

@Composable
private fun TarjetaSeccion(titulo: String, contenido: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            contenido()
        }
    }
}
