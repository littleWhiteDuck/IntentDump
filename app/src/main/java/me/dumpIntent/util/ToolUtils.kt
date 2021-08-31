package me.dumpIntent.util

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

object ToolUtils {

    @SuppressLint("WrongConstant")
    fun toClip(context: Context, configs:String){
        (context.getSystemService("clipboard") as ClipboardManager).setPrimaryClip(
            ClipData.newPlainText(
                "app_config",
                configs
            )
        )
    }
}