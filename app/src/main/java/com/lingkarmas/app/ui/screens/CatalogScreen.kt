package com.lingkarmas.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingkarmas.app.data.Repo
import com.lingkarmas.app.ui.ChipRow
import com.lingkarmas.app.ui.ProductCard
import com.lingkarmas.app.ui.theme.*

@Composable
fun CatalogScreen(onOrder: (Long) -> Unit) {
    var query by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("Semua") }
    val list = Repo.products.filter {
        (cat == "Semua" || it.kategori == cat) && it.nama.contains(query, ignoreCase = true)
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Text("Katalog Produk", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 24.dp))
        OutlinedTextField(
            query, { query = it }, singleLine = true, placeholder = { Text("Cari produk") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Navy, unfocusedBorderColor = Line,
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )
        Spacer(Modifier.height(16.dp))
        ChipRow(Repo.kategori, cat) { cat = it }
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            items(list) { p -> ProductCard(p) { onOrder(p.id) } }
        }
    }
}