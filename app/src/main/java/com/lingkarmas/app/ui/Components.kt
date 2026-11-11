package com.lingkarmas.app.ui

import androidx.compose.runtime.remember
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.lingkarmas.app.data.Product
import com.lingkarmas.app.data.rupiah
import com.lingkarmas.app.ui.theme.*
import com.lingkarmas.app.R

@Composable
fun LogoRing(modifier: Modifier = Modifier) {
    Image(
        painterResource(R.drawable.logo_lingkarmas), "Logo CV. Lingkar Mas",
        modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).background(Color.White).padding(6.dp),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun ProductImage(foto: String, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val base = modifier.clip(RoundedCornerShape(14.dp))
    val bitmap = remember(foto) {
        if (foto.startsWith("/")) BitmapFactory.decodeFile(foto)?.asImageBitmap() else null
    }
    val resId = remember(foto) {
        if (foto.startsWith("/")) 0 else ctx.resources.getIdentifier(foto, "drawable", ctx.packageName)
    }
    when {
        bitmap != null -> Image(bitmap, null, base, contentScale = ContentScale.Crop)
        resId != 0 -> Image(painterResource(resId), null, base, contentScale = ContentScale.Crop)
        else -> Box(base.background(Brush.linearGradient(listOf(PhA, PhB))))
    }
}

@Composable
fun GoldButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)
    ) { Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
}

@Composable
fun ChipRow(items: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach {
            FilterChip(
                selected = it == selected, onClick = { onSelect(it) },
                label = { Text(it, fontWeight = FontWeight.SemiBold) },
                shape = RoundedCornerShape(18.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,
                    selectedContainerColor = Navy, selectedLabelColor = Color.White
                )
            )
        }
    }
}

fun statusColors(s: String): Pair<Color, Color> = when (s) {
    "Menunggu" -> Color(0xFFFFF1CC) to Color(0xFF7A5200)
    "Diproses" -> Color(0xFFDDEBFF) to Color(0xFF124A9C)
    "Dikirim" -> Color(0xFFD5F3EC) to Color(0xFF0B5E4D)
    else -> Color(0xFFFCE0DE) to Color(0xFF9B1C14)
}

@Composable
fun StatusChip(text: String, bg: Color, fg: Color) {
    Text(
        text, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@Composable
fun ProductCard(p: Product, onAdd: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ProductImage(p.foto, Modifier.size(84.dp))

            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(p.nama, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(p.spesifikasi, color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Text(rupiah(p.harga), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                val low = p.stok <= 5
                StatusChip(
                    if (low) "Stok menipis" else "Stok ${p.stok}",
                    if (low) Color(0xFFFFF1CC) else Color(0xFFE3F4EC),
                    if (low) Color(0xFF7A5200) else Color(0xFF0B5E4D)
                )
                Spacer(Modifier.height(10.dp))
                FilledIconButton(
                    onClick = onAdd, modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Navy)
                ) { Icon(Icons.Default.Add, contentDescription = "Pesan ${p.nama}") }
            }
        }
    }
}

@Composable
fun BestSellerCard(
    p: Product,
    soldCount: Int = 0,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(170.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                ProductImage(p.foto, Modifier.fillMaxSize())
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Gold,
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = "Best Seller",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Navy,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = p.nama,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = p.kategori,
                color = Muted,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = rupiah(p.harga),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = Navy
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (soldCount > 0) "$soldCount terjual" else "Stok ${p.stok}",
                    fontSize = 11.sp,
                    color = if (p.stok <= 5 && soldCount == 0) Color(0xFF7A5200) else Muted,
                    fontWeight = FontWeight.Medium
                )

                FilledIconButton(
                    onClick = onClick,
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Navy)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Pesan ${p.nama}",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
