package com.cityfruit.myapplication

import android.content.Context
import android.widget.Toast

object ToastUtils {

    fun show(context: Context?, txt: String) {
        context?.applicationContext?.let {
            Toast.makeText(it, txt, Toast.LENGTH_SHORT).show()
        }
    }
}
