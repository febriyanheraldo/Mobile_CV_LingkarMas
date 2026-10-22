package com.lingkarmas.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingkarmas.app.ui.GoldButton
import com.lingkarmas.app.ui.theme.*
import com.lingkarmas.app.data.Repo

@Composable
fun RegisterScreen(onBack: () -> Unit, onDone: () -> Unit) {
    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var hp by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var konfirmasi by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showOk by remember { mutableStateOf(false) }

    fun validasi(): String? = when {
        nama.isBlank() || email.isBlank() || hp.isBlank() || pass.isBlank() -> "Semua kolom wajib diisi"
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Format email tidak valid"
        pass.length < 6 -> "Password minimal 6 karakter"
        pass != konfirmasi -> "Konfirmasi password tidak sama"
        else -> null
    }

    Column(Modifier.fillMaxSize().background(Navy)) {
        Column(Modifier.padding(start = 12.dp, end = 24.dp, top = 40.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Color.White)
            }
            Text("Buat Akun Baru", color = Color.White, fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 12.dp))
            Text("Daftar untuk mulai memesan produk CV. Lingkar Mas.",
                color = Color(0xFFB8C6D6), fontSize = 14.sp,
                modifier = Modifier.padding(start = 12.dp, top = 8.dp))
        }
        Spacer(Modifier.height(24.dp))
        Column(
            Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Field(nama, { nama = it }, "Nama lengkap")
            Field(email, { email = it }, "Email", KeyboardType.Email)
            Field(hp, { hp = it }, "No. HP", KeyboardType.Phone)
            Field(pass, { pass = it }, "Password", KeyboardType.Password, password = true)
            Field(konfirmasi, { konfirmasi = it }, "Konfirmasi password", KeyboardType.Password, password = true)
            error?.let {
                Text(it, color = Color(0xFF9B1C14), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            GoldButton("Daftar", Modifier.padding(top = 6.dp)) {
                error = validasi() ?: Repo.register(nama, email, hp, pass)
                if (error == null) showOk = true
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("Sudah punya akun? ", color = Muted, fontSize = 14.sp)
                Text("Masuk", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp,
                    modifier = Modifier.clickable { onBack() })
            }
        }
    }

    if (showOk) AlertDialog(
        onDismissRequest = {}, shape = RoundedCornerShape(24.dp),
        title = { Text("Akun Berhasil Dibuat", fontWeight = FontWeight.ExtraBold) },
        text = { Text("Silakan masuk dengan email dan password yang baru kamu daftarkan.") },
        confirmButton = { TextButton(onClick = { showOk = false; onDone() }) { Text("Masuk") } }
    )
}

@Composable
private fun Field(
    value: String, onChange: (String) -> Unit, label: String,
    keyboard: KeyboardType = KeyboardType.Text, password: Boolean = false
) {
    OutlinedTextField(
        value, onChange, label = { Text(label) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Navy, unfocusedBorderColor = Line),
        modifier = Modifier.fillMaxWidth()
    )
}