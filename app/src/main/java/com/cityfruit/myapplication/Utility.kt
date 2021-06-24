package com.cityfruit.myapplication

import android.os.Bundle
import com.zj.cf.fragments.BaseFragment
import com.zj.cf.managers.FragmentHelper

fun getBundle(obj: String): Bundle {
    return Bundle().apply {
        putString("a", obj)
    }
}

fun getTop(manager: FragmentHelper<*>): BaseFragment? {
    return manager.getTopOfStack()
}

fun printBundle(b: Bundle?, isBack: Boolean) {
}