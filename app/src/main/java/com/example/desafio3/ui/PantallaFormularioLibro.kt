package com.example.desafio3.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.desafio3.datos.CAMPO_AUTOR
import com.example.desafio3.datos.CAMPO_CALIFICACION
import com.example.desafio3.datos.CAMPO_LEIDAS
import com.example.desafio3.datos.CAMPO_TITULO
import com.example.desafio3.datos.CAMPO_TOTAL
import com.example.desafio3.datos.ESTADOS
import com.example.desafio3.datos.GENEROS
import com.example.desafio3.datos.Libro
import com.example.desafio3.datos.POR_LEER
import com.example.desafio3.datos.TERMINADO
import com.example.desafio3.datos.validarLibro

@Composable
fun PantallaFormularioLibro(
    libroOriginal: Libro?,
    modifier: Modifier = Modifier,
    onGuardar: (Libro) -> Unit,
    onCancelar: () -> Unit
) {
    val contexto = LocalContext.current
    val focusManager = LocalFocusManager.current

    var titulo by rememberSaveable { mutableStateOf(libroOriginal?.titulo ?: "") }
    var autor by rememberSaveable { mutableStateOf(libroOriginal?.autor ?: "") }
    var genero by rememberSaveable { mutableStateOf(libroOriginal?.genero ?: GENEROS[0]) }
    var totalTexto by rememberSaveable { mutableStateOf(libroOriginal?.totalPaginas?.toString() ?: "") }
    var leidasTexto by rememberSaveable { mutableStateOf(libroOriginal?.paginasLeidas?.toString() ?: "0") }
    var estado by rememberSaveable { mutableStateOf(libroOriginal?.estado ?: POR_LEER) }
    var calificacion by rememberSaveable { mutableIntStateOf(libroOriginal?.calificacion ?: 0) }
    var errores by rememberSaveable { mutableStateOf(mapOf<String, String>()) }

    fun cambiarEstado(nuevoEstado: String) {
        estado = nuevoEstado
        if (nuevoEstado == TERMINADO) {
            // Si ya está terminado, se asume que se leyó completo
            totalTexto.trim().toIntOrNull()?.takeIf { it > 0 }?.let { leidasTexto = it.toString() }
        } else {
            calificacion = 0
        }
        errores = errores - CAMPO_CALIFICACION - CAMPO_LEIDAS
    }

    fun guardar() {
        focusManager.clearFocus()
        errores = validarLibro(titulo, autor, totalTexto, leidasTexto, estado, calificacion)
        if (errores.isNotEmpty()) {
            Toast.makeText(contexto, "Revisa los campos marcados en rojo", Toast.LENGTH_SHORT).show()
            return
        }

        val fecha = when {
            estado != TERMINADO -> 0L
            libroOriginal?.terminado() == true && libroOriginal.fechaTerminado > 0 -> libroOriginal.fechaTerminado
            else -> System.currentTimeMillis()
        }

        onGuardar(
            Libro(
                id = libroOriginal?.id ?: "",
                titulo = titulo.trim(),
                autor = autor.trim(),
                genero = genero,
                totalPaginas = totalTexto.trim().toInt(),
                paginasLeidas = leidasTexto.trim().toInt(),
                estado = estado,
                calificacion = if (estado == TERMINADO) calificacion else 0,
                fechaTerminado = fecha
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CampoTexto("Título", titulo, errores[CAMPO_TITULO]) { titulo = it; errores = errores - CAMPO_TITULO }
        CampoTexto("Autor", autor, errores[CAMPO_AUTOR]) { autor = it; errores = errores - CAMPO_AUTOR }

        Text("Género", style = MaterialTheme.typography.titleSmall)
        GrupoOpciones(opciones = GENEROS, seleccionada = genero, onSeleccion = { genero = it })

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CampoTexto(
                etiqueta = "Total de páginas",
                valor = totalTexto,
                error = errores[CAMPO_TOTAL],
                numerico = true,
                modifier = Modifier.weight(1f)
            ) {
                totalTexto = it
                errores = errores - CAMPO_TOTAL
            }
            CampoTexto(
                etiqueta = "Páginas leídas",
                valor = leidasTexto,
                error = errores[CAMPO_LEIDAS],
                numerico = true,
                modifier = Modifier.weight(1f)
            ) {
                leidasTexto = it
                errores = errores - CAMPO_LEIDAS
            }
        }

        Text("Estado", style = MaterialTheme.typography.titleSmall)
        GrupoOpciones(opciones = ESTADOS, seleccionada = estado, onSeleccion = ::cambiarEstado)

        Text("Calificación", style = MaterialTheme.typography.titleSmall)
        SelectorEstrellas(calificacion = calificacion, habilitado = estado == TERMINADO) {
            calificacion = it
            errores = errores - CAMPO_CALIFICACION
        }
        val mensajeCalificacion = errores[CAMPO_CALIFICACION]
        if (mensajeCalificacion != null) {
            Text(mensajeCalificacion, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        } else if (estado != TERMINADO) {
            Text(
                "Solo puedes calificar cuando el libro está terminado",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            OutlinedButton(onClick = onCancelar, modifier = Modifier.weight(1f)) { Text("Cancelar") }
            Button(onClick = ::guardar, modifier = Modifier.weight(1f)) { Text("Guardar") }
        }
    }
}

@Composable
private fun CampoTexto(
    etiqueta: String,
    valor: String,
    error: String?,
    modifier: Modifier = Modifier.fillMaxWidth(),
    numerico: Boolean = false,
    onCambio: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        keyboardOptions = if (numerico) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        modifier = modifier
    )
}

@Composable
private fun GrupoOpciones(opciones: List<String>, seleccionada: String, onSeleccion: (String) -> Unit) {
    Column {
        opciones.forEach { opcion ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = opcion == seleccionada,
                        onClick = { onSeleccion(opcion) },
                        role = Role.RadioButton
                    )
            ) {
                RadioButton(selected = opcion == seleccionada, onClick = null)
                Text(opcion, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
