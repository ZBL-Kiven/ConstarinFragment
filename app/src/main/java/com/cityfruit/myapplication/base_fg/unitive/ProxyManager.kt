package com.cityfruit.myapplication.base_fg.unitive

import android.os.Bundle
import com.cityfruit.myapplication.base_fg.annotations.parser.ConstrainFragmentAnnotationParser
import com.cityfruit.myapplication.base_fg.fragments.BaseFragment
import com.cityfruit.myapplication.base_fg.fragments.ConstrainFragment
import com.cityfruit.myapplication.base_fg.getSimpleId
import com.cityfruit.myapplication.base_fg.managers.ConstrainFragmentManager

data class ProxyManager<T : ConstrainFragment>(internal val mFragmentClass: Class<T>, val id: String, val backMode: Int, val launchMode: Int, val isHome: Boolean, val bundle: Bundle?) {

    private var fragmentManager: ConstrainFragmentManager? = null

    private var resultBundle: Bundle? = null

    fun getManagerId(): String? {
        return fragmentManager?.managerId
    }

    fun getStackTop(): BaseFragment? {
        return fragmentManager?.getTopOfStack()
    }

    fun getResultBundle(): Bundle? {
        return resultBundle
    }

    fun setResult(b: Bundle?) {
        resultBundle = b
    }

    internal fun setFragmentManager(fragmentManager: ConstrainFragmentManager?): ConstrainFragmentManager {
        this.fragmentManager = fragmentManager
        return this.fragmentManager ?: throw NullPointerException("your fragment manager can not use form null!")
    }

    fun finish(obs: ((isEmptyStack: Boolean) -> Unit)? = null) {
        fragmentManager?.finishFragment(id, obs)
    }

    fun clearStack() {
        fragmentManager?.clearStack()
    }

    fun <CLS : ConstrainFragment> start(fragmentCls: Class<CLS>, bundle: Bundle?) {
        val frg = ConstrainFragmentAnnotationParser.parseAnnotations(fragmentCls, bundle, fragmentManager)
        try {
            frg.setFragmentManager(fragmentManager).startFragment(frg)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(IllegalAccessException::class)
    fun <F : ConstrainFragment> upDatePreviousFragment(previousFragment: Class<F>, bundle: Bundle?) {
        fragmentManager?.let {
            val proxy = ConstrainFragmentAnnotationParser.parseAnnotations(previousFragment, bundle, it)
            val curId = getSimpleId(it.getCurrentItemId()).first
            val proxyId = getSimpleId(proxy.id).first
            val thisId = getSimpleId(this.id).first
            if (thisId != curId) throw IllegalAccessException("back stack only access in stack top!")
            if (proxyId == thisId) throw IllegalAccessException("back stack can not use by self class!")
            if (isHome || it.getCurrentStackSize() <= 1) throw IllegalAccessException("back stack can not use in home statement!")
            it.setBackStack(proxy)
        }
    }
}