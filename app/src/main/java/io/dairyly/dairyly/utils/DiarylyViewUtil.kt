package io.dairyly.dairyly.utils

import android.content.res.ColorStateList
import android.view.LayoutInflater
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import io.dairyly.dairyly.R
import io.dairyly.dairyly.models.data.DiaryEntry
import io.dairyly.dairyly.models.data.getForegroundAndAccentColor

fun ChipGroup.populateTags(
        item: DiaryEntry) {

    val color = item.getForegroundAndAccentColor(this.context)

    // val color = calculateForegroundColorToPair(item.color)
    if(this.childCount > 0) {
        this.removeAllViews()
    }
    for(element in item.tags) {
        val chip = (LayoutInflater
                .from(this.context)
                .inflate(R.layout.chip_tag, this, false) as Chip)

        chip.apply {
            // setChipDrawable(ChipDrawable.createFromAttributes(context, null, 0, R.style.Widget_MaterialComponents_Chip_Action))
            setTextColor(color.first)
            chipBackgroundColor = ColorStateList.valueOf(color.second)

            text = element.title
            invalidate()
        }

        this.addView(chip)
    }
}