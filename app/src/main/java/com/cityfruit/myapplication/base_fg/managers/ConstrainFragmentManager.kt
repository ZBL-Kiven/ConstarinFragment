package com.cityfruit.myapplication.base_fg.managers

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.annotation.UiThread
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import com.cityfruit.myapplication.base_fg.BackMode
import com.cityfruit.myapplication.base_fg.LaunchMode
import com.cityfruit.myapplication.base_fg.fragments.ConstrainFragment
import com.cityfruit.myapplication.base_fg.getSimpleId
import com.cityfruit.myapplication.base_fg.log
import com.cityfruit.myapplication.base_fg.unitive.ProxyManager
import java.lang.NullPointerException
import java.util.*

internal open class ConstrainFragmentManager(manager: FragmentManager, @IdRes containerId: Int, val whenEmptyStack: () -> Unit) : FragmentHelper<ConstrainFragment>(manager, containerId) {

    val managerId: String = UUID.randomUUID().toString()

    private var stack: Stack<ProxyManager<*>>? = null
        get() {
            if (field == null) field = Stack();return field
        }

    override fun beginTransaction(transaction: FragmentTransaction, curShowId: String, mFragments: MutableMap<String, ConstrainFragment>) {
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
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
                }
                printStack()
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
                val maxIndex = Math.max(firstStackIndex, stack?.lastIndex ?: 0)
                if (maxIndex == firstStackIndex) {
                    stack?.removeAllElements();stack?.push(proxy)
                } else {
                    (0..(maxIndex - firstStackIndex)).forEach { _ ->
                        val p = stack?.pop()
                        if (p?.backMode == BackMode.ONLY_ONCE) {
                            removeFragmentById(p.id)
                        }
                    }
                    stack?.push(proxy)
                }
            }
        }

        when (proxy.launchMode) {
            LaunchMode.STACK -> stack(proxy)
            LaunchMode.FOLLOW -> follow(proxy)
            LaunchMode.CLEAR_BACK_STACK -> clearStack(proxy)
        }
        printStack()
        syncFrag(false, null)
    }

    private fun syncFrag(isBack: Boolean, bundle: Bundle?) {
        if (!stack.isNullOrEmpty()) stack?.let {
            synchronized(it) {
                it.peek()?.let { it ->
                    var frg = getFragmentById(it.id)
                    if (frg == null) {
                        frg = it.mFragmentClass.newInstance()
                        frg?.setProxy(it)
                        if (isBack) frg?.onPostValue(it.bundle)
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
    fun finishFragment(id: String, onFinished: (() -> Unit)? = null) {
        if (stack?.peek()?.id != id) onFinished?.invoke(); else {
            stack?.pop()?.let {
                if (it.isHome || stack.isNullOrEmpty()) {
                    stack?.clear()
                    clearFragments()
                    whenEmptyStack()
                    return
                }
                when (it.backMode) {
                    BackMode.ONLY_ONCE -> {
                        removeFragmentById(it.id, onFinished)
                    }
                    BackMode.LASTING -> {
                        hideFragment(it.id, onFinished)
                    }
                };syncFrag(true, it.getResultBundle())
            }
            printStack()
        }
    }

    private fun printStack() {
        val print = stack?.map { getSimpleId(it.id).first }?.asSequence()?.joinToString()
        log("the cur stack data is : $print")
    }
}