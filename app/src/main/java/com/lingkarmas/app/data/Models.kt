package com.lingkarmas.app.data

data class User(val id: Long, val nama: String, val email: String, val hp: String)

data class Product(
    val id: Long,
    val nama: String,
    val kategori: String,
    val deskripsi: String,
    val spesifikasi: String,
    val harga: Int,
    val stok: Int,
    val foto: String,
    val terjual: Int = 0
) {
    /** Dua baris pertama spesifikasi, untuk kartu produk. */
    val ringkas: String
        get() = spesifikasi.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString(" · ")
}

data class Order(
    val id: Long, val kode: String, val tanggal: String, val status: String,
    val productId: Long, val produkNama: String, val jumlah: Int,
    val alamat: String, val total: Int
)

fun rupiah(v: Int): String = "Rp " + "%,d".format(v).replace(',', '.')