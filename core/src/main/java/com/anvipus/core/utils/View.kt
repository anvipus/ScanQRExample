package com.anvipus.core.utils

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.anvipus.core.R
import com.anvipus.core.view.DividerItemDecorator
import com.anvipus.core.view.RecyclerMarginize
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.signature.ObjectKey

val View.keyboardIsVisible: Boolean
    get() = ViewCompat.getRootWindowInsets(this)?.isVisible(WindowInsetsCompat.Type.ime()).orFalse()

fun ImageView.loadResource(resId: Int?) {
    Glide.with(context)
        .load(resId)
        .into(this)
}

fun ImageView.toGrayscale() {
    val matrix = ColorMatrix().apply {
        setSaturation(0f)
    }
    colorFilter = ColorMatrixColorFilter(matrix)
}

fun ViewGroup.inflate(res: Int): View {
    return LayoutInflater.from(context).inflate(res, this, false)
}

fun RecyclerView.linear() {
    this.layoutManager = LinearLayoutManager(context)
}

fun RecyclerView.gridHorizontal(span: Int = 1) {
    this.layoutManager = GridLayoutManager(this.context, span, GridLayoutManager.HORIZONTAL, false)
}

fun RecyclerView.grid(span: Int = 2) {
    val gridLayoutManager = GridLayoutManager(this.context, span)
    gridLayoutManager.stackFromEnd = false
    this.layoutManager = gridLayoutManager
}

fun RecyclerView.gridWithNetworkState(hasExtraRow: Boolean) {
    val gridLayoutManager = GridLayoutManager(context, 2)
    gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return if ((position == gridLayoutManager.itemCount - 1 && hasExtraRow)) 2
            else 1
        }
    }
    this.layoutManager = gridLayoutManager
}

fun RecyclerView.stagGrid(span: Int = 2) {
    this.layoutManager = StaggeredGridLayoutManager(span, StaggeredGridLayoutManager.VERTICAL)
}

fun RecyclerView.applyMargin(
    left: Int = 0,
    top: Int = 0,
    right: Int = 0,
    bottom: Int = 0
) {
    this.addItemDecoration(RecyclerMarginize(left, top, right, bottom))
}

fun RecyclerView.divider() {
    addItemDecoration(DividerItemDecorator(context.resDrawable(R.drawable.divider)))
}