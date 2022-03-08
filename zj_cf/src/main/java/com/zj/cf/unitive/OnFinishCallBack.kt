package com.zj.cf.unitive

import com.zj.cf.managers.FragmentHelper

abstract class OnFinishCallBack {

    open fun finished(id: String, clearWhenEmptyStack: Boolean) {}

    open fun errorWithStackEmpty() {}

    open fun errorWithNotCurrent() {}

    open fun setToPrevious(previousManagerId: FragmentHelper<*>?) {}

    open fun finishKeepWithTop(id: String) {}

}