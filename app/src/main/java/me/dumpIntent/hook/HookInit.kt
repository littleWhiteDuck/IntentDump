package me.dumpIntent.hook


import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.content.contentValuesOf
import com.google.gson.Gson
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.dumpIntent.BuildConfig
import me.dumpIntent.bean.ConfigBean
import me.dumpIntent.bean.ExtraBean

private const val ACTIVITY = "android.app.Activity"
private const val CONTEXT_WRAPPER = "android.content.ContextWrapper"
private const val START_ACTIVITY = "startActivity"
private const val START_ACTIVITY_FOR_RESULT = "startActivityForResult"
private val uri = Uri.parse("content://me.dumpIntent.provider/configs")

class HookInit : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == BuildConfig.APPLICATION_ID) {
            selfHook(lpparam.classLoader)
        } else {
            startHook()
        }
    }

    private fun selfHook(classLoader: ClassLoader) {
        XposedHelpers.findAndHookMethod(
            "me.dumpIntent.MainActivity",
            classLoader,
            "isModuleLive",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    super.afterHookedMethod(param)
                    param.result = true
                }
            })
    }

    private fun startHook() {
        XposedHelpers.findAndHookMethod(
            Application::class.java,
            "attach",
            Context::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    super.afterHookedMethod(param)
                    val context = param!!.args[0] as Context
                    intentHook(context.classLoader, context)
                }
            }
        )
    }

    private fun intentHook(classLoader: ClassLoader, context: Context) {
        XposedHelpers.findAndHookMethod(
            ACTIVITY, classLoader,
            START_ACTIVITY, Intent::class.java, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val intent = param.args[0] as Intent
                    saveLog(intent, context)
                }
            })

        XposedHelpers.findAndHookMethod(CONTEXT_WRAPPER, classLoader,
            START_ACTIVITY, Intent::class.java, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val intent = param.args[0] as Intent
                    saveLog(intent, context)
                }
            })

        XposedHelpers.findAndHookMethod(
            CONTEXT_WRAPPER,
            classLoader, START_ACTIVITY,
            Intent::class.java,
            Bundle::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val intent = param.args[0] as Intent
                    saveLog(intent, context)
                }
            })

        XposedHelpers.findAndHookMethod(
            ACTIVITY,
            classLoader, START_ACTIVITY_FOR_RESULT,
            Intent::class.java,
            Int::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val intent = param.args[0] as Intent
                    saveLog(intent, context)
                }
            })
        XposedHelpers.findAndHookMethod(
            ACTIVITY,
            classLoader, START_ACTIVITY_FOR_RESULT,
            Intent::class.java,
            Int::class.java,
            Bundle::class.java,
            object : XC_MethodHook() {

                override fun beforeHookedMethod(param: MethodHookParam) {
                    val intent = param.args[0] as Intent
                    saveLog(intent, context)
                }
            })
    }

    private fun saveLog(intent: Intent, context: Context) {
        val className = intent.component?.className ?: ""
        val packageName = intent.component?.packageName ?: ""
        val action = intent.action ?: ""
        val data = intent.dataString ?: ""
        val extraList = ArrayList<ExtraBean>()
        val extras = intent.extras
        extras?.keySet()?.forEach {
            val type = when (extras.get(it)) {
                is Boolean -> "--ez"
                is String -> "--es"
                is Int -> "--ei"
                is Long -> "--el"
                is Float -> "--ef"
                else -> "--eu" // maybe error
            }
            extraList.add(ExtraBean(type, it, extras.get(it).toString()))
        }
        val configBean = ConfigBean(packageName, className, action, data, extraList)
        XposedBridge.log(Gson().toJson(configBean))
        val contentValues = contentValuesOf("config" to Gson().toJson(configBean))
        context.contentResolver.insert(uri, contentValues)
    }

}