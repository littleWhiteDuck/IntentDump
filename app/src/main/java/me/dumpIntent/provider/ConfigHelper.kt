package me.dumpIntent.provider

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ConfigHelper(
    context: Context,
    name: String,
    version: Int,
) : SQLiteOpenHelper(context, name, null, version) {
    private val createConfig = "create table IntentConfig (" +
            "config text," +
            "id integer primary key autoincrement)"
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createConfig)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }
}