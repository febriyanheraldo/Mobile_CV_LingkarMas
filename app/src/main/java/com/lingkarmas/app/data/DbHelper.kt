
package com.lingkarmas.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) :
    SQLiteOpenHelper(context, "lingkarmas.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nama TEXT NOT NULL, email TEXT NOT NULL UNIQUE, " +
                    "hp TEXT NOT NULL, password TEXT NOT NULL)"
        )

        db.execSQL(
            "CREATE TABLE products(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nama TEXT NOT NULL, kategori TEXT NOT NULL, " +
                    "deskripsi TEXT NOT NULL DEFAULT '', " +
                    "spesifikasi TEXT NOT NULL, harga INTEGER NOT NULL, " +
                    "stok INTEGER NOT NULL, foto TEXT NOT NULL)"
        )

        db.execSQL(
            "CREATE TABLE orders(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "tanggal TEXT NOT NULL, status TEXT NOT NULL, " +
                    "user_id INTEGER NOT NULL, product_id INTEGER NOT NULL, " +
                    "produk_nama TEXT NOT NULL, jumlah INTEGER NOT NULL, " +
                    "alamat TEXT NOT NULL, total INTEGER NOT NULL)"
        )

        seed(db)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (oldVersion < 2) {
            db.execSQL(
                "ALTER TABLE products ADD COLUMN deskripsi TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    private fun seed(db: SQLiteDatabase) {
        val data = listOf(
            Product(
                0, "Industrial Pump", "Pompa & Blower",
                "Pompa industri untuk mendukung proses pemindahan cairan.",
                "5.5 kW, material besi cor",
                12500000, 15, "pump"
            ),
            Product(
                0, "Industrial Roots Blower", "Pompa & Blower",
                "Blower industri untuk menghasilkan aliran udara secara stabil.",
                "Kapasitas 10 m³/menit, 3 lobe",
                28000000, 8, "roots_blower"
            ),
            Product(
                0, "Industrial Bearing", "Komponen Mesin",
                "Komponen bantalan untuk membantu mengurangi gesekan pada mesin.",
                "Baja kromium, seri 6300",
                450000, 120, "bearing"
            ),
            Product(
                0, "Industrial Oil Seal", "Komponen Mesin",
                "Komponen penyekat untuk mencegah kebocoran oli pada mesin.",
                "Karet NBR, tahan oli dan panas",
                85000, 300, "oil_seal"
            )
        )

        data.forEach { p ->
            db.insert(
                "products",
                null,
                ContentValues().apply {
                    put("nama", p.nama)
                    put("kategori", p.kategori)
                    put("deskripsi", p.deskripsi)
                    put("spesifikasi", p.spesifikasi)
                    put("harga", p.harga)
                    put("stok", p.stok)
                    put("foto", p.foto)
                }
            )
        }
    }
}
