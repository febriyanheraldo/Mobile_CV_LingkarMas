
package com.lingkarmas.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingkarmas.app.data.Product
import com.lingkarmas.app.data.Repo
import com.lingkarmas.app.data.rupiah
import com.lingkarmas.app.ui.ChipRow
import com.lingkarmas.app.ui.ProductCard
import com.lingkarmas.app.ui.ProductImage
import com.lingkarmas.app.ui.theme.*

@Composable
fun CatalogScreen(onOrder: (Long) -> Unit) {
    var query by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("Semua") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    val list = Repo.products.filter {
        (cat == "Semua" || it.kategori == cat) &&
                (
                        it.nama.contains(query, ignoreCase = true) ||
                                it.deskripsi.contains(query, ignoreCase = true)
                        )
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp)
    ) {
        Text(
            "Katalog Produk",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 24.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            placeholder = { Text("Cari produk") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Navy,
                unfocusedBorderColor = Line,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        ChipRow(Repo.kategori, cat) {
            cat = it
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(list, key = { it.id }) { p ->
                Box(
                    Modifier.fillMaxWidth().clickable {
                        selectedProduct = p
                    }
                ) {
                    ProductCard(p) {
                        onOrder(p.id)
                    }
                }
            }
        }
    }

    selectedProduct?.let { p ->
        AlertDialog(
            onDismissRequest = { selectedProduct = null },
            title = {
                Text(
                    "Detail Produk",
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProductImage(
                        p.foto,
                        Modifier.fillMaxWidth().height(180.dp)
                    )

                    Text(
                        p.nama,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )

                    Text(
                        p.kategori,
                        fontSize = 13.sp,
                        color = Muted
                    )

                    HorizontalDivider()

                    Text(
                        "Deskripsi Produk",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Text(
                        p.deskripsi.ifBlank { "Belum ada deskripsi produk." },
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )

                    Text(
                        "Spesifikasi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Text(
                        p.spesifikasi,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )

                    HorizontalDivider()

                    Text(
                        "Harga: ${rupiah(p.harga)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Navy
                    )

                    Text(
                        "Stok tersedia: ${p.stok}",
                        fontSize = 13.sp,
                        color = if (p.stok <= 5) Color(0xFF9B1C14) else Muted
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedProduct = null
                        onOrder(p.id)
                    }
                ) {
                    Text("Buat Pesanan", color = Navy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedProduct = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}
