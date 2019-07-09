package com.cityfruit.myapplication.base_fg.fragments

import android.os.Bundle
import com.cityfruit.myapplication.base_fg.unitive.ProxyManager

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
        if (!removing) {
            removing = true
            proxy?.finish {
                rootView = null
            }
        }
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
