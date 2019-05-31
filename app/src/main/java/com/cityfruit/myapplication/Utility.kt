package com.cityfruit.myapplication

import android.os.Bundle
import android.util.Log

fun getBundle(obj: String): Bundle {
    return Bundle().apply {
        putString("a", obj)
    }
}

fun printBundle(b: Bundle?, isBack: Boolean) {
    Log.e("---- bundle got :", "${if (isBack) "onFragmentResults" else "postValue"} =  ${b?.getString("a") ?: "null"}")
}