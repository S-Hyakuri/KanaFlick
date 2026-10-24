package com.hyakuri.kanaflick.ui.startup

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.FrameLayout

/** Layout after the platform has positioned its children; do not mix icon-local and root coordinates. */
internal object SplashWordmarkLayout {
    fun attach(host: ViewGroup, icon: View, artwork: Drawable): ImageView {
        val label = ImageView(host.context).apply {
            setImageDrawable(artwork)
            scaleType = ImageView.ScaleType.FIT_CENTER
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        host.addView(label, FrameLayout.LayoutParams(0, 0))
        val iconRect = Rect()
        fun position() {
            if (host.width == 0 || icon.width == 0) return
            val width = (icon.width * LogoMotion.WORDMARK_WIDTH_RATIO).toInt()
                .coerceAtMost(host.width - host.paddingLeft - host.paddingRight)
                .coerceAtLeast(1)
            val height = (width.toFloat() * artwork.intrinsicHeight / artwork.intrinsicWidth).toInt().coerceAtLeast(1)
            if (label.layoutParams.width != width || label.layoutParams.height != height) {
                label.layoutParams = label.layoutParams.apply { this.width = width; this.height = height }
            }
            iconRect.set(0, 0, icon.width, icon.height)
            host.offsetDescendantRectToMyCoords(icon, iconRect)
            // x/y are assigned AFTER layout: compensate for root padding and the child's real left/top.
            val centeredX = (host.width - width) / 2f
            label.x = (centeredX + icon.width * LogoMotion.WORDMARK_OFFSET_X_RATIO)
                .coerceIn(host.paddingLeft.toFloat(),
                    (host.width - host.paddingRight - width).toFloat().coerceAtLeast(host.paddingLeft.toFloat()))
            label.y = iconRect.exactCenterY() + iconRect.height() * LogoMotion.WORDMARK_TOP_RATIO
        }
        val listener = ViewTreeObserver.OnGlobalLayoutListener { position() }
        host.viewTreeObserver.addOnGlobalLayoutListener(listener)
        label.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> position() }
        host.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) { position() }
            override fun onViewDetachedFromWindow(v: View) {
                if (host.viewTreeObserver.isAlive) host.viewTreeObserver.removeOnGlobalLayoutListener(listener)
                host.removeOnAttachStateChangeListener(this)
            }
        })
        position()
        return label
    }
}
