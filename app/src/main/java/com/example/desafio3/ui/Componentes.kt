package com.example.desafio3.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.desafio3.datos.LEYENDO
import com.example.desafio3.datos.Libro
import com.example.desafio3.datos.TERMINADO
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val colorEstrella = Color(0xFFF2B01E)

fun formatearFecha(fecha: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fecha))

@Composable
fun Estrellas(calificacion: Int, modifier: Modifier = Modifier) {
    Text(
        text = "★".repeat(calificacion) + "☆".repeat(5 - calificacion),
        color = colorEstrella,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier
    )
}

@Composable
fun SelectorEstrellas(calificacion: Int, habilitado: Boolean, onCambio: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        for (numero in 1..5) {
            TextButton(onClick = { onCambio(numero) }, enabled = habilitado) {
                Text(
                    text = if (numero <= calificacion) "★" else "☆",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (habilitado) colorEstrella else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun BarraAvance(libro: Libro) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        LinearProgressIndicator(
            progress = { libro.porcentajeAvance() / 100f },
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(12.dp))
        Text("${libro.porcentajeAvance()}%", style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun TarjetaLibro(
    libro: Libro,
    onClick: () -> Unit,
    onEliminar: (() -> Unit)? = null
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(libro.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("de ${libro.autor}", style = MaterialTheme.typography.bodyMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = onClick, label = { Text(libro.genero) })
                AssistChip(onClick = onClick, label = { Text(libro.estado) })
            }

            BarraAvance(libro)
            Text(
                "${libro.paginasLeidas} de ${libro.totalPaginas} páginas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (libro.estado == TERMINADO) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Estrellas(libro.calificacion)
                    if (libro.fechaTerminado > 0) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Terminado el ${formatearFecha(libro.fechaTerminado)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (onEliminar != null) {
                TextButton(onClick = onEliminar, modifier = Modifier.align(Alignment.End)) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

fun librosLeyendo(libros: List<Libro>): List<Libro> =
    libros.filter { it.estado == LEYENDO }.sortedByDescending { it.porcentajeAvance() }
