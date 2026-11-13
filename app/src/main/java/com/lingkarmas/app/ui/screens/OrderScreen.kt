package com.lingkarmas.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingkarmas.app.data.Repo
import com.lingkarmas.app.data.Order
import com.lingkarmas.app.data.rupiah
import com.lingkarmas.app.ui.GoldButton
import com.lingkarmas.app.ui.theme.*
import com.lingkarmas.app.ui.ProductImage
import kotlinx.coroutines.launch

@Composable
fun OrderScreen(productId: Long, onBack: () -> Unit, onDone: () -> Unit) {
    val p = Repo.products.firstOrNull { it.id == productId }
    if (p == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }
    val scope = rememberCoroutineScope()
    var sending by remember { mutableStateOf(false) }
    var qty by remember { mutableIntStateOf(1) }
    var alamat by remember { mutableStateOf("") }
    var showOk by remember { mutableStateOf(false) }
    var showErr by remember { mutableStateOf(false) }
    var alamatErr by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") }
                Text("Buat Pesanan", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(12.dp))
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProductImage(p.foto, Modifier.size(84.dp))
                    Column(Modifier.padding(start = 14.dp)) {
                        Text(p.nama, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(p.ringkas, color = Muted, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Stok tersedia: ${p.stok}", color = Color(0xFF0B5E4D),
                            fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Jumlah pesanan", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Muted)
                        Text("Harga satuan ${rupiah(p.harga)}", fontSize = 12.sp, color = Muted)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedIconButton(onClick = { if (qty > 1) qty-- }, modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.Remove, "Kurangi") }
                        Text("$qty", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        FilledIconButton(onClick = { if (qty < p.stok) qty++ else showErr = true },
                            modifier = Modifier.size(44.dp), shape = RoundedCornerShape(14.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Navy)
                        ) { Icon(Icons.Default.Add, "Tambah") }
                    }
                }
            }
            if (showErr) Text("Stok tidak cukup", color = Color(0xFF9B1C14),
                fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(alamat, { alamat = it }, label = { Text("Alamat pengiriman") },
                shape = RoundedCornerShape(14.dp), minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Navy, unfocusedBorderColor = Line,
                    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
                modifier = Modifier.fillMaxWidth())
        }
        Surface(shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), shadowElevation = 12.dp,
            color = Color.White) {
            Column(Modifier.padding(20.dp)) {
                Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Subtotal", color = Muted, fontSize = 14.sp)
                    Text(rupiah(p.harga * qty), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }
                alamatErr?.let {
                    Text(it, color = Color(0xFF9B1C14), fontWeight = FontWeight.Bold,
                        fontSize = 13.sp, modifier = Modifier.padding(bottom = 10.dp))
                }
                GoldButton(
                    if (sending) "Memproses..." else "Buat Pesanan",
                    enabled = !sending
                ) {
                    if (alamat.isBlank()) {
                        alamatErr = "Alamat pengiriman wajib diisi"
                        return@GoldButton
                    }
                    alamatErr = null
                    sending = true
                    scope.launch {
                        val err = Repo.createOrder(p, qty, alamat.trim())
                        sending = false
                        if (err == null) showOk = true else alamatErr = err
                    }
                }
            }
        }
    }
    if (showOk) AlertDialog(
        onDismissRequest = {}, shape = RoundedCornerShape(24.dp),
        title = { Text("Pesanan Berhasil", fontWeight = FontWeight.ExtraBold) },
        text = { Text("Pesananmu sudah dikirim dan menunggu konfirmasi admin.") },
        confirmButton = { TextButton(onClick = { showOk = false; onDone() }) { Text("Lihat Riwayat") } }
    )
}