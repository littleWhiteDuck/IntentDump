package me.dumpIntent.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import me.dumpIntent.R

object AppUtils {
    fun getAppName(context: Context, packageName: String): String {
        return try {
            context.packageManager.getPackageInfo(packageName, 0).applicationInfo.loadLabel(
                context.packageManager
            ).toString()
        } catch (e: Exception) {
            "unknown"
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun getIcon(context: Context, packageName: String): Drawable {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            context.resources.getDrawable(R.drawable.ic_launcher_foreground, null)
        }
    }
}