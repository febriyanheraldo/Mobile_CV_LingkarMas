package com.lingkarmas.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingkarmas.app.ui.GoldButton
import com.lingkarmas.app.ui.LogoRing
import com.lingkarmas.app.ui.theme.*
import com.lingkarmas.app.data.Repo
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLogin: () -> Unit, onRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)
    val colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Navy, unfocusedBorderColor = Line)

    Column(Modifier.fillMaxSize().background(Navy)) {
        Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 72.dp)) {
            LogoRing()
            Spacer(Modifier.height(20.dp))
            Text("Selamat datang di\nCV. Lingkar Mas", color = Color.White,
                fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(10.dp))
            Text("Pesan produk lebih mudah, pantau status pesanan kapan saja.",
                color = Color(0xFFB8C6D6), fontSize = 14.sp)
        }
        Spacer(Modifier.height(36.dp))
        Column(
            Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true,
                shape = shape, colors = colors, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(pass, { pass = it }, label = { Text("Password") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = shape, colors = colors, modifier = Modifier.fillMaxWidth())
            error?.let { Text(it, color = Color(0xFF9B1C14), fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            GoldButton(
                if (loading) "Memproses..." else "Masuk",
                Modifier.padding(top = 6.dp),
                enabled = !loading
            ) {
                if (email.isBlank() || pass.isBlank()) {
                    error = "Email dan password wajib diisi"
                    return@GoldButton
                }
                loading = true
                scope.launch {
                    val err = Repo.login(email, pass)
                    loading = false
                    if (err == null) { error = null; onLogin() } else error = err
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("Belum punya akun? ", color = Muted, fontSize = 14.sp)
                Text("Daftar sekarang", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, modifier = Modifier.clickable { onRegister() })
            }
        }
    }
}