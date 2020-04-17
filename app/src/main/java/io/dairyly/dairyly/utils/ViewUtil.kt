package io.dairyly.dairyly.utils

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.Px
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import com.google.android.material.chip.Chip
import io.dairyly.dairyly.R


val Int.toDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.toPx: Int
    @Px
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun View.setMargins(@Px l: Int = marginLeft, @Px t: Int = marginTop, @Px r: Int = marginRight, @Px b: Int = marginBottom) {
    if(layoutParams is MarginLayoutParams) {
        val p = layoutParams as MarginLayoutParams
        p.setMargins(l, t, r, b)
        requestLayout()
    }
}

fun Chip.changeBackgroundColor(colorInt: Int = Color.BLACK) {
    val colorPair = calculateForegroundColorToPair(colorInt)
    val foreground = if(colorPair.first == Color.BLACK){
        context!!.getColor(R.color.blackAlpha60)
    }else{
        colorPair.first
    }
    setTextColor(foreground)
    closeIconTint = ColorStateList.valueOf(foreground)
    chipBackgroundColor = ColorStateList.valueOf(colorPair.second)
}
