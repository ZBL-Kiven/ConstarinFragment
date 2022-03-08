package com.zj.cf.fragments

import android.os.Bundle
import android.view.ViewGroup
import com.zj.cf.managers.FragmentHelper
import com.zj.cf.unitive.OnFinishCallBack
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

    open fun finish(onFinished: ((success: Boolean, inTopOfStack: Boolean) -> Unit)? = null) {
        if (onBack() && !removing) {
            removing = true
            proxy?.finish(object : OnFinishCallBack() {

                override fun finishKeepWithTop(id: String) {
                    removing = false
                    onFinished?.invoke(false, true)
                }

                override fun finished(id: String, clearWhenEmptyStack: Boolean) {
                    (rootView as? ViewGroup)?.removeAllViews()
                    rootView = null
                    if (!clearWhenEmptyStack) onFinished?.invoke(true, clearWhenEmptyStack)
                }

                override fun errorWithStackEmpty() {
                    removing = false
                    onFinished?.invoke(true, true)
                }

                override fun setToPrevious(previousManagerId: FragmentHelper<*>?) {
                    /**The current stack position rolls back to the previous stack,
                     * since it is called by this ConstrainFragment and therefore should not affect its corresponding Activity.*/
                }

                override fun errorWithNotCurrent() {
                    removing = false
                }
            })
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

    protected fun clearStack(keepCurrent: Boolean = false) {
        this.proxy?.clearStack(keepCurrent)
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
