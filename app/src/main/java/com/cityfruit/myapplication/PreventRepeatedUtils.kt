package com.cityfruit.myapplication

import android.view.View
import kotlin.math.abs


fun View.addOnClickListener(clickTime: Long = 300L, l: (View) -> Unit) {
    this.setOnClickListener { v ->
        if (!isFastDoubleClick(clickTime)) {
            l.invoke(v)
        }
    }
}

private var lastClickTime: Long = 0

private fun isFastDoubleClick(diff: Long): Boolean {
    val time = System.currentTimeMillis()
    val timeD = time - lastClickTime
    val isDoubleClick = (abs(timeD) < diff)
    if (isDoubleClick && lastClickTime > 0) {
        return true
    }
    lastClickTime = time
    return false
}
