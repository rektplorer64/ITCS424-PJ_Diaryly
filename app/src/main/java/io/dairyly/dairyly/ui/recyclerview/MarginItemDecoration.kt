package io.dairyly.dairyly.ui.recyclerview

import android.content.Context
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration


/**
 * Adds 8dp padding to the top of the first and the bottom of the last item in the list,
 * as specified in https://www.google.com/design/spec/components/lists.html#lists-specs
 */
class MarginItemDecoration(context: Context, val orientation: Int) :
        ItemDecoration() {

    private val mPadding: Int

    override fun getItemOffsets(outRect: Rect, view: View,
                                parent: RecyclerView,
                                state: RecyclerView.State) {
        val itemPosition = parent.getChildAdapterPosition(view)
        if(itemPosition == RecyclerView.NO_POSITION) {
            return
        }
        if(orientation == RecyclerView.VERTICAL) {
            if(itemPosition == 0) {
                outRect.top = mPadding
            }
            val adapter = parent.adapter
            if(adapter != null && itemPosition == adapter.itemCount - 1) {
                outRect.bottom = mPadding
            }
        } else {
            // if(itemPosition == 0) {
            //     outRect.left = mPadding
            // }
            val adapter = parent.adapter
            if(adapter != null) {
                outRect.right = mPadding
            }
        }
    }

    companion object {
        private const val PADDING_IN_DIPS = 8
    }

    init {
        val metrics = context.resources.displayMetrics
        mPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                             PADDING_IN_DIPS.toFloat(),
                                             metrics).toInt()
    }
}