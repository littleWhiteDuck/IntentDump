package me.dumpIntent

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import me.dumpIntent.adapter.ConfigAdapter
import me.dumpIntent.bean.*
import me.dumpIntent.databinding.ActivityMainBinding
import me.dumpIntent.util.AppUtils
import me.dumpIntent.util.CipherUtils
import me.dumpIntent.util.ToolUtils

class MainActivity : AppCompatActivity() {
    private val uri = Uri.parse("content://me.dumpIntent.provider/configs")
    private lateinit var binding: ActivityMainBinding

    private val mAdapter by lazy { ConfigAdapter { onItemClick(it) } }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val moduleLiveTip = if (isModuleLive()) "模块已激活" else "模块未激活"
        Snackbar.make(binding.rev, moduleLiveTip, Snackbar.LENGTH_SHORT).show()
        initView()
        fetchData()
    }

    private fun initView() {
        binding.apply {
            rev.apply {
                adapter = mAdapter
                layoutManager = LinearLayoutManager(this@MainActivity)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onItemClick(mainItem: MainItem) {
        val configBean = mainItem.configBean
        var extraStr = ""
        if (configBean.extras.isNotEmpty()) {
            configBean.extras.forEach {
                extraStr += "key：${it.key}, value：${it.value}\n"
            }
            extraStr = extraStr.substring(0, extraStr.length - 1)
        }
        val message =
            "应用名: ${mainItem.appName}\n包名: ${configBean.packageName}\n类名: ${configBean.className}\n" + "Action：${configBean.action}\nData: ${configBean.data}\nExtras: $extraStr"
        MaterialAlertDialogBuilder(this).setTitle("详情").setMessage(message)
            .setPositiveButton("复制为Anywhere-配置") { dialog, _ ->
                val extras: Any = configBean.extras.ifEmpty { "" }
                val bean2 = Bean2(configBean.data, configBean.action, "", extras)
                val bean1 = Bean1(
                    app_name = mainItem.appName,
                    id = (System.currentTimeMillis()).toString(),
                    param_1 = configBean.packageName,
                    param_2 = configBean.className,
                    param_3 = Gson().toJson(bean2),
                    time_stamp = (System.currentTimeMillis()).toString()
                )
                CipherUtils.encrypt(Gson().toJson(bean1))?.let { ToolUtils.toClip(this, it) }
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun fetchData() {
        // 未考虑数据量大和交互
        val dataList = ArrayList<MainItem>()
        contentResolver.query(uri, null, null, null, "ID DESC")?.apply {
            while (moveToNext()) {
                val config = getString(getColumnIndex("config"))
                val id = getInt(getColumnIndex("id"))
                val configBean = Gson().fromJson(config, ConfigBean::class.java)
                dataList.add(
                    MainItem(
                        id,
                        AppUtils.getAppName(this@MainActivity, configBean.packageName),
                        AppUtils.getIcon(this@MainActivity, configBean.packageName),
                        configBean
                    )
                )
            }
            close()
        }
        mAdapter.submitList(dataList)
        if (dataList.size >= 50) Snackbar.make(binding.rev, "请及时清理数据，以免加载数据过慢", Snackbar.LENGTH_SHORT).show()
    }

    @Keep
    private fun isModuleLive() = false

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> {
                contentResolver.delete(uri, null, null)
                fetchData()
            }
            R.id.refresh -> fetchData()
            R.id.github -> {
                val intent = Intent()
                intent.data = Uri.parse("https://github.com/littleWhiteDuck/IntentDump")
                startActivity(intent)
            }
            R.id.about -> showAboutMe()
        }
        return true
    }

    private fun showAboutMe() {
        MaterialAlertDialogBuilder(this).setTitle("关于").setMessage(
            """
            作者：littleWhiteDuck
            应用介绍：本应用仅仅Hook了常用的几个启动Activity时的方法,可能不能面向所有的Activity
            使用本软件需要Xposed；
            本人邮箱：484303285@qq.com；
            说明：为了可以一键复制为Anywhere-配置，使用了Absinthe大佬的Anywhere-的加密密钥和相关代码
        """.trimIndent()
        ).setPositiveButton("确认", null).setNegativeButton("取消", null).show()
    }


    override fun onRestart() {
        super.onRestart()
        fetchData()
    }
}