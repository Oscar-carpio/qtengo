package com.example.qtengo.core.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qtengo.core.ui.theme.BlueDark

data class ProfileOption(val title: String, val description: String, val icon: String)

/**
 * Pantalla de selección de perfil de usuario.
 * Permite al usuario elegir entre Familiar, Pyme o Restauración.
 */
@Composable
fun ProfileScreen(onProfileSelected: (String) -> Unit) {
    val profiles = listOf(
        ProfileOption("Familiar", "Gestión del hogar y familia", "🏠"),
        ProfileOption("Pyme", "Gestión para pequeñas empresas", "🏢"),
        ProfileOption("Restauración", "Gestión para hostelería", "🍽️")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "¿Cuál es tu perfil?",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = BlueDark
        )
        Text(
            text = "Selecciona cómo quieres usar Q-Tengo",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
        )

        profiles.forEach { profile ->
            ProfileCard(profile = profile, onClick = { onProfileSelected(profile.title) })
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileCard(profile: ProfileOption, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = profile.icon, fontSize = 36.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = profile.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueDark
                )
                Text(
                    text = profile.description,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
