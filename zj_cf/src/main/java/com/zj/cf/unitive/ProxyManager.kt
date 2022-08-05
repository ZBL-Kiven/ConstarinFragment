package com.zj.cf.unitive

import android.os.Bundle
import com.zj.cf.FMStore.getSimpleId
import com.zj.cf.annotations.parser.ConstrainFragmentAnnotationParser
import com.zj.cf.fragments.BaseFragment
import com.zj.cf.fragments.ConstrainFragment
import com.zj.cf.managers.ConstrainFragmentManager

internal data class ProxyManager<T : ConstrainFragment>(internal val mFragmentClass: Class<T>, val id: String, val backMode: Int, val launchMode: Int, val isHome: Boolean, val bundle: Bundle?) {

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

    fun finish(obs: OnFinishCallBack? = null, force: Boolean = false) {
        fragmentManager?.finishFragment(id, obs, force)
    }

    fun clearStack(keepCurrent: Boolean) {
        fragmentManager?.clearStack(keepCurrent)
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