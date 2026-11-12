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
    val foto: String
)
data class Order(
    val id: Long, val kode: String, val tanggal: String, val status: String,
    val productId: Long, val produkNama: String, val jumlah: Int,
    val alamat: String, val total: Int
)

fun rupiah(v: Int): String = "Rp " + "%,d".format(v).replace(',', '.')