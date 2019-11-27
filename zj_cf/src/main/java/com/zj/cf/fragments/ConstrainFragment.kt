package com.zj.cf.fragments

import android.os.Bundle
import android.view.ViewGroup
import com.zj.cf.unitive.ProxyManager

@Suppress("unused")
abstract class ConstrainFragment : BaseFragment() {

    /* only validate in ConstrainFragmentManager **/
    private var proxy: ProxyManager<*>? = null

    final override val fId: String
        get() {
            return proxy?.id ?: super.fId
        }

    open fun onPostValue(bundle: Bundle?) {}

    open fun onFragmentResult(bundle: Bundle?) {}

    open fun finish() {
        if (onBack() && !removing) {
            removing = true
            proxy?.finish { isEmptyStack, clearWhenEmptyStack ->
                if (!clearWhenEmptyStack) removing = false
                rootView?.apply {
                    if (isEmptyStack && this is ViewGroup) {
                        removeAllViews()
                    }
                }
                rootView = null
            }
        }
    }

    /**
     * it may interrupt or invalidate the finish called
     * */
    open fun onBack(): Boolean {
        return true
    }

    protected fun getStackTop(): BaseFragment? {
        return this.proxy?.getStackTop()
    }

    internal fun <T : ConstrainFragment> setProxy(proxy: ProxyManager<T>) {
        this.proxy = proxy
    }

    protected fun clearStack() {
        this.proxy?.clearStack()
    }

    protected fun setResult(bundle: Bundle?) {
        this.proxy?.setResult(bundle)
    }

    protected fun <T : ConstrainFragment> setPrevious(previousFragment: Class<T>, bundle: Bundle? = null) {
        this.proxy?.upDatePreviousFragment(previousFragment, bundle)
    }

    protected fun <T : ConstrainFragment> startFragment(fragmentCls: Class<T>, bundle: Bundle? = null) {
        proxy?.start(fragmentCls, bundle)
    }
}
