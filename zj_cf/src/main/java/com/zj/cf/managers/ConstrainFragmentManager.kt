@file:Suppress("unused")

package com.zj.cf.managers

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.UiThread
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.zj.cf.FMStore
import com.zj.cf.FMStore.getSimpleId
import com.zj.cf.annotations.ConstrainMode.*
import com.zj.cf.fragments.ConstrainFragment
import com.zj.cf.unitive.OnFinishCallBack
import com.zj.cf.unitive.ProxyManager
import java.util.*
import kotlin.math.max

abstract class ConstrainFragmentManager(act: FragmentActivity, managerId: String, manager: FragmentManager, @IdRes containerId: Int, internal val clearWhenEmptyStack: () -> Boolean) : FragmentHelper<ConstrainFragment>(act, managerId, manager, containerId) {

    private var stack: Stack<ProxyManager<*>>? = null
        get() {
            if (field == null) field = Stack();return field
        }

    @UiThread
    internal fun getCurrentStackSize(): Int {
        return stack?.size ?: 0
    }

    @UiThread
    internal fun setBackStack(backed: ProxyManager<*>) {
        stack?.let {
            synchronized(it) {
                val current: ProxyManager<*> = it.pop()
                val index = stack?.indexOfLast { px ->
                    getSimpleId(px.id).first == getSimpleId(backed.id).first
                } ?: -1
                if (index == -1) {
                    it.push(backed)
                    it.push(current)
                } else {
                    if (index == it.lastIndex) {
                        return
                    }
                    (0 until (it.lastIndex - index)).forEach { _ ->
                        stack?.pop()
                    }
                    it.push(current)
                }
            }
        }
    }

    @UiThread
    internal fun startFragment(proxy: ProxyManager<*>) {

        fun follow(proxy: ProxyManager<*>) {
            stack?.push(proxy)
        }

        fun clearStack(proxy: ProxyManager<*>) {
            stack?.removeAllElements(); stack?.push(proxy)
        }

        fun stack(proxy: ProxyManager<*>) {
            val firstStackIndex = stack?.indexOfLast { v ->
                getSimpleId(v.id).first == getSimpleId(proxy.id).first
            } ?: -1
            if (firstStackIndex == -1) {
                stack?.push(proxy)
            } else {
                val maxIndex = max(firstStackIndex, stack?.lastIndex ?: 0)
                if (maxIndex == firstStackIndex) {
                    stack?.removeAllElements();stack?.push(proxy)
                } else {
                    (0..(maxIndex - firstStackIndex)).forEach { _ ->
                        val p = stack?.pop()
                        if (p?.backMode == CONST_ONLY_ONCE) {
                            removeFragmentById(p.id)
                        }
                    }
                    stack?.push(proxy)
                }
            }
        }

        when (proxy.launchMode) {
            STACK -> stack(proxy)
            FOLLOW -> follow(proxy)
            CLEAR_BACK_STACK -> clearStack(proxy)
        }
        syncFrag(false, null)
    }

    private fun syncFrag(isBack: Boolean, bundle: Bundle?) {
        if (!stack.isNullOrEmpty()) stack?.let {
            synchronized(it) {
                it.peek()?.let { it ->
                    var frg = getFragmentById(it.id)
                    if (frg == null) {
                        frg = it.mFragmentClass.newInstance()
                        frg.setProxy(it)
                        if (isBack) frg.onPostValue(it.bundle)
                        addFragment(frg)
                    } else {
                        frg.setProxy(it)
                    }
                    if (isBack) frg?.onFragmentResult(bundle) else frg?.onPostValue(it.bundle)
                    showFragment(it.id)
                }
            }
        }
    }

    @UiThread
    fun finishTopFragment(onFinished: OnFinishCallBack? = null) {
        val fid = getTopOfStack()?.fId
        if (fid.isNullOrEmpty()) {
            onFinished?.errorWithStackEmpty()
            return
        }
        finishFragment(fid, onFinished)
    }

    @UiThread
    fun finishAll() {
        clearStack(false)
    }

    @UiThread
    fun finishFragment(id: String, onFinished: OnFinishCallBack? = null, forced: Boolean = false) {
        when {
            stack.isNullOrEmpty() -> onFinished?.errorWithStackEmpty()
            stack?.peek()?.id != id -> onFinished?.errorWithNotCurrent()
            else -> {
                stack?.pop()?.let {
                    if (it.isHome || stack.isNullOrEmpty()) {
                        stack?.clear()
                        FMStore.checkIsConstrainParent(managerId)
                        if (clearWhenEmptyStack() || forced) {
                            removeFragmentById(it.id) {
                                val mid = it.getManagerId()
                                val lastManager = FMStore.getManagerByLevel(mid, -1)
                                FMStore.removeManager(mid)
                                onFinished?.finished(id, true)
                                if (lastManager != null) {
                                    lastManager.getCurrentFragment()?.resumeFragment()
                                    onFinished?.setToPrevious(lastManager)
                                }
                                clearFragments()
                            }
                        } else {

                            stack?.push(it)
                            onFinished?.finishKeepWithTop(id)
                        }
                        return
                    }
                    when (it.backMode) {
                        CONST_ONLY_ONCE -> {
                            removeFragmentById(it.id) { onFinished?.finished(id, false) }
                        }
                        CONST_LASTING -> {
                            hideFragment(it.id) { onFinished?.finished(id, false) }
                        }
                    };syncFrag(true, it.getResultBundle())
                }
            }
        }
    }

    @UiThread
    fun clearStack(keepCurrent: Boolean = false) {
        if (stack?.isNotEmpty() == true) {
            val cur = if (keepCurrent) stack?.pop() else null
            while (stack?.isNotEmpty() == true) {
                stack?.peek()?.finish(force = true)
            }
            cur?.let { stack?.push(it) }
        }
    }
}