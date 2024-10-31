package com.anvipus.core.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anvipus.core.utils.*

class RecyclerMarginize(
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0
): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        if(parent.getChildAdapterPosition(view) == 0){
            if(left != 0)outRect.left = parent.context.dp(left).toInt()
            if(top != 0)outRect.top = parent.context.dp(top).toInt()
            if(right != 0)outRect.right = parent.context.dp(right).toInt()
            if(bottom != 0)outRect.bottom = parent.context.dp(bottom).toInt()
        }
    }

}