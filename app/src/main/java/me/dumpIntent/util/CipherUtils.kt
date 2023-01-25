package me.dumpIntent.util


import android.text.TextUtils
import android.util.Base64
import androidx.annotation.Keep
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Keep
object CipherUtils {

    private const val CipherMode = "AES/CFB/NoPadding" //Use CFB to encrypt, IV is need
    private const val KEY = "absintheeeeeeeeeeeeeeeeeeeeeeeee"

    private fun generateKey(): SecretKeySpec {
        val data: ByteArray = KEY.toByteArray(StandardCharsets.UTF_8)
        return SecretKeySpec(data, "AES")
    }

    fun encrypt(data: String): String? {
        return if (TextUtils.isEmpty(data)) {
            null
        } else try {
            val cipher = Cipher.getInstance(CipherMode)
            cipher.init(
                Cipher.ENCRYPT_MODE,
                generateKey(),
                IvParameterSpec(ByteArray(cipher.blockSize))
            )
            val encrypted = cipher.doFinal(data.toByteArray())
            Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }
}
