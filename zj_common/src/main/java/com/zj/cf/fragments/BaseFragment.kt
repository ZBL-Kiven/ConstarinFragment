@file:Suppress("MemberVisibilityCanBePrivate")

package com.zj.cf.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zj.cf.log
import java.util.*

/**
 * Created by zjj on 19.05.14.
 */
abstract class BaseFragment : Fragment() {

    enum class Lifecycle {
        NONE, CREATED, RESUME, PAUSE, DESTROY
    }

    open val id: String = UUID.randomUUID().toString()
    internal var managerId: String = ""
    var rootView: View? = null
    var removing = false
    private var curLifeState = Lifecycle.NONE

    val exists: Boolean
        get() = curLifeState != Lifecycle.NONE

    val isResume: Boolean
        get() = curLifeState == Lifecycle.RESUME

    val isStop: Boolean
        get() = curLifeState == Lifecycle.PAUSE

    val isDestroyed: Boolean
        get() = curLifeState == Lifecycle.DESTROY

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        curLifeState = Lifecycle.CREATED
        rootView = getView(inflater, container)
        val parent = rootView?.parent as ViewGroup?
        if (parent != null && parent.childCount > 0) {
            parent.removeView(rootView)
        }
        return rootView
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        log("onFragmentStateChange --- init  $id")
        initView()
        initData()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        onShownStateChange(hidden)
    }

    internal fun onFragmentDestroy() {
        if (!isStop) {
            onShownStateChange(true)
        }
        onDestroyed()
        curLifeState = Lifecycle.DESTROY
        rootView = null
        log("onFragmentStateChange --- destroy  $id")
    }

    private fun onShownStateChange(isHidden: Boolean) {
        val hiddenState = if (isHidden) Lifecycle.PAUSE else Lifecycle.RESUME
        if (curLifeState != hiddenState && curLifeState != Lifecycle.DESTROY) {
            curLifeState = hiddenState
            if (isHidden) {
                log("onFragmentStateChange --- onStopped  $id")
                onStopped()
            } else {
                log("onFragmentStateChange --- onResumed  $id")
                onResumed()
            }
        }
    }

    protected abstract fun getView(inflater: LayoutInflater, container: ViewGroup?): View

    protected abstract fun initView()

    protected abstract fun initData()

    protected fun <T : View> find(id: Int): T? {
        return rootView?.findViewById(id)
    }

    protected open fun onResumed() {}
    protected open fun onStopped() {}
    protected open fun onDestroyed() {}

    /**
     * use the custom lifecycle and disable the region func
     * */

    final override fun onResume() {
        onShownStateChange(false)
        super.onResume()
    }

    final override fun onPause() {
        onShownStateChange(true)
        super.onPause()
    }

    final override fun onStop() {
        onShownStateChange(true)
        super.onStop()
    }

    final override fun onStart() {
        super.onStart()
    }

    final override fun onDestroy() {
        super.onDestroy()
    }

    //    final override fun onDestroyView() {
    //        super.onDestroyView()
    //    }

    final override fun onDetach() {
        super.onDetach()
    }

    final override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    final override fun onSaveInstanceState(outState: Bundle) {}
}