package io.dairyly.dairyly.utils

import android.graphics.Color
import androidx.annotation.ColorInt

fun Int.toHexString(): String{
    return java.lang.String.format("#%06X", 0xFFFFFF and this)
}

@ColorInt
fun calculateForegroundColor(@ColorInt backgroundColorInt: Int): Int {
    // Counting the perceptive luminance - human eye favors green color...
    val a = 1 - (0.299 * Color.red(
            backgroundColorInt) + 0.587 * Color.green(
            backgroundColorInt) + 0.114 * Color.blue(backgroundColorInt)) / 255
    val d: Int
    d = if(a < 0.5) {
        0 // bright colors - black font
    } else {
        255 // dark colors - white font
    }
    return Color.rgb(d, d, d)
}

/**
 * Calculate the Foreground color that has the greatest readability for the given background
 * @param backgroundColorInt Int color integer of the background
 * @return Pair<Int, Int> a pair of newly calculated foreground and the parameter background
 */
fun calculateForegroundColorToPair(@ColorInt backgroundColorInt: Int): Pair<Int, Int>{
    return Pair(calculateForegroundColor(backgroundColorInt), backgroundColorInt)
}