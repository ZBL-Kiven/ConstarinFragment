package com.zj.cf.fragments

import android.os.Bundle
import android.view.ViewGroup
import com.zj.cf.log
import com.zj.cf.unitive.ProxyManager

@Suppress("unused")
abstract class ConstrainFragment : BaseFragment() {

    /* only validate in ConstrainFragmentManager **/
    private var proxy: ProxyManager<*>? = null

    override val id: String
        get() {
            return proxy?.id ?: super.id
        }

    open fun onPostValue(bundle: Bundle?) {}

    open fun onFragmentResult(bundle: Bundle?) {}

    open fun finish() {
        log(" ----   finish ----- ")
        if (onBack() && !removing) {
            removing = true
            log(" ----   removing ----- ")
            proxy?.finish { isEmptyStack, clearWhenEmptyStack ->
                log(" ----   finish proxy empty: $isEmptyStack    cleared: $clearWhenEmptyStack----- ")
                if (!clearWhenEmptyStack) removing = false
                rootView?.apply {
                    if (isEmptyStack && this is ViewGroup) {
                        log(" ----   removeAllViews ----- ")
                        removeAllViews()
                    }
                }
                rootView = null
            }
        }
        log(" ----   end ----- ")
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
