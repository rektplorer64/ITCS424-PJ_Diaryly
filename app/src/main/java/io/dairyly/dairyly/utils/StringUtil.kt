package io.dairyly.dairyly.utils

import android.util.Patterns
import java.security.MessageDigest

fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this!!).matches()

fun String.toSHA256(): String{
    val md = MessageDigest.getInstance("SHA-256")
    md.update(this.toByteArray())
    val digest = md.digest()
    return digest.contentToString()
}