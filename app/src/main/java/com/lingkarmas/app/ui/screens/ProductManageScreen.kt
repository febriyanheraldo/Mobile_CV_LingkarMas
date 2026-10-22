package com.lingkarmas.app.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingkarmas.app.data.Product
import com.lingkarmas.app.data.Repo
import com.lingkarmas.app.data.rupiah
import com.lingkarmas.app.ui.GoldButton
import com.lingkarmas.app.ui.ProductImage
import com.lingkarmas.app.ui.theme.*
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProductManageScreen(onBack: () -> Unit) {
    var form by remember { mutableStateOf<Product?>(null) }
    var hapus by remember { mutableStateOf<Product?>(null) }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") }
            Text("Kelola Produk", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.height(12.dp))
        GoldButton("+ Tambah Produk") { form = Product(0, "", "", "", 0, 0, "") }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            items(Repo.products, key = { it.id }) { p ->
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        ProductImage(p.foto, Modifier.size(56.dp))
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(p.nama, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${rupiah(p.harga)} · Stok ${p.stok}", color = Muted, fontSize = 12.sp)
                        }
                        IconButton(onClick = { form = p }) { Icon(Icons.Default.Edit, "Ubah ${p.nama}") }
                        IconButton(onClick = { hapus = p }) {
                            Icon(Icons.Default.Delete, "Hapus ${p.nama}", tint = Color(0xFF9B1C14))
                        }
                    }
                }
            }
        }
    }

    form?.let { ProductFormDialog(it) { form = null } }
    hapus?.let { p ->
        AlertDialog(
            onDismissRequest = { hapus = null },
            title = { Text("Hapus produk?", fontWeight = FontWeight.ExtraBold) },
            text = { Text("${p.nama} akan dihapus dari katalog.") },
            confirmButton = {
                TextButton(onClick = { Repo.deleteProduct(p.id); hapus = null }) {
                    Text("Ya, hapus", color = Color(0xFF9B1C14))
                }
            },
            dismissButton = { TextButton(onClick = { hapus = null }) { Text("Batal") } }
        )
    }
}

@Composable
private fun ProductFormDialog(p: Product, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    var nama by remember { mutableStateOf(p.nama) }
    var kategori by remember { mutableStateOf(p.kategori) }
    var spek by remember { mutableStateOf(p.spesifikasi) }
    var harga by remember { mutableStateOf(if (p.id == 0L) "" else p.harga.toString()) }
    var stok by remember { mutableStateOf(if (p.id == 0L) "" else p.stok.toString()) }
    var foto by remember { mutableStateOf(p.foto) }
    var err by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) saveImage(ctx, uri)?.let { foto = it }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (p.id == 0L) "Tambah Produk" else "Ubah Produk", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ProductImage(foto, Modifier.size(96.dp))
                OutlinedButton(onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) { Text("Pilih foto") }
                OutlinedTextField(nama, { nama = it }, label = { Text("Nama produk") }, singleLine = true)
                OutlinedTextField(kategori, { kategori = it }, label = { Text("Kategori") }, singleLine = true)
                OutlinedTextField(spek, { spek = it }, label = { Text("Spesifikasi") })
                OutlinedTextField(harga, { harga = it }, label = { Text("Harga (Rp)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(stok, { stok = it }, label = { Text("Stok") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                err?.let { Text(it, color = Color(0xFF9B1C14), fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val h = harga.toIntOrNull()
                val s = stok.toIntOrNull()
                if (nama.isBlank() || kategori.isBlank() || h == null || s == null) {
                    err = "Nama, kategori, harga, dan stok wajib diisi dengan benar"
                } else {
                    Repo.saveProduct(Product(p.id, nama.trim(), kategori.trim(), spek.trim(), h, s, foto))
                    onDismiss()
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

// Salin foto pilihan ke penyimpanan aplikasi (diperkecil), kembalikan path file-nya
private fun saveImage(ctx: Context, uri: Uri): String? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > 1000) sample *= 2
        val bmp = ctx.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null
        val f = File(ctx.filesDir, "produk_${System.currentTimeMillis()}.jpg")
        FileOutputStream(f).use { bmp.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        f.absolutePath
    } catch (e: Exception) {
        null
    }
}