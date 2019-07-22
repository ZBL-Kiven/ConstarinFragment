package com.cityfruit.myapplication

import android.os.Bundle
import android.util.Log
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
    Log.e("---- bundle got :", "${if (isBack) "onFragmentResults" else "postValue"} =  ${b?.getString("a") ?: "null"}")
}