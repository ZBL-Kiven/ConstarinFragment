@file:Suppress("MemberVisibilityCanBePrivate")

package com.zj.cf.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleRegistry
import com.zj.cf.FMStore
import com.zj.cf.lifecycleCallback
import com.zj.cf.unitive.Lifecycle
import java.lang.IllegalArgumentException
import java.util.*

/**
 * Created by zjj on 19.05.14.
 */
abstract class BaseFragment : Fragment() {

    open val fId: String = UUID.randomUUID().toString()
    open var useDefaultLifecycle = false

    private val lifecycleRegistry by lazy { LifecycleRegistry(this) }

    internal var managerId: String = ""
    var rootView: View? = null
    var removing = false
    private var onDestroyListener: ((frag: BaseFragment) -> Unit)? = null
    internal fun setOnDestroyCallback(onDestroyListener: ((BaseFragment) -> Unit)?) {
        this.onDestroyListener = onDestroyListener
    }

    private var curLifeState = Lifecycle.NONE
        set(value) {
            field = value
            kotlin.runCatching {
                when (value) {
                    Lifecycle.CREATE, Lifecycle.STOP -> lifecycleRegistry.currentState = androidx.lifecycle.Lifecycle.State.CREATED
                    Lifecycle.START, Lifecycle.RESTART, Lifecycle.PAUSE -> lifecycleRegistry.currentState = androidx.lifecycle.Lifecycle.State.STARTED
                    Lifecycle.NONE -> lifecycleRegistry.currentState = androidx.lifecycle.Lifecycle.State.INITIALIZED
                    Lifecycle.RESUME -> lifecycleRegistry.currentState = androidx.lifecycle.Lifecycle.State.RESUMED
                    Lifecycle.DESTROY -> lifecycleRegistry.currentState = androidx.lifecycle.Lifecycle.State.DESTROYED
                    else -> {
                    }
                }
                lifecycleRegistry.handleLifecycleEvent(when (value) {
                    Lifecycle.NONE -> androidx.lifecycle.Lifecycle.Event.ON_ANY
                    Lifecycle.CREATE, Lifecycle.CREATED -> androidx.lifecycle.Lifecycle.Event.ON_CREATE
                    Lifecycle.START, Lifecycle.RESTART -> androidx.lifecycle.Lifecycle.Event.ON_START
                    Lifecycle.RESUME -> androidx.lifecycle.Lifecycle.Event.ON_RESUME
                    Lifecycle.PAUSE -> androidx.lifecycle.Lifecycle.Event.ON_PAUSE
                    Lifecycle.STOP -> androidx.lifecycle.Lifecycle.Event.ON_STOP
                    Lifecycle.DESTROY -> androidx.lifecycle.Lifecycle.Event.ON_DESTROY
                })
            }
            lifecycleCallback?.invoke(value, fId, FMStore.getManagersInfo())
        }

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
        curLifeState = Lifecycle.CREATE
        onHiddenChanged(isHidden)
    }

    protected abstract fun getView(inflater: LayoutInflater, container: ViewGroup?): View

    final override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (exists) change()
    }

    @Deprecated("Deprecated in Java", ReplaceWith("throw IllegalArgumentException(it was deprecated because thus has no ordering guarantees with regard to fragment lifecycle method calls.)"))
    final override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        throw IllegalArgumentException("it was deprecated because thus has no ordering guarantees with regard to fragment lifecycle method calls.")
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

    open fun performResume() { //Prevent all Fragment of Activity resume from getting focus
        if (parentHide(this)) {
            return
        }
        if (curLifeState == Lifecycle.CREATE) {
            performCreate()
            performStart()
        }

        //Since Fragment does not have onReStart()
        if (curLifeState == Lifecycle.STOP) {
            performReStart()
        }

        if (curLifeState == Lifecycle.START || curLifeState == Lifecycle.RESTART || curLifeState == Lifecycle.PAUSE) {
            curLifeState = Lifecycle.RESUME
            onResumed() //Adjust the top of the stack and expand at the back
            performChildResume()
        }
    }

    /**
     * @return true skipped  else processed.
     * */
    internal open fun onBackPressed(): Boolean {
        return true
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

    internal fun destroyFragment() {
        performPause()
        performStop()
        onDestroyed()
    }

    internal fun resumeFragment() {
        if (isStop) {
            onReStart()
        }
        onResumed()
    }

    protected open fun onCreate() {
    }

    protected open fun onStarted() {
    }

    protected open fun onReStart() {
    }

    protected open fun onResumed() {
        enableRoot(true)
    }

    protected open fun onPaused() {
        enableRoot(false)
    }

    protected open fun onStopped() {
    }

    protected open fun onDestroyed() {
        curLifeState = Lifecycle.DESTROY
        rootView = null
        onDestroyListener?.invoke(this)
        onDestroyListener = null
    }

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * @Deprecated use [onViewCreated] for code touching
     * the Fragment's view and {@link #onCreate(Bundle)} for other initialization.
     * */
    @Deprecated("Deprecated in Java", ReplaceWith("super.onActivityCreated(savedInstanceState)", "androidx.fragment.app.Fragment"))
    @Suppress("DEPRECATION")
    final override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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

    override fun onDestroyView() {
        destroyFragment()
        super.onDestroyView()
    }

    final override fun onDestroy() {
        super.onDestroy()
    }

    final override fun onDetach() {
        super.onDetach()
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

    override fun getLifecycle(): androidx.lifecycle.Lifecycle {
        return if (useDefaultLifecycle) super.getLifecycle() else lifecycleRegistry
    }
}