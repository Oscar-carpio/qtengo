package com.example.qtengo.login.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onLoginExitoso: (uid: String, perfiles: List<String>) -> Unit,
    onIrARegistro: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qtengo_prefs", Context.MODE_PRIVATE) }

    var email by remember { mutableStateOf(prefs.getString("ultimo_email", "") ?: "") }
    var password by remember { mutableStateOf(prefs.getString("password_guardada", "") ?: "") }
    var recordarPassword by remember { mutableStateOf(prefs.getBoolean("recordar_password", false)) }

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val success = authState as AuthState.Success

            // Siempre guardamos el email
            prefs.edit().putString("ultimo_email", email).apply()

            // Guardamos o borramos la contraseña según el checkbox
            if (recordarPassword) {
                prefs.edit()
                    .putString("password_guardada", password)
                    .putBoolean("recordar_password", true)
                    .apply()
            } else {
                prefs.edit()
                    .remove("password_guardada")
                    .putBoolean("recordar_password", false)
                    .apply()
            }

            onLoginExitoso(success.uid, success.perfiles)
            authViewModel.reset()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Checkbox "Recordar contraseña"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = recordarPassword,
                onCheckedChange = { recordarPassword = it }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Recordar contraseña", style = MaterialTheme.typography.bodyMedium)
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
            onClick = { authViewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Entrar")
            }
        }

        /* if (authState is AuthState.RecuperacionEnviada) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "✅ Correo de recuperación enviado. Revisa tu bandeja.",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        } */

        Spacer(modifier = Modifier.height(4.dp))

        /*TextButton(onClick = { authViewModel.recuperarPassword(email) }) {
            Text("¿Olvidaste tu contraseña?")
        }*/

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onIrARegistro) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}