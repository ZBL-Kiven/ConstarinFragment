package com.cityfruit.myapplication

import android.content.Context
import android.widget.Toast

object ToastUtils {

    private var toast: Toast? = null

    fun show(context: Context?, txt: String) {
        context?.applicationContext?.let {
            if (toast == null) toast = Toast.makeText(it, txt, Toast.LENGTH_SHORT)
            else toast?.setText(txt)
            toast?.show()
        }
    }
}
