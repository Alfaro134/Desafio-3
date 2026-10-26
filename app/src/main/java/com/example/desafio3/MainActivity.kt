package com.example.desafio3

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.desafio3.datos.BaseDatos
import com.example.desafio3.datos.Libro
import com.example.desafio3.datos.anioActual
import com.example.desafio3.datos.calcularResumen
import com.example.desafio3.ui.FlujoAutenticacion
import com.example.desafio3.ui.PantallaBiblioteca
import com.example.desafio3.ui.PantallaEstadisticas
import com.example.desafio3.ui.PantallaFormularioLibro
import com.example.desafio3.ui.PantallaInicio
import com.example.desafio3.ui.librosLeyendo
import com.example.desafio3.ui.theme.Desafio3Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

enum class Pantalla { INICIO, BIBLIOTECA, ESTADISTICAS, FORMULARIO }

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private var usuario by mutableStateOf<FirebaseUser?>(null)

    // Cada vez que cambia la sesión (login, registro o cierre) se actualiza la pantalla
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        usuario = firebaseAuth.currentUser
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        usuario = auth.currentUser

        setContent {
            Desafio3Theme {
                val usuarioActual = usuario
                if (usuarioActual == null) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Box(Modifier.padding(innerPadding)) {
                            FlujoAutenticacion(auth)
                        }
                    }
                } else {
                    AppLector(usuario = usuarioActual, onCerrarSesion = ::cerrarSesion)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        auth.addAuthStateListener(authStateListener)
    }

    override fun onPause() {
        super.onPause()
        auth.removeAuthStateListener(authStateListener)
    }

    private fun cerrarSesion() {
        auth.signOut()
        usuario = null
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLector(usuario: FirebaseUser, onCerrarSesion: () -> Unit) {
    val contexto = LocalContext.current
    val uid = usuario.uid
    val anio = remember { anioActual() }

    var libros by remember(uid) { mutableStateOf<List<Libro>>(emptyList()) }
    var meta by remember(uid) { mutableIntStateOf(0) }

    var pantalla by rememberSaveable { mutableStateOf(Pantalla.INICIO) }
    var pantallaAnterior by rememberSaveable { mutableStateOf(Pantalla.INICIO) }
    var idLibroEditando by rememberSaveable { mutableStateOf<String?>(null) }

    // Escucha los cambios en Firebase igual que en el CRUD de la guía, pero solo del usuario actual
    DisposableEffect(uid) {
        val refLibros = BaseDatos.refLibros(uid)
        val refMeta = BaseDatos.refMeta(uid, anio)

        val escuchaLibros = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                libros = dataSnapshot.children.mapNotNull { dato ->
                    dato.getValue(Libro::class.java)?.apply { id = dato.key ?: "" }
                }.sortedBy { it.titulo.lowercase() }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(contexto, "Error al leer libros: ${databaseError.message}", Toast.LENGTH_LONG).show()
            }
        }

        val escuchaMeta = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                meta = (dataSnapshot.value as? Long)?.toInt() ?: 0
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(contexto, "Error al leer la meta: ${databaseError.message}", Toast.LENGTH_LONG).show()
            }
        }

        refLibros.addValueEventListener(escuchaLibros)
        refMeta.addValueEventListener(escuchaMeta)

        onDispose {
            refLibros.removeEventListener(escuchaLibros)
            refMeta.removeEventListener(escuchaMeta)
        }
    }

    val resumen = remember(libros, anio) { calcularResumen(libros, anio) }

    fun irA(destino: Pantalla) {
        pantallaAnterior = pantalla
        pantalla = destino
    }

    fun abrirFormulario(libro: Libro?) {
        idLibroEditando = libro?.id
        irA(Pantalla.FORMULARIO)
    }

    fun regresar() {
        pantalla = if (pantalla == Pantalla.FORMULARIO) pantallaAnterior else Pantalla.INICIO
        pantallaAnterior = Pantalla.INICIO
        idLibroEditando = null
    }

    fun guardarLibro(libro: Libro) {
        val refLibros = BaseDatos.refLibros(uid)
        if (libro.id.isEmpty()) idLibroEditando?.let { libro.id = it }
        val esNuevo = libro.id.isEmpty()
        if (esNuevo) {
            val nuevaClave = refLibros.push().key
            if (nuevaClave == null) {
                Toast.makeText(contexto, "No se pudo generar una clave", Toast.LENGTH_SHORT).show()
                return
            }
            libro.id = nuevaClave
        }
        refLibros.child(libro.id).setValue(libro)
            .addOnFailureListener {
                Toast.makeText(contexto, "Error al guardar: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        Toast.makeText(contexto, if (esNuevo) "Libro agregado" else "Libro actualizado", Toast.LENGTH_SHORT).show()
        regresar()
    }

    fun eliminarLibro(libro: Libro) {
        BaseDatos.refLibros(uid).child(libro.id).removeValue()
            .addOnFailureListener {
                Toast.makeText(contexto, "Error al eliminar: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        Toast.makeText(contexto, "Libro eliminado", Toast.LENGTH_SHORT).show()
    }

    fun guardarMeta(nuevaMeta: Int) {
        BaseDatos.refMeta(uid, anio).setValue(nuevaMeta)
            .addOnFailureListener {
                Toast.makeText(contexto, "Error al guardar la meta: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        Toast.makeText(contexto, "Meta actualizada", Toast.LENGTH_SHORT).show()
    }

    BackHandler(enabled = pantalla != Pantalla.INICIO) { regresar() }

    val titulo = when (pantalla) {
        Pantalla.INICIO -> "Mis Lecturas"
        Pantalla.BIBLIOTECA -> "Biblioteca"
        Pantalla.ESTADISTICAS -> "Estadísticas"
        Pantalla.FORMULARIO -> if (idLibroEditando == null) "Nuevo libro" else "Editar libro"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    if (pantalla != Pantalla.INICIO) {
                        TextButton(onClick = ::regresar) { Text("‹ Volver") }
                    }
                },
                actions = {
                    if (pantalla == Pantalla.INICIO) {
                        TextButton(onClick = onCerrarSesion) { Text("Cerrar sesión") }
                    }
                }
            )
        }
    ) { innerPadding ->
        val modificador = Modifier.padding(innerPadding)
        when (pantalla) {
            Pantalla.INICIO -> PantallaInicio(
                correo = usuario.displayName?.takeIf { it.isNotBlank() } ?: usuario.email.orEmpty(),
                anio = anio,
                meta = meta,
                terminadosAnio = resumen.totalTerminados,
                leyendo = librosLeyendo(libros),
                modifier = modificador,
                onGuardarMeta = ::guardarMeta,
                onAgregarLibro = { abrirFormulario(null) },
                onVerBiblioteca = { irA(Pantalla.BIBLIOTECA) },
                onVerEstadisticas = { irA(Pantalla.ESTADISTICAS) },
                onEditarLibro = { abrirFormulario(it) }
            )

            Pantalla.BIBLIOTECA -> PantallaBiblioteca(
                libros = libros,
                modifier = modificador,
                onEditarLibro = { abrirFormulario(it) },
                onEliminarLibro = ::eliminarLibro,
                onAgregarLibro = { abrirFormulario(null) }
            )

            Pantalla.ESTADISTICAS -> PantallaEstadisticas(anio = anio, resumen = resumen, modifier = modificador)

            Pantalla.FORMULARIO -> {
                val libroOriginal = libros.firstOrNull { it.id == idLibroEditando }
                PantallaFormularioLibro(
                    libroOriginal = libroOriginal,
                    modifier = modificador,
                    onGuardar = ::guardarLibro,
                    onCancelar = ::regresar
                )
            }
        }
    }
}
