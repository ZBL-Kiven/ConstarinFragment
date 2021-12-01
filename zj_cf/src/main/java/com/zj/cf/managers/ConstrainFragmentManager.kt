package com.zj.cf.managers

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.UiThread
import androidx.fragment.app.FragmentManager
import com.zj.cf.FMStore
import com.zj.cf.FMStore.getSimpleId
import com.zj.cf.annotations.ConstrainMode.*
import com.zj.cf.fragments.ConstrainFragment
import com.zj.cf.unitive.ProxyManager
import java.util.*
import kotlin.math.max

internal abstract class ConstrainFragmentManager(managerId: String, manager: FragmentManager, @IdRes containerId: Int, val clearWhenEmptyStack: () -> Boolean) : FragmentHelper<ConstrainFragment>(managerId, manager, containerId) {

    private var stack: Stack<ProxyManager<*>>? = null
        get() {
            if (field == null) field = Stack();return field
        }

    @UiThread
    fun getCurrentStackSize(): Int {
        return stack?.size ?: 0
    }

    @UiThread
    fun setBackStack(backed: ProxyManager<*>) {
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
    fun startFragment(proxy: ProxyManager<*>) {

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
    fun finishFragment(id: String, onFinished: ((isEmptyStack: Boolean, clearWhenEmptyStack: Boolean) -> Unit)? = null) {
        if (stack?.peek()?.id != id) onFinished?.invoke(false, true); else {
            stack?.pop()?.let {
                if (it.isHome || stack.isNullOrEmpty()) {
                    stack?.clear()
                    FMStore.checkIsConstrainParent(managerId)
                    if (clearWhenEmptyStack()) {
                        removeFragmentById(it.id) {
                            clearFragments()
                            val mid = it.getManagerId()
                            FMStore.getManagerByLevel(mid, -1)?.let { lastManager ->
                                lastManager.getCurrentFragment()?.resumeFragment()
                            }
                            FMStore.removeManager(mid)
                            onFinished?.invoke(true, true)
                        }

                    } else {
                        stack?.push(it)
                        onFinished?.invoke(false, true)
                    }
                    return
                }
                when (it.backMode) {
                    CONST_ONLY_ONCE -> {
                        removeFragmentById(it.id) { onFinished?.invoke(false, false) }
                    }
                    CONST_LASTING -> {
                        hideFragment(it.id) { onFinished?.invoke(false, false) }
                    }
                };syncFrag(true, it.getResultBundle())
            }
        }
    }

    @UiThread
    fun clearStack() {
        if (stack?.isNotEmpty() == true) {
            val cur = stack?.pop()
            while (stack?.isNotEmpty() == true) {
                stack?.pop()
            }
            stack?.push(cur)
        }
    }
}