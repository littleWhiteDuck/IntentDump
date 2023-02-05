package me.dumpIntent.bean

import android.graphics.drawable.Drawable
import androidx.annotation.Keep

@Keep
data class ConfigBean(
    val packageName: String, val className: String,
    val action: String, val data: String,
    val extras: List<ExtraBean>
)

@Keep
data class ExtraBean(val type: String, val key: String, val value: String)

data class MainItem(val id: Int, val appName: String, val appIcon: Drawable, val configBean: ConfigBean)
