package com.lingkarmas.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingkarmas.app.data.Repo
import com.lingkarmas.app.ui.*
import com.lingkarmas.app.ui.theme.*

@Composable
fun HomeScreen(onOrder: (Long) -> Unit, onSeeAll: () -> Unit) {
    var cat by remember { mutableStateOf("Semua") }
    val orders = Repo.orders
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Selamat datang,", color = Muted, fontSize = 13.sp)
                Text(Repo.user?.nama ?: "", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            }
            LogoRing()
        }
        Spacer(Modifier.height(20.dp))
        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Navy)) {
            Column(Modifier.padding(20.dp)) {
                Text("Pesanan aktif", color = Color(0xFFB8C6D6), fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    listOf("Diproses", "Dikirim", "Menunggu").forEach { s ->
                        Column {
                            Text("${orders.count { it.status == s }}", color = Gold,
                                fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                            Text(s, color = Color(0xFFB8C6D6), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        Text("Kategori", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp))
        ChipRow(Repo.kategori, cat) { cat = it }
        Row(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Produk terbaru", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            TextButton(onClick = onSeeAll) { Text("Lihat semua", fontWeight = FontWeight.Bold) }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Repo.products.filter { cat == "Semua" || it.kategori == cat }.take(2).forEach { p ->
                ProductCard(p) { onOrder(p.id) }
            }
        }
    }
}