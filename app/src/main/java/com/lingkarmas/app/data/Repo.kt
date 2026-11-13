package com.lingkarmas.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object Repo {
    private lateinit var prefs: SharedPreferences

    val products = mutableStateListOf<Product>()
    val orders = mutableStateListOf<Order>()

    var user by mutableStateOf<User?>(null)
        private set

    /** true selama produk/pesanan sedang dimuat. */
    var loading by mutableStateOf(false)
        private set

    /** Pesan error terakhir saat memuat data (null jika tidak ada). */
    var loadError by mutableStateOf<String?>(null)
        private set

    val kategori: List<String>
        get() = listOf("Semua") + products.map { it.kategori }.distinct()

    private var active = 0

    // ---------- SESI ----------

    fun init(context: Context) {
        if (::prefs.isInitialized) return

        prefs = context.applicationContext.getSharedPreferences("session", Context.MODE_PRIVATE)
        Api.token = prefs.getString("token", null)

        val cached = prefs.getString("user", null)
        if (Api.token != null && cached != null) {
            user = try { JSONObject(cached).toUser() } catch (_: Exception) { null }
        }
        if (user == null) clearSession()
    }

    private fun userJson(u: User) = JSONObject()
        .put("id", u.id).put("nama", u.nama).put("email", u.email).put("hp", u.hp)
        .toString()

    private fun startSession(token: String, u: User) {
        Api.token = token
        user = u
        prefs.edit().putString("token", token).putString("user", userJson(u)).apply()
    }

    private fun updateCachedUser(u: User) {
        user = u
        prefs.edit().putString("user", userJson(u)).apply()
    }

    private fun clearSession() {
        Api.token = null
        user = null
        products.clear()
        orders.clear()
        loadError = null
        if (::prefs.isInitialized) prefs.edit().clear().apply()
    }

    // ---------- PEMBANTU ----------

    private class Res<T>(val data: T?, val error: String?)

    /** 401 pada request berotorisasi mengakhiri sesi (layar otomatis kembali ke login). */
    private fun fail(e: ApiException, authed: Boolean): String {
        if (e.code == 401 && authed) clearSession()
        return e.message
    }

    private suspend fun <T> attempt(authed: Boolean = true, block: suspend () -> T): Res<T> =
        try {
            Res(block(), null)
        } catch (e: ApiException) {
            Res(null, fail(e, authed))
        } catch (e: JSONException) {
            Res(null, "Respons server tidak dikenali")
        }

    private suspend fun <T> tracked(block: suspend () -> T): T {
        active++
        loading = true
        try {
            return block()
        } finally {
            active--
            loading = active > 0
        }
    }

    /** Jika server belum punya foto, pakai drawable lokal sesuai nama produk. */
    private fun fotoFor(nama: String, url: String): String {
        if (url.isNotBlank()) return url
        val n = nama.lowercase()
        return when {
            "pump" in n || "pompa" in n -> "pump"
            "roots" in n || "blower" in n -> "roots_blower"
            "bearing" in n -> "bearing"
            "seal" in n -> "oil_seal"
            else -> ""
        }
    }

    private fun JSONObject.toUser() = User(getLong("id"), str("nama"), str("email"), str("hp"))

    private fun JSONObject.toProduct() = Product(
        id = getLong("id"),
        nama = str("nama"),
        kategori = str("kategori"),
        deskripsi = "",
        spesifikasi = str("spesifikasi"),
        harga = optInt("harga"),
        stok = optInt("stok"),
        foto = fotoFor(str("nama"), str("foto_url")),
        terjual = optInt("terjual")
    )

    private fun JSONObject.toOrder() = Order(
        id = getLong("id"),
        kode = str("kode"),
        tanggal = str("tanggal"),
        status = str("status"),
        productId = optLong("product_id"),   // 0 jika produk sudah dihapus admin
        produkNama = str("produk_nama"),
        jumlah = optInt("jumlah"),
        alamat = str("alamat"),
        total = optInt("total")
    )

    // ---------- USER ----------

    /** Mengembalikan pesan error, atau null jika berhasil. */
    suspend fun register(nama: String, email: String, hp: String, pass: String): String? =
        attempt(authed = false) {
            Api.call(
                "POST", "register",
                JSONObject()
                    .put("nama", nama.trim())
                    .put("email", email.trim().lowercase())
                    .put("hp", hp.trim())
                    .put("password", pass),
                auth = false
            )
        }.error

    /** Mengembalikan pesan error, atau null jika berhasil. */
    suspend fun login(email: String, pass: String): String? =
        attempt(authed = false) {
            val res = JSONObject(
                Api.call(
                    "POST", "login",
                    JSONObject().put("email", email.trim().lowercase()).put("password", pass),
                    auth = false
                )
            )
            startSession(res.getString("token"), res.getJSONObject("user").toUser())
        }.error

    suspend fun logout() {
        try {
            Api.call("POST", "logout")
        } catch (_: ApiException) {
        } finally {
            clearSession()
        }
    }

    suspend fun updateProfile(nama: String, hp: String): String? =
        attempt {
            val res = JSONObject(
                Api.call("PUT", "me", JSONObject().put("nama", nama.trim()).put("hp", hp.trim()))
            )
            updateCachedUser(res.toUser())
        }.error

    suspend fun deleteAccount(): String? =
        attempt {
            Api.call("DELETE", "me")
            clearSession()
        }.error

    // ---------- PRODUK ----------

    suspend fun loadProducts() {
        if (user == null) return
        loadError = null
        val r = tracked {
            attempt {
                val arr = JSONArray(Api.call("GET", "products"))
                (0 until arr.length()).map { arr.getJSONObject(it).toProduct() }
            }
        }
        r.data?.let { products.clear(); products.addAll(it) }
        r.error?.let { loadError = it }
    }

    // ---------- BEST SELLER (data `terjual` dari server) ----------

    fun getBestSellers(limit: Int = 5): List<Product> =
        products.sortedByDescending { it.terjual }.take(limit)

    fun getProductSalesCount(productId: Long): Int =
        products.firstOrNull { it.id == productId }?.terjual ?: 0

    // ---------- PESANAN ----------

    suspend fun loadOrders() {
        if (user == null) return
        loadError = null
        val r = tracked {
            attempt {
                val arr = JSONArray(Api.call("GET", "orders"))
                (0 until arr.length()).map { arr.getJSONObject(it).toOrder() }
            }
        }
        r.data?.let { orders.clear(); orders.addAll(it) }
        r.error?.let { loadError = it }
    }

    /** Total dihitung server. Error stok: "Stok tidak cukup". */
    suspend fun createOrder(p: Product, jumlah: Int, alamat: String): String? {
        val r = attempt {
            Api.call(
                "POST", "orders",
                JSONObject().put("product_id", p.id).put("jumlah", jumlah).put("alamat", alamat)
            )
        }
        if (r.error != null) return r.error

        loadProducts()   // stok di katalog ikut berubah
        loadOrders()
        return null
    }

    suspend fun updateAlamat(orderId: Long, alamat: String): String? {
        val r = attempt { Api.call("PUT", "orders/$orderId", JSONObject().put("alamat", alamat)) }
        loadOrders()     // sinkronkan juga saat gagal (status mungkin sudah diubah admin)
        return r.error
    }

    suspend fun cancelOrder(o: Order): String? {
        val r = attempt { Api.call("DELETE", "orders/${o.id}") }
        if (r.error == null) loadProducts()
        loadOrders()
        return r.error
    }
}