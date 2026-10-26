package com.example.desafio3.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.desafio3.datos.Libro
import com.example.desafio3.datos.validarMeta

@Composable
fun PantallaInicio(
    correo: String,
    anio: Int,
    meta: Int,
    terminadosAnio: Int,
    leyendo: List<Libro>,
    modifier: Modifier = Modifier,
    onGuardarMeta: (Int) -> Unit,
    onAgregarLibro: () -> Unit,
    onVerBiblioteca: () -> Unit,
    onVerEstadisticas: () -> Unit,
    onEditarLibro: (Libro) -> Unit
) {
    var mostrarDialogoMeta by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("¡Hola, $correo!", style = MaterialTheme.typography.titleLarge)
        }

        item {
            TarjetaMeta(
                anio = anio,
                meta = meta,
                terminados = terminadosAnio,
                onModificar = { mostrarDialogoMeta = true }
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAgregarLibro, modifier = Modifier.fillMaxWidth()) {
                    Text("Agregar libro")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onVerBiblioteca, modifier = Modifier.weight(1f)) {
                        Text("Biblioteca")
                    }
                    OutlinedButton(onClick = onVerEstadisticas, modifier = Modifier.weight(1f)) {
                        Text("Estadísticas")
                    }
                }
            }
        }

        item {
            Text(
                "Leyendo actualmente",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (leyendo.isEmpty()) {
            item {
                Text(
                    "No tienes libros en lectura. Agrega uno con estado \"Leyendo\".",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(leyendo, key = { it.id }) { libro ->
                TarjetaLibro(libro = libro, onClick = { onEditarLibro(libro) })
            }
        }
    }

    if (mostrarDialogoMeta) {
        DialogoMeta(
            metaActual = meta,
            onConfirmar = { nuevaMeta ->
                onGuardarMeta(nuevaMeta)
                mostrarDialogoMeta = false
            },
            onCancelar = { mostrarDialogoMeta = false }
        )
    }
}

@Composable
private fun TarjetaMeta(anio: Int, meta: Int, terminados: Int, onModificar: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Meta de lectura $anio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            if (meta <= 0) {
                Text("Todavía no has definido cuántos libros quieres leer este año.")
                Text("Libros terminados en $anio: $terminados")
            } else {
                val faltan = (meta - terminados).coerceAtLeast(0)
                Text("Meta: $meta libros")
                Text("Terminados: $terminados")
                Text(if (faltan == 0) "¡Meta cumplida! 🎉" else "Te faltan $faltan para cumplirla")
                LinearProgressIndicator(
                    progress = { (terminados.toFloat() / meta).coerceAtMost(1f) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            TextButton(onClick = onModificar) {
                Text(if (meta <= 0) "Definir meta" else "Modificar meta")
            }
        }
    }
}

@Composable
private fun DialogoMeta(metaActual: Int, onConfirmar: (Int) -> Unit, onCancelar: () -> Unit) {
    var texto by rememberSaveable { mutableStateOf(if (metaActual > 0) metaActual.toString() else "") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Meta anual") },
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it; error = null },
                label = { Text("Libros a leer este año") },
                singleLine = true,
                isError = error != null,
                supportingText = error?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            TextButton(onClick = {
                error = validarMeta(texto)
                if (error == null) onConfirmar(texto.trim().toInt())
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
