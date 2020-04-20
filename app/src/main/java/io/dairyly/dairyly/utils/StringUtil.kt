package io.dairyly.dairyly.utils

import android.util.Patterns
import java.security.MessageDigest
import java.util.regex.Pattern

fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this!!)
        .matches()

fun String.toSHA256(): String {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(this.toByteArray())
    val digest = md.digest()
    return digest.contentToString()
}

fun CharSequence?.isValidUsername(): Boolean {
    val patterns = Pattern.compile("^(?=.{8,20}\$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])\$")
    val a = patterns.matcher(this ?: "")
    return a.find()
}

