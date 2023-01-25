package me.dumpIntent.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.contentValuesOf

class RecordReceiver : BroadcastReceiver() {
    private val uri = Uri.parse("content://me.dumpIntent.provider/configs")
    override fun onReceive(context: Context, intent: Intent) {
        val recordString = intent.getStringExtra("record_intent_hook")
        val contentValues = contentValuesOf("config" to recordString)
        context.contentResolver?.insert(uri, contentValues)
    }
}