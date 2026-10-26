package com.example.desafio3.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.desafio3.datos.Libro

@Composable
fun PantallaBiblioteca(
    libros: List<Libro>,
    modifier: Modifier = Modifier,
    onEditarLibro: (Libro) -> Unit,
    onEliminarLibro: (Libro) -> Unit,
    onAgregarLibro: () -> Unit
) {
    var libroAEliminar by remember { mutableStateOf<Libro?>(null) }

    if (libros.isEmpty()) {
        Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("📖", style = MaterialTheme.typography.displaySmall)
                Text(
                    "Tu biblioteca está vacía.\nAgrega tu primer libro para empezar.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(onClick = onAgregarLibro) { Text("Agregar libro") }
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "${libros.size} libro(s) · toca uno para editarlo",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(libros, key = { it.id }) { libro ->
                TarjetaLibro(
                    libro = libro,
                    onClick = { onEditarLibro(libro) },
                    onEliminar = { libroAEliminar = libro }
                )
            }
        }
    }

    libroAEliminar?.let { libro ->
        AlertDialog(
            onDismissRequest = { libroAEliminar = null },
            title = { Text("Confirmación") },
            text = { Text("¿Está seguro de eliminar \"${libro.titulo}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    onEliminarLibro(libro)
                    libroAEliminar = null
                }) { Text("Sí, eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { libroAEliminar = null }) { Text("No") }
            }
        )
    }
}
