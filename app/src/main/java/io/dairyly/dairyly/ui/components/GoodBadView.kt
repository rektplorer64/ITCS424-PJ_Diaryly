package io.dairyly.dairyly.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import com.mikhaellopez.circularimageview.CircularImageView
import io.dairyly.dairyly.R
import io.dairyly.dairyly.utils.calculateForegroundColorToPair

class GoodBadView(context: Context, attributes: AttributeSet) : ConstraintLayout(context, attributes) {

    private val textView: TextView
    private val iconView: CircularImageView

    @ColorInt
    private val defaultColor: Int

    @ColorInt
    private var overriddenBackgroundColor: Int? = null

    init {
        val attrs = context.obtainStyledAttributes(attributes, R.styleable.GoodBadView, 0, 0)

        val typedVal = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorPrimary, typedVal, true)

        defaultColor = attrs.getColor(R.styleable.GoodBadView_iconBackgroundTint, typedVal.data)
        attrs.recycle()

        val root = inflate(context, R.layout.view_good_bad, this)

        textView = root.findViewById(R.id.sideIconText)
        iconView = root.findViewById(R.id.sideIcon)
    }

    fun setNumber(int: Int){
        val string = if(int > 0){
            "+ $int"
        }else{
            int.toString()
        }
        textView.text = string
    }

    private fun getNumber(): Int{
        val string = textView.text.toString().replace("+", "").trim()
        return string.toInt()
    }

    fun setIconBackgroundColor(@ColorInt colorInt: Int){
        overriddenBackgroundColor = colorInt
        redrawIcon()
    }

    private fun redrawIcon(){
        val number = getNumber()
        val resourceId = when {
            number > 0 -> R.drawable.ic_thumb_up_black_24dp
            number < 0 -> R.drawable.ic_thumb_down_black_24dp
            else       -> R.drawable.ic_circle_off_black_24dp
        }

        val colorInt = if(overriddenBackgroundColor != null){
            overriddenBackgroundColor
        }else{
            defaultColor
        }

        val color = calculateForegroundColorToPair(colorInt!!)

        var foregroundColor = color.first
        if(color.first == Color.BLACK){
            foregroundColor = context.getColor(R.color.blackAlpha80)
        }

        iconView.apply {
            circleColor = color.second
            setImageResource(resourceId)
            imageTintList = ColorStateList.valueOf(foregroundColor)
        }
        invalidate()
        requestLayout()
    }
}