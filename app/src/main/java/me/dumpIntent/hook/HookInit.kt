package me.dumpIntent.hook


import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION_CODES
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
import kotlin.properties.Delegates

private const val ACTIVITY = "android.app.Activity"
private const val CONTEXT_WRAPPER = "android.content.ContextWrapper"
private const val START_ACTIVITY = "startActivity"
private const val START_ACTIVITY_FOR_RESULT = "startActivityForResult"
private val uri = Uri.parse("content://me.dumpIntent.provider/configs")

class HookInit : IXposedHookLoadPackage {
    private var targetSdkVersion by Delegates.notNull<Int>()
    private lateinit var appContext: Context
    private lateinit var appClassLoader: ClassLoader
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == BuildConfig.APPLICATION_ID) {
            selfHook(lpparam.classLoader)
        } else {
            if (::appContext.isInitialized) return
            targetSdkVersion = lpparam.appInfo.targetSdkVersion
            startHook()
        }
    }

    private fun selfHook(classLoader: ClassLoader) {
        XposedHelpers.findAndHookMethod("me.dumpIntent.MainActivity",
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
        XposedHelpers.findAndHookMethod(Application::class.java,
            "attach",
            Context::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    super.afterHookedMethod(param)
                    appContext = param!!.args[0] as Context
                    appClassLoader = appContext.classLoader
                    intentHook()
                }
            })
    }

    private fun intentHook() {
        XposedHelpers.findAndHookMethod(ACTIVITY,
            appClassLoader,
            START_ACTIVITY,
            Intent::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val intent = param.args[0] as Intent
                    saveLog(intent)
                }
            })

        XposedHelpers.findAndHookMethod(CONTEXT_WRAPPER,
            appClassLoader,
            START_ACTIVITY,
            Intent::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val intent = param.args[0] as Intent
                    saveLog(intent)
                }
            })

        XposedHelpers.findAndHookMethod(CONTEXT_WRAPPER,
            appClassLoader,
            START_ACTIVITY,
            Intent::class.java,
            Bundle::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val intent = param.args[0] as Intent
                    saveLog(intent)
                }
            })

        XposedHelpers.findAndHookMethod(ACTIVITY,
            appClassLoader,
            START_ACTIVITY_FOR_RESULT,
            Intent::class.java,
            Int::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val intent = param.args[0] as Intent
                    saveLog(intent)
                }
            })
        XposedHelpers.findAndHookMethod(ACTIVITY,
            appClassLoader,
            START_ACTIVITY_FOR_RESULT,
            Intent::class.java,
            Int::class.java,
            Bundle::class.java,
            object : XC_MethodHook() {

                override fun beforeHookedMethod(param: MethodHookParam) {
                    val intent = param.args[0] as Intent
                    saveLog(intent)
                }
            })
    }

    private fun saveLog(intent: Intent) {
        val className = intent.component?.className ?: ""
        val packageName = intent.component?.packageName ?: ""
        val action = intent.action ?: ""
        val data = intent.dataString ?: ""
        val extraList = ArrayList<ExtraBean>()
        val extras = intent.extras
        extras?.keySet()?.forEach {
            val type = when (extras.get(it)) {
                is Boolean -> "--ez"
                is Int -> "--ei"
                is Long -> "--el"
                is Float -> "--ef"
                is Uri -> "--eu"
                else ->  "--es"// maybe error
            }
            extraList.add(ExtraBean(type, it, extras.get(it).toString()))
        }
        val configBean = ConfigBean(packageName, className, action, data, extraList)
        if (targetSdkVersion > VERSION_CODES.Q) {
            //使用广播发送
            val sendIntent = Intent("me.dumpIntent.broadcast.ACTION_RECEIVE_RECORD").also {
                it.setPackage(BuildConfig.APPLICATION_ID)
                it.putExtra("record_intent_hook", Gson().toJson(configBean))
            }
            appContext.sendBroadcast(sendIntent, null)
        } else {
            try {
                val contentValues = contentValuesOf("config" to Gson().toJson(configBean))
                appContext.contentResolver.insert(uri, contentValues)
            } catch (_: Throwable) {

            }
        }
        XposedBridge.log(Gson().toJson(configBean))
    }

}