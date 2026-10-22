package com.lingkarmas.app.data

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Repo {
    private lateinit var helper: DbHelper
    private lateinit var prefs: SharedPreferences

    val products = mutableStateListOf<Product>()
    val orders = mutableStateListOf<Order>()
    var user by mutableStateOf<User?>(null)
        private set

    val kategori: List<String>
        get() = listOf("Semua") + products.map { it.kategori }.distinct()

    fun init(context: Context) {
        if (::helper.isInitialized) return
        helper = DbHelper(context.applicationContext)
        prefs = context.applicationContext.getSharedPreferences("session", Context.MODE_PRIVATE)
        loadProducts()
        val uid = prefs.getLong("uid", -1L)
        if (uid != -1L) {
            helper.readableDatabase.rawQuery(
                "SELECT id,nama,email,hp FROM users WHERE id=?", arrayOf(uid.toString())
            ).use { c ->
                if (c.moveToFirst()) startSession(User(c.getLong(0), c.getString(1), c.getString(2), c.getString(3)))
            }
        }
    }

    private fun hash(s: String) = MessageDigest.getInstance("SHA-256")
        .digest(s.toByteArray()).joinToString("") { "%02x".format(it) }

    private fun today() = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date())

    private fun startSession(u: User?) {
        user = u
        prefs.edit().putLong("uid", u?.id ?: -1L).apply()
        loadOrders()
    }

    // ---------- USER ----------
    fun register(nama: String, email: String, hp: String, pass: String): String? {
        val mail = email.trim().lowercase()
        helper.readableDatabase.rawQuery("SELECT id FROM users WHERE email=?", arrayOf(mail)).use {
            if (it.moveToFirst()) return "Email sudah terdaftar"
        }
        helper.writableDatabase.insert("users", null, ContentValues().apply {
            put("nama", nama.trim()); put("email", mail); put("hp", hp.trim()); put("password", hash(pass))
        })
        return null
    }

    fun login(email: String, pass: String): Boolean {
        helper.readableDatabase.rawQuery(
            "SELECT id,nama,email,hp FROM users WHERE email=? AND password=?",
            arrayOf(email.trim().lowercase(), hash(pass))
        ).use { c ->
            if (c.moveToFirst()) {
                startSession(User(c.getLong(0), c.getString(1), c.getString(2), c.getString(3)))
                return true
            }
        }
        return false
    }

    fun logout() = startSession(null)

    fun updateProfile(nama: String, hp: String) {
        val u = user ?: return
        helper.writableDatabase.update("users", ContentValues().apply {
            put("nama", nama.trim()); put("hp", hp.trim())
        }, "id=?", arrayOf(u.id.toString()))
        user = u.copy(nama = nama.trim(), hp = hp.trim())
    }

    fun deleteAccount() {
        val u = user ?: return
        val w = helper.writableDatabase
        w.delete("orders", "user_id=?", arrayOf(u.id.toString()))
        w.delete("users", "id=?", arrayOf(u.id.toString()))
        startSession(null)
    }

    // ---------- PRODUK ----------
    fun loadProducts() {
        val list = mutableListOf<Product>()
        helper.readableDatabase.rawQuery(
            "SELECT id,nama,kategori,spesifikasi,harga,stok,foto FROM products ORDER BY id", null
        ).use { c ->
            while (c.moveToNext()) list.add(
                Product(c.getLong(0), c.getString(1), c.getString(2), c.getString(3),
                    c.getInt(4), c.getInt(5), c.getString(6))
            )
        }
        products.clear(); products.addAll(list)
    }

    // p.id == 0 -> tambah baru, selain itu -> ubah
    fun saveProduct(p: Product) {
        val cv = ContentValues().apply {
            put("nama", p.nama); put("kategori", p.kategori); put("spesifikasi", p.spesifikasi)
            put("harga", p.harga); put("stok", p.stok); put("foto", p.foto)
        }
        if (p.id == 0L) helper.writableDatabase.insert("products", null, cv)
        else helper.writableDatabase.update("products", cv, "id=?", arrayOf(p.id.toString()))
        loadProducts()
    }

    fun deleteProduct(id: Long) {
        helper.writableDatabase.delete("products", "id=?", arrayOf(id.toString()))
        loadProducts()
    }

    // ---------- PESANAN ----------
    fun loadOrders() {
        val u = user
        val list = mutableListOf<Order>()
        if (u != null) {
            helper.readableDatabase.rawQuery(
                "SELECT id,tanggal,status,product_id,produk_nama,jumlah,alamat,total " +
                        "FROM orders WHERE user_id=? ORDER BY id DESC", arrayOf(u.id.toString())
            ).use { c ->
                while (c.moveToNext()) list.add(
                    Order(c.getLong(0), "LM-%04d".format(c.getLong(0)), c.getString(1), c.getString(2),
                        c.getLong(3), c.getString(4), c.getInt(5), c.getString(6), c.getInt(7))
                )
            }
        }
        orders.clear(); orders.addAll(list)
    }

    // Mengembalikan pesan error, atau null jika berhasil
    fun createOrder(p: Product, jumlah: Int, alamat: String): String? {
        val u = user ?: return "Silakan login dulu"
        val cur = products.firstOrNull { it.id == p.id } ?: return "Produk tidak ditemukan"
        if (jumlah > cur.stok) return "Stok tidak cukup"
        val w = helper.writableDatabase
        w.beginTransaction()
        try {
            w.insert("orders", null, ContentValues().apply {
                put("tanggal", today()); put("status", "Menunggu"); put("user_id", u.id)
                put("product_id", cur.id); put("produk_nama", cur.nama); put("jumlah", jumlah)
                put("alamat", alamat); put("total", cur.harga * jumlah)
            })
            w.execSQL("UPDATE products SET stok = stok - ? WHERE id=?", arrayOf<Any>(jumlah, cur.id))
            w.setTransactionSuccessful()
        } finally { w.endTransaction() }
        loadProducts(); loadOrders()
        return null
    }

    fun updateAlamat(orderId: Long, alamat: String) {
        helper.writableDatabase.update("orders", ContentValues().apply { put("alamat", alamat) },
            "id=?", arrayOf(orderId.toString()))
        loadOrders()
    }

    // Hanya pesanan berstatus "Menunggu" yang bisa dibatalkan; stok dikembalikan
    fun cancelOrder(o: Order) {
        if (o.status != "Menunggu") return
        val w = helper.writableDatabase
        w.beginTransaction()
        try {
            w.delete("orders", "id=?", arrayOf(o.id.toString()))
            w.execSQL("UPDATE products SET stok = stok + ? WHERE id=?", arrayOf<Any>(o.jumlah, o.productId))
            w.setTransactionSuccessful()
        } finally { w.endTransaction() }
        loadProducts(); loadOrders()
    }
}