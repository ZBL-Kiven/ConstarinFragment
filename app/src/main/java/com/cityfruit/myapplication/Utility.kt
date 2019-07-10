package com.cityfruit.myapplication

import android.os.Bundle
import android.util.Log
import com.cityfruit.myapplication.base_fg.fragments.BaseFragment
import com.cityfruit.myapplication.base_fg.log
import com.cityfruit.myapplication.base_fg.managers.FragmentHelper

fun getBundle(obj: String): Bundle {
    return Bundle().apply {
        putString("a", obj)
    }
}

fun getTop(manager: FragmentHelper<*>): BaseFragment? {
    val top = manager.getTopOfStack()
    log("the manager was : ${manager.managerId}, and the top was : ${top?.id} ,top manager was : ${top?.managerId}")
    return top
}

fun printBundle(b: Bundle?, isBack: Boolean) {
    Log.e("---- bundle got :", "${if (isBack) "onFragmentResults" else "postValue"} =  ${b?.getString("a") ?: "null"}")
}