
package com.lingkarmas.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.lingkarmas.app.data.Product
import com.lingkarmas.app.data.Repo
import com.lingkarmas.app.data.rupiah
import com.lingkarmas.app.ui.*
import com.lingkarmas.app.ui.theme.*
@Composable
fun HomeScreen(onOrder: (Long) -> Unit, onSeeAll: () -> Unit) {
    var cat by remember { mutableStateOf("Semua") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    val orders = Repo.orders
    val bestSellers = remember(Repo.products.size, orders.size) {
        Repo.getBestSellers(5)
    }

    Column(
        Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Selamat datang,", color = Muted, fontSize = 13.sp)
                Text(
                    Repo.user?.nama ?: "",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            LogoRing()
        }

        Spacer(Modifier.height(20.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Navy)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Pesanan aktif", color = Color(0xFFB8C6D6), fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    listOf("Diproses", "Dikirim", "Menunggu").forEach { s ->
                        Column {
                            Text(
                                "${orders.count { it.status == s }}",
                                color = Gold,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(s, color = Color(0xFFB8C6D6), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (bestSellers.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Produk Best Seller", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                TextButton(onClick = onSeeAll) {
                    Text("Semua", fontWeight = FontWeight.Bold)
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(items = bestSellers) { p ->
                    BestSellerCard(
                        p = p,
                        soldCount = Repo.getProductSalesCount(p.id),
                        onClick = { selectedProduct = p },
                        onAdd = { onOrder(p.id) }
                    )
                }
            }
        }

        Text(
            "Kategori",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
        )

        ChipRow(Repo.kategori, cat) { cat = it }

        Row(
            Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Produk lainnya", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            TextButton(onClick = onSeeAll) {
                Text("Lihat semua", fontWeight = FontWeight.Bold)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Repo.products
                .filter { cat == "Semua" || it.kategori == cat }
                .take(4)
                .forEach { p ->
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
                Text("Detail Produk", fontWeight = FontWeight.ExtraBold)
            },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProductImage(p.foto, Modifier.fillMaxWidth().height(180.dp))

                    Text(
                        p.nama,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )

                    Text(p.kategori, fontSize = 13.sp, color = Muted)

                    HorizontalDivider()

                    Text("Deskripsi Produk", fontWeight = FontWeight.Bold)
                    Text(
                        p.deskripsi.ifBlank { "Belum ada deskripsi produk." },
                        fontSize = 13.sp
                    )

                    Text("Spesifikasi", fontWeight = FontWeight.Bold)
                    Text(p.spesifikasi, fontSize = 13.sp)

                    HorizontalDivider()

                    Text(
                        "Harga: ${rupiah(p.harga)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Navy
                    )

                    Text("Stok tersedia: ${p.stok}", color = Muted)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedProduct = null
                    onOrder(p.id)
                }) {
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
