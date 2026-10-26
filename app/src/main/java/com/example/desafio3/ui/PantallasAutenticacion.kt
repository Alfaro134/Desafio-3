package com.example.desafio3.ui

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

private fun validarCorreo(correo: String): String? = when {
    correo.isBlank() -> "El correo es obligatorio"
    !Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches() -> "El formato del correo no es válido"
    else -> null
}

private fun validarContrasena(contrasena: String): String? = when {
    contrasena.isEmpty() -> "La contraseña es obligatoria"
    contrasena.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
    else -> null
}

// Firebase devuelve los errores en inglés, aquí se traducen los más comunes
private fun mensajeError(excepcion: Exception, esRegistro: Boolean): String = when (excepcion) {
    is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil, usa al menos 6 caracteres"
    is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo"
    is FirebaseAuthInvalidUserException -> "No existe una cuenta con ese correo o fue deshabilitada"
    is FirebaseAuthInvalidCredentialsException ->
        if (esRegistro) "El correo no es válido" else "Correo o contraseña incorrectos"
    is FirebaseNetworkException -> "No hay conexión a internet, intenta de nuevo"
    is FirebaseTooManyRequestsException -> "Demasiados intentos, espera un momento"
    else -> "No se pudo completar: ${excepcion.localizedMessage}"
}

@Composable
fun FlujoAutenticacion(auth: FirebaseAuth) {
    var mostrarRegistro by rememberSaveable { mutableStateOf(false) }

    if (mostrarRegistro) {
        PantallaRegistro(auth = auth, onIrALogin = { mostrarRegistro = false })
    } else {
        PantallaLogin(auth = auth, onIrARegistro = { mostrarRegistro = true })
    }
}

@Composable
fun PantallaLogin(auth: FirebaseAuth, onIrARegistro: () -> Unit) {
    val contexto = LocalContext.current
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var errorCorreo by rememberSaveable { mutableStateOf<String?>(null) }
    var errorContrasena by rememberSaveable { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    fun iniciarSesion() {
        errorCorreo = validarCorreo(correo)
        errorContrasena = validarContrasena(contrasena)
        if (errorCorreo != null || errorContrasena != null) return

        focusManager.clearFocus()
        cargando = true
        auth.signInWithEmailAndPassword(correo.trim(), contrasena)
            .addOnCompleteListener { cargando = false }
            .addOnFailureListener { excepcion ->
                Toast.makeText(contexto, mensajeError(excepcion, esRegistro = false), Toast.LENGTH_LONG).show()
            }
    }

    FormularioAutenticacion(titulo = "Iniciar sesión", subtitulo = "Lleva el control de tus lecturas") {
        CampoCorreo(correo, errorCorreo) { correo = it; errorCorreo = null }
        CampoContrasena("Contraseña", contrasena, errorContrasena) { contrasena = it; errorContrasena = null }

        Spacer(Modifier.height(8.dp))
        BotonConCarga(texto = "Ingresar", cargando = cargando, onClick = ::iniciarSesion)
        TextButton(onClick = onIrARegistro) { Text("¿No tienes una cuenta? Regístrate") }
    }
}

@Composable
fun PantallaRegistro(auth: FirebaseAuth, onIrALogin: () -> Unit) {
    val contexto = LocalContext.current
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }
    var errorCorreo by rememberSaveable { mutableStateOf<String?>(null) }
    var errorContrasena by rememberSaveable { mutableStateOf<String?>(null) }
    var errorConfirmacion by rememberSaveable { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    fun registrar() {
        errorCorreo = validarCorreo(correo)
        errorContrasena = validarContrasena(contrasena)
        errorConfirmacion = when {
            confirmacion.isEmpty() -> "Confirma tu contraseña"
            confirmacion != contrasena -> "Las contraseñas no coinciden"
            else -> null
        }
        if (errorCorreo != null || errorContrasena != null || errorConfirmacion != null) return

        focusManager.clearFocus()
        cargando = true
        auth.createUserWithEmailAndPassword(correo.trim(), contrasena)
            .addOnCompleteListener { tarea ->
                cargando = false
                if (tarea.isSuccessful) {
                    Toast.makeText(contexto, "Cuenta creada", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { excepcion ->
                Toast.makeText(contexto, mensajeError(excepcion, esRegistro = true), Toast.LENGTH_LONG).show()
            }
    }

    FormularioAutenticacion(titulo = "Crear cuenta", subtitulo = "Regístrate con tu correo") {
        CampoCorreo(correo, errorCorreo) { correo = it; errorCorreo = null }
        CampoContrasena("Contraseña", contrasena, errorContrasena) { contrasena = it; errorContrasena = null }
        CampoContrasena("Confirmar contraseña", confirmacion, errorConfirmacion) {
            confirmacion = it
            errorConfirmacion = null
        }

        Spacer(Modifier.height(8.dp))
        BotonConCarga(texto = "Registrar", cargando = cargando, onClick = ::registrar)
        TextButton(onClick = onIrALogin) { Text("¿Ya tienes una cuenta? Ingresa") }
    }
}

@Composable
private fun FormularioAutenticacion(
    titulo: String,
    subtitulo: String,
    contenido: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("📚", style = MaterialTheme.typography.displayMedium)
        Text(titulo, style = MaterialTheme.typography.headlineMedium)
        Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        contenido()
    }
}

@Composable
private fun CampoCorreo(valor: String, error: String?, onCambio: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text("Correo electrónico") },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CampoContrasena(etiqueta: String, valor: String, error: String?, onCambio: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun BotonConCarga(texto: String, cargando: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = !cargando, modifier = Modifier.fillMaxWidth().height(50.dp)) {
        if (cargando) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            Text(texto)
        }
    }
}
