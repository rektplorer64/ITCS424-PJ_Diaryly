package io.dairyly.dairyly.utils

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.Px
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop


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