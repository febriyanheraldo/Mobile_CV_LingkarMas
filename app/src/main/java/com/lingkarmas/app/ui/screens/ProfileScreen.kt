package com.lingkarmas.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingkarmas.app.data.Repo
import com.lingkarmas.app.ui.GoldButton
import com.lingkarmas.app.ui.theme.*

@Composable
fun ProfileScreen(onManageProducts: () -> Unit, onLogout: () -> Unit) {
    val u = Repo.user
    var showEdit by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(24.dp))
        Box(Modifier.size(88.dp).clip(CircleShape).background(Navy), contentAlignment = Alignment.Center) {
            Text(u?.nama?.firstOrNull()?.uppercase() ?: "K", color = Gold,
                fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
        }
        Text(u?.nama ?: "", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 16.dp))
        Text(u?.email ?: "", color = Muted, fontSize = 14.sp)
        Spacer(Modifier.height(24.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("No. HP", color = Muted, fontSize = 12.sp)
                Text(u?.hp ?: "", fontWeight = FontWeight.SemiBold)
                HorizontalDivider(color = Line)
                Text("Peran", color = Muted, fontSize = 12.sp)
                Text("Sales / Klien", fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = { showEdit = true }, shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Ubah Profil", fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onManageProducts, shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Kelola Produk (demo)", fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(24.dp))
        GoldButton("Keluar") { Repo.logout(); onLogout() }
        TextButton(onClick = { showDelete = true }) {
            Text("Hapus Akun", color = Color(0xFF9B1C14), fontWeight = FontWeight.Bold)
        }
    }

    if (showEdit && u != null) {
        var nama by remember { mutableStateOf(u.nama) }
        var hp by remember { mutableStateOf(u.hp) }
        AlertDialog(
            onDismissRequest = { showEdit = false },
            title = { Text("Ubah Profil", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(nama, { nama = it }, label = { Text("Nama lengkap") }, singleLine = true)
                    OutlinedTextField(hp, { hp = it }, label = { Text("No. HP") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nama.isNotBlank() && hp.isNotBlank()) { Repo.updateProfile(nama, hp); showEdit = false }
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { showEdit = false }) { Text("Batal") } }
        )
    }
    if (showDelete) AlertDialog(
        onDismissRequest = { showDelete = false },
        title = { Text("Hapus akun?", fontWeight = FontWeight.ExtraBold) },
        text = { Text("Akun beserta seluruh riwayat pesananmu akan dihapus permanen.") },
        confirmButton = {
            TextButton(onClick = { Repo.deleteAccount(); showDelete = false; onLogout() }) {
                Text("Ya, hapus", color = Color(0xFF9B1C14))
            }
        },
        dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Batal") } }
    )
}