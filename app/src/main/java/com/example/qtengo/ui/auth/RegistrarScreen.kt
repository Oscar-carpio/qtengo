package com.example.qtengo.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.data.local.model.User

@Composable
fun RegisterScreen(
    onRegistroExitoso: (User) -> Unit,
    onIrALogin: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var perfilSeleccionado by remember { mutableStateOf("") }

    var errorNombre by remember { mutableStateOf("") }
    var errorApellidos by remember { mutableStateOf("") }
    var errorEmail by remember { mutableStateOf("") }
    var errorPassword by remember { mutableStateOf("") }
    var errorPerfil by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()
    val perfiles = listOf("Familiar", "Restauración", "Pyme")

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onRegistroExitoso((authState as AuthState.Success).user)
            authViewModel.reset()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it.filter { c -> c.isLetter() || c.isWhitespace() }
                errorNombre = ""
            },
            label = { Text("Nombre") },
            isError = errorNombre.isNotEmpty(),
            supportingText = { if (errorNombre.isNotEmpty()) Text(errorNombre, color = MaterialTheme.colorScheme.error) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = apellidos,
            onValueChange = {
                apellidos = it.filter { c -> c.isLetter() || c.isWhitespace() }
                errorApellidos = ""
            },
            label = { Text("Apellidos") },
            isError = errorApellidos.isNotEmpty(),
            supportingText = { if (errorApellidos.isNotEmpty()) Text(errorApellidos, color = MaterialTheme.colorScheme.error) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorEmail = ""
            },
            label = { Text("Email") },
            isError = errorEmail.isNotEmpty(),
            supportingText = { if (errorEmail.isNotEmpty()) Text(errorEmail, color = MaterialTheme.colorScheme.error) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorPassword = ""
            },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            isError = errorPassword.isNotEmpty(),
            supportingText = { if (errorPassword.isNotEmpty()) Text(errorPassword, color = MaterialTheme.colorScheme.error) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Mínimo 8 caracteres, una mayúscula y un número",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Selecciona tu perfil", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(8.dp))

        perfiles.forEach { perfil ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = perfilSeleccionado == perfil,
                    onClick = {
                        perfilSeleccionado = perfil
                        errorPerfil = ""
                    }
                )
                Text(perfil)
            }
        }

        if (errorPerfil.isNotEmpty()) {
            Text(errorPerfil, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).mensaje,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                var valido = true

                if (nombre.trim().isEmpty()) {
                    errorNombre = "El nombre no puede estar vacío"
                    valido = false
                }
                if (apellidos.trim().isEmpty()) {
                    errorApellidos = "Los apellidos no pueden estar vacíos"
                    valido = false
                }
                if (email.trim().isEmpty()) {
                    errorEmail = "El email no puede estar vacío"
                    valido = false
                }
                if (password.trim().isEmpty()) {
                    errorPassword = "La contraseña no puede estar vacía"
                    valido = false
                }
                if (perfilSeleccionado.isEmpty()) {
                    errorPerfil = "Debes seleccionar un perfil"
                    valido = false
                }

                if (valido) {
                    authViewModel.registrar(
                        nombre.trim(),
                        apellidos.trim(),
                        "",
                        email.trim(),
                        password.trim(),
                        perfilSeleccionado
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarse")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onIrALogin) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}