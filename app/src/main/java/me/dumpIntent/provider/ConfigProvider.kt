package me.dumpIntent.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.net.Uri

class ConfigProvider : ContentProvider() {
    private val configDir = 0
    private var dbHelper: ConfigHelper? = null
    private val authority = "me.dumpIntent.provider"
    private val uriMatcher by lazy {
        val matcher = UriMatcher(UriMatcher.NO_MATCH)
        matcher.addURI(authority, "configs", configDir)
        matcher
    }

    override fun onCreate() = context?.let {
        dbHelper = ConfigHelper(it, "config.db", 1)
        true
    } ?: false

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ) = dbHelper?.let {
        val db = it.readableDatabase
        val cursor = when (uriMatcher.match(uri)) {
            configDir -> {
                db.query("IntentConfig", projection, selection, selectionArgs, null, null, sortOrder)
            }
            else -> null
        }
        cursor
    }

    override fun getType(uri: Uri) = when (uriMatcher.match(uri)) {
        configDir -> "vnd.android.cursor.dir/vnd.me.dumpIntent.provider.configs"
        else -> null
    }

    override fun insert(uri: Uri, values: ContentValues?) = dbHelper?.let {
        val db = it.readableDatabase
        val uriReturn = when (uriMatcher.match(uri)) {
            configDir -> {
                val newConfigId = db.insert("IntentConfig", null, values)
                Uri.parse("content://$authority/IntentConfig/$newConfigId")
            }
            else -> null
        }
        uriReturn
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) =
        dbHelper?.let {
            val db = it.readableDatabase
            val count = when (uriMatcher.match(uri)) {
                configDir -> db.delete("IntentConfig", selection, selectionArgs)
                else -> 0
            }
            count
        } ?: 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        TODO("Not yet implemented")
    }
}