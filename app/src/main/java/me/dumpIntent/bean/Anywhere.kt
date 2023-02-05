package me.dumpIntent.bean

import androidx.annotation.Keep

@Keep
data class Bean1(
    val app_name: String,
    val category: String = "",
    val color: Int = 0,
    val description: String = "",
    val execWithRoot: Boolean = false,
    val iconUri: String = "",
    val id: String,
    val param_1: String,
    val param_2: String,
    val param_3: String,
    val time_stamp: String,
    val type: Int = 1
)

@Keep
data class Bean2(
    val data: String,
    val action: String,
    val category: String,
    val extras: Any
)
