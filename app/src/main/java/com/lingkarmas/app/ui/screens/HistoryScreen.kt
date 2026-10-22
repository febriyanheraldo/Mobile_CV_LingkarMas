package com.lingkarmas.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingkarmas.app.data.Order
import com.lingkarmas.app.data.Repo
import com.lingkarmas.app.data.rupiah
import com.lingkarmas.app.ui.ChipRow
import com.lingkarmas.app.ui.StatusChip
import com.lingkarmas.app.ui.statusColors
import com.lingkarmas.app.ui.theme.*

@Composable
fun HistoryScreen() {
    var filter by remember { mutableStateOf("Semua") }
    var edit by remember { mutableStateOf<Order?>(null) }
    var batal by remember { mutableStateOf<Order?>(null) }
    val list = Repo.orders.filter { filter == "Semua" || it.status == filter }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Text("Riwayat Pesanan", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp))
        ChipRow(listOf("Semua", "Menunggu", "Diproses", "Dikirim", "Ditolak"), filter) { filter = it }
        Spacer(Modifier.height(16.dp))
        if (list.isEmpty()) Text("Belum ada pesanan.", color = Muted, fontSize = 14.sp)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
        }
    }

    edit?.let { o ->
        var alamat by remember(o.id) { mutableStateOf(o.alamat) }
        AlertDialog(
            onDismissRequest = { edit = null },
            title = { Text("Ubah alamat pengiriman", fontWeight = FontWeight.ExtraBold) },
            text = { OutlinedTextField(alamat, { alamat = it }, minLines = 2, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                TextButton(onClick = {
                    if (alamat.isNotBlank()) { Repo.updateAlamat(o.id, alamat.trim()); edit = null }
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { edit = null }) { Text("Batal") } }
        )
    }
    batal?.let { o ->
        AlertDialog(
            onDismissRequest = { batal = null },
            title = { Text("Batalkan pesanan?", fontWeight = FontWeight.ExtraBold) },
            text = { Text("Pesanan ${o.kode} akan dihapus dan stok produk dikembalikan.") },
            confirmButton = {
                TextButton(onClick = { Repo.cancelOrder(o); batal = null }) {
                    Text("Ya, batalkan", color = Color(0xFF9B1C14))
                }
            },
            dismissButton = { TextButton(onClick = { batal = null }) { Text("Tidak") } }
        )
    }
}

@Composable
private fun OrderCard(o: Order, onEdit: (Order) -> Unit, onCancel: (Order) -> Unit) {
    val (bg, fg) = statusColors(o.status)
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(o.kode, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                StatusChip(o.status, bg, fg)
            }
            Text("${o.tanggal} · ${o.jumlah} x ${o.produkNama}", color = Muted, fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp))
            Text("Kirim ke: ${o.alamat}", color = Muted, fontSize = 12.sp)
            val step = when (o.status) { "Menunggu" -> 1; "Diproses" -> 2; "Dikirim" -> 3; else -> 0 }
            if (step > 0) Timeline(step)
            HorizontalDivider(Modifier.padding(top = 14.dp, bottom = 12.dp), color = Line)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total harga", color = Muted, fontSize = 13.sp)
                Text(rupiah(o.total), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            if (o.status == "Menunggu") Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onEdit(o) }) { Text("Ubah alamat", fontWeight = FontWeight.Bold) }
                TextButton(onClick = { onCancel(o) }) {
                    Text("Batalkan", fontWeight = FontWeight.Bold, color = Color(0xFF9B1C14))
                }
            }
        }
    }
}

@Composable
private fun Timeline(step: Int) {
    val labels = listOf("Dibuat", "Diproses", "Dikirim")
    Column(Modifier.padding(top = 16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            labels.indices.forEach { i ->
                Box(Modifier.size(12.dp).clip(CircleShape).background(if (i < step) Gold else Line))
                if (i < labels.lastIndex)
                    Box(Modifier.weight(1f).height(3.dp).background(if (i < step - 1) Gold else Line))
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEach { Text(it, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Muted) }
        }
    }
}