package com.lingkarmas.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Ubah sesuai perangkat. Harus berakhiran "/". */
object ApiConfig {
    // Emulator Android Studio:
    // const val BASE_URL = "http://10.0.2.2:8000/api/"

    // HP asli (satu Wi-Fi dengan laptop): pakai IP laptop, contoh
    const val BASE_URL = "http://192.168.0.101:8000/api/"
}

class ApiException(val code: Int, override val message: String) : Exception(message)

/** JSON null menjadi string kosong (bukan teks "null"). */
fun JSONObject.str(key: String): String = if (isNull(key)) "" else optString(key, "")

object Api {
    var token: String? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonType = "application/json; charset=utf-8".toMediaType()

    /** Mengembalikan isi respons (teks JSON). Melempar ApiException jika gagal. */
    suspend fun call(
        method: String,
        path: String,
        body: JSONObject? = null,
        auth: Boolean = true
    ): String = withContext(Dispatchers.IO) {
        val builder = Request.Builder()
            .url(ApiConfig.BASE_URL + path)
            .header("Accept", "application/json")

        val t = token
        if (auth && t != null) builder.header("Authorization", "Bearer $t")

        val payload = (body ?: JSONObject()).toString().toRequestBody(jsonType)
        when (method) {
            "POST" -> builder.post(payload)
            "PUT" -> builder.put(payload)
            "DELETE" -> builder.delete()
            else -> builder.get()
        }

        try {
            client.newCall(builder.build()).execute().use { res ->
                val text = res.body?.string().orEmpty()
                if (!res.isSuccessful) throw ApiException(res.code, errorMessage(res.code, text))
                text
            }
        } catch (e: IOException) {
            android.util.Log.e("LingkarApi", "Gagal ke ${ApiConfig.BASE_URL}: $e", e)
            throw ApiException(0, "Tidak dapat terhubung ke server. Periksa koneksi Anda.")
        }
    }

    private fun errorMessage(code: Int, text: String): String {
        val msg = try {
            JSONObject(text).optString("message", "")
        } catch (_: Exception) {
            ""
        }
        if (msg.isNotBlank()) return msg

        return when (code) {
            401 -> "Sesi berakhir, silakan login kembali"
            403 -> "Anda tidak memiliki akses"
            404 -> "Data tidak ditemukan"
            422 -> "Data tidak valid"
            429 -> "Terlalu banyak percobaan, coba lagi beberapa saat lagi"
            else -> "Terjadi kesalahan pada server ($code)"
        }
    }
}