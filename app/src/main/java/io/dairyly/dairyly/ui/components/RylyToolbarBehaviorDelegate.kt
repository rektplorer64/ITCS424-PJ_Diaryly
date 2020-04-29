package io.dairyly.dairyly.ui.components

import android.view.View
import androidx.annotation.LayoutRes
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textview.MaterialTextView

interface RylyToolbarBehaviorDelegate<TabType>{

    @get:LayoutRes
    val itemLayoutRes: Int

    /**
     * This is method determine the behavior of the toolbar when the tab layout is scrolled.
     * @param overlineTextView MaterialTextView the TextView that located above the tab layout.
     * @param headerTextView MaterialTextView the TextView that has the largest font size and located below the tab layout
     * @param subtitleTextView MaterialTextView the TextView that located below the headerTextView
     * @param item TabType an object
     */
    fun onTabScrolled(overlineTextView: MaterialTextView,
                      headerTextView: MaterialTextView,
                      subtitleTextView: MaterialTextView,
                      item: TabType)

    /**
     * This method specifies the behavior when a tab is selected.
     * @param overlineTextView MaterialTextView the TextView that located above the tab layout.
     * @param headerTextView MaterialTextView the TextView that has the largest font size and located below the tab layout
     * @param subtitleTextView MaterialTextView the TextView that located below the headerTextView
     * @param item TabType      the data item that will be bind to the target tab.
     */
    fun onTabSelected(overlineTextView: MaterialTextView,
                      headerTextView: MaterialTextView,
                      subtitleTextView: MaterialTextView,
                      item: TabType)

    /**
     * This method specifies the appearance of each individual Tag on its creation time.
     * @param tab Tab           the tab that is the target.
     * @param itemIndex Int           an integer that specifies the index of the tab.
     * @param item TabType      the data item that will be bind to the target tab.
     */
    fun onCreateTab(tab: TabLayout.Tab, itemIndex: Int, item: TabType)

    /**
     * This method determines the condition that could be used to determine always selected item like
     * the current date that needed to be focused.
     * @param item TabType      the data item that will be used to determine whether to treat the item as always emphasized or not.
     * @return Boolean?         a truth value that if true -> always emphasize. Otherwise -> do not. If this returns null, it means that there is no item to emphasize.
     */
    fun isAwaysEmphasized(item: TabType): Boolean?

    fun onOverlineTextClickListener(textView: View, item: TabType)
}