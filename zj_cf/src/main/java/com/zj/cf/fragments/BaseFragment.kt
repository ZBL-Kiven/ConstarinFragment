@file:Suppress("MemberVisibilityCanBePrivate")

package com.zj.cf.fragments

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.*

/**
 * Created by zjj on 19.05.14.
 */
abstract class BaseFragment : Fragment() {

    private enum class Lifecycle(val value: Int) {
        NONE(-1), CREATE(-1), CREATED(3), START(9), RESTART(9), RESUME(15), PAUSE(8), STOP(4), DESTROY(2)
    }

    open val id: String = UUID.randomUUID().toString()
    internal var managerId: String = ""
    var rootView: View? = null
    var removing = false
    private var curLifeState = Lifecycle.NONE

    val exists: Boolean
        get() = curLifeState != Lifecycle.NONE

    val isCreated: Boolean
        get() = curLifeState.value % 3 == 0

    val isStart: Boolean
        get() = (curLifeState.value > Lifecycle.CREATED.value) && (curLifeState.value % 3 == 0)

    val isResume: Boolean
        get() = (curLifeState.value > Lifecycle.START.value) && (curLifeState.value % 3 == 0)

    val isPause: Boolean
        get() = curLifeState.value % 2 == 0

    val isStop: Boolean
        get() = (curLifeState.value > Lifecycle.PAUSE.value) && (curLifeState.value % 2 == 0)

    val isDestroyed: Boolean
        get() = (curLifeState.value > Lifecycle.STOP.value) && (curLifeState.value % 2 == 0)

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = getView(inflater, container)
        val parent = rootView?.parent as ViewGroup?
        if (parent != null && parent.childCount > 0) {
            parent.removeView(rootView)
        }
        return rootView
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        curLifeState = Lifecycle.CREATE
        onHiddenChanged(isHidden)
    }

    protected abstract fun getView(inflater: LayoutInflater, container: ViewGroup?): View

    final override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (exists) change()
    }

    final override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (exists) change()
    }

    private fun change() {
        if (!parentHide(this)) {
            performResume()
        } else {
            performPause()
            performStop()
        }
    }

    private fun enableRoot(enable: Boolean) {
        rootView?.isEnabled = enable
        rootView?.isFocusable = enable
    }


    private fun performCreate() {
        if (curLifeState == Lifecycle.CREATE) {
            curLifeState = Lifecycle.CREATED
            onCreate()
        }
    }

    private fun performStart() {
        if (curLifeState == Lifecycle.CREATED) {
            curLifeState = Lifecycle.START
            onStarted()
        }
    }

    private fun performReStart() {
        if (curLifeState == Lifecycle.STOP && !parentHide(this)) {
            curLifeState = Lifecycle.RESTART
            onReStart()
            performChildReStart()
        }
    }

    private fun performChildReStart() {
        for (fragment in getFragments()) {
            fragment.performReStart()
        }
    }

    open fun performResume() {
        //避免 Activity resume 所有 Fragment 得到焦点
        if (parentHide(this)) {
            return
        }

        if (curLifeState == Lifecycle.CREATE) {
            performCreate()
            performStart()
        }

        //由于Fragment 没有onReStart()
        if (curLifeState == Lifecycle.STOP) {
            performReStart()
        }

        if (curLifeState == Lifecycle.START || curLifeState == Lifecycle.RESTART || curLifeState == Lifecycle.PAUSE) {
            curLifeState = Lifecycle.RESUME
            onResumed()
            //调节栈顶 后面扩展
            performChildResume()
        }
    }

    open fun performChildResume() {
        getFragments().forEach { it.performResume() }
    }

    private fun performPause() {
        if (curLifeState == Lifecycle.RESUME) {
            curLifeState = Lifecycle.PAUSE
            performChildPause()
            onPaused()
        }
    }

    protected fun performChildPause() {
        getFragments().forEach {
            if (!it.isHidden) {
                it.performPause()
            }
        }
    }

    private fun performStop() {
        if (curLifeState == Lifecycle.PAUSE) {
            curLifeState = Lifecycle.STOP
            performChildStop()
            onStopped()
        }
    }

    private fun performChildStop() {
        getFragments().forEach { it.performStop() }
    }

    private fun getFragments(): MutableList<BaseFragment> {
        val fs = childFragmentManager.fragments
        val bf = mutableListOf<BaseFragment>()
        fs.forEach {
            if (it is BaseFragment) {
                bf.add(it)
            }
        }
        return bf
    }

    internal fun onFragmentDestroy() {
        performPause()
        performStop()
        onDestroyed()
    }

    internal fun onFragmentResumed() {
        if (isStop) {
            onReStart()
        }
        onResumed()
    }

    @CallSuper
    protected open fun onCreate() {
    }

    protected open fun onStarted() {
    }

    @CallSuper
    protected open fun onReStart() {
    }

    @CallSuper
    protected open fun onResumed() {
        enableRoot(true)
    }

    @CallSuper
    protected open fun onPaused() {
        enableRoot(false)
    }

    @CallSuper
    protected open fun onStopped() {
    }

    @CallSuper
    protected open fun onDestroyed() {
        curLifeState = Lifecycle.DESTROY
        rootView = null
    }

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    final override fun onStart() {
        super.onStart()
        performStart()
    }

    final override fun onResume() {
        super.onResume()
        performResume()
    }

    final override fun onPause() {
        super.onPause()
        performPause()
    }

    final override fun onStop() {
        super.onStop()
        performStop()
    }

    final override fun onDestroy() {
        super.onDestroy()
    }

    final override fun onDetach() {
        super.onDetach()
    }

    final override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    final override fun onSaveInstanceState(outState: Bundle) {}

    private fun parentHide(frg: Fragment?): Boolean {
        if (frg == null) {
            return false
        }
        if (frg.isHidden) {
            return true
        }
        frg.parentFragment?.let {
            if (it.isHidden) {
                return true
            }
            return parentHide(it)
        }
        return false
    }

    private fun getChildFragments(): List<Fragment> {
        val childFragments = childFragmentManager.fragments.filterNotNull().filterIsInstance<BaseFragment>()
        return if (childFragments.isNullOrEmpty()) arrayListOf() else childFragments
    }

    protected fun <T : View> find(id: Int): T? {
        return rootView?.findViewById(id)
    }

}