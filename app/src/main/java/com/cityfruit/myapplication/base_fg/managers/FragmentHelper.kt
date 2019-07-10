@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.cityfruit.myapplication.base_fg.managers

import android.support.annotation.UiThread
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import com.cityfruit.myapplication.base_fg.FMStore
import com.cityfruit.myapplication.base_fg.fragments.BaseFragment
import com.cityfruit.myapplication.base_fg.fragments.ConstrainFragment
import com.cityfruit.myapplication.base_fg.log
import com.cityfruit.myapplication.base_fg.unitive.FragmentObserver
import com.cityfruit.myapplication.base_fg.unitive.FragmentOperator
import java.util.*
import java.lang.NullPointerException

/**
 * Created by zjj on 19.05.14
 */
@UiThread
abstract class FragmentHelper<F : BaseFragment> : FragmentOperator<F> {

    open val managerId: String = UUID.randomUUID().toString()
    private val fragmentManager: FragmentManager
    private val containId: Int

    constructor(fragment: BaseFragment, containId: Int) : this(if (fragment is ConstrainFragment) fragment.managerId else fragment.id, fragment.childFragmentManager, containId)

    constructor(act: FragmentActivity, containId: Int) : this("", act.supportFragmentManager, containId)

    constructor(managerId: String, f: FragmentManager, c: Int) {
        FMStore.putAManager(managerId, getManager());fragmentManager = f;containId = c
    }

    private fun getManager(): FragmentHelper<*> {
        return this
    }

    private var currentItem = ""
    private var oldItem = ""
    private val mFragments = mutableMapOf<String, F>()
    private var fragmentObserver: FragmentObserver? = null

    fun getCurrentItemId(): String {
        return currentItem
    }

    @UiThread
    fun getFragments(): List<F>? {
        return if (mFragments.isNullOrEmpty()) null else mFragments.mapTo(arrayListOf()) {
            it.value
        }
    }

    fun getFragmentIds(): List<String>? {
        return if (mFragments.isNullOrEmpty()) null else mFragments.mapTo(arrayListOf()) {
            it.key
        }
    }

    /**
     * it may useless when the activity inserted,
     *
     * only supported by fragmentHelper extensions.
     * */
    fun getTopOfStack(): BaseFragment? {
        return FMStore.getTopConstrainFragment(managerId)
    }

    @UiThread
    fun getFragmentById(id: String): F? {
        return mFragments[id]
    }

    @UiThread
    fun getCurrentFragment(): F? {
        return mFragments[currentItem]
    }

    /**
     * it could be ANR，when the fragment was transaction by some long tasks
     * setFragmentObserver can pause the transaction,call observer's method : StateChange to resume this operation when it was finalized
     */
    @UiThread
    fun setFragmentObserver(observer: FragmentObserver) {
        this.fragmentObserver = observer
    }

    @UiThread
    fun addFragment(vararg fragment: F?) {
        addFragments(fragment.filterNotNull().toList())
    }

    @UiThread
    fun addFragments(fragments: List<F>?) {
        if (!fragments.isNullOrEmpty()) fragments.forEach {
            mFragments[it.id] = it.apply {
                this.managerId = this@FragmentHelper.managerId
            }
        }
    }

    @UiThread
    fun removeFragmentById(id: String, onRemoved: (() -> Unit)? = null) {
        log("removeFragmentById : $id")
        fun remove(frg: F) {
            runInTransaction(true, frg) {
                it.remove(frg)
                mFragments.remove(id)
                onRemoved?.invoke()
            }
        }
        getFragmentById(id)?.let { frg ->
            if (currentItem == id) {
                hideFragment(frg, true) {
                    remove(frg)
                }
            } else {
                remove(frg)
            }
        }
    }

    @UiThread
    fun hideFragment(id: String, onHidden: (() -> Unit)? = null) {
        fun hide() {
            onHidden?.invoke()
        }
        getFragmentById(id)?.let { frg ->
            if (currentItem == id) {
                hideFragment(frg, true) {
                    hide()
                }
            } else {
                hide()
            }
        }
    }

    /**
     * @param showId the shown fragment id
     */
    @UiThread
    fun showFragment(showId: String) {
        if (showId == currentItem) {
            whenShowSameFragment(showId)
        } else {
            if (whenShowNotSameFragment(showId)) {
                currentItem = showId
                performSelectItem()
            }
        }
    }

    /**
     * hide the current fragment,and display next fragment
     */
    private fun performSelectItem() {
        if (currentItem == oldItem) return
        hideFragments(false) { k, _ -> return@hideFragments k != currentItem }
        showNewFragment {
            syncSelectState(it)
            oldItem = it
        }
    }

    private fun showNewFragment(onShown: ((cur: String) -> Unit)? = null) {
        getFragmentById(currentItem)?.let { frg ->
            fun shown() {
                runInTransaction(true, frg) { it.show(frg) }
                onShown?.invoke(frg.id)
            }
            if (frg.isAdded) {
                frg.onResume()
                (fragmentObserver?.beforeHiddenChange(frg, false) { shown() }) ?: shown()
            } else {
                frg.let { fragmentManager.beginTransaction().add(containId, frg, frg.javaClass.simpleName).show(frg).commit();onShown?.invoke(frg.id) }
            }
        } ?: throw NullPointerException("bad call ! ,case : your current shown item was never instanced form data source")
    }

    private fun hideFragments(isRemoved: Boolean, case: (k: String, v: F) -> Boolean) {
        mFragments.forEach { (k, v) ->
            if (case(k, v)) hideFragment(v, isRemoved)
        }
    }

    private fun hideFragment(v: F?, isRemoved: Boolean, onHidden: (() -> Unit)? = null) {
        fun hide(v: F) {
            if (v.isResumed) {
                v.onPause()
                v.onStop()
            }
            runInTransaction(true, v) { it.hide(v);onHidden?.invoke() }
        }

        if (v == null) {
            onHidden?.invoke();return
        }
        if (v.isAdded) {
            if (!v.isHidden) (fragmentObserver?.beforeHiddenChange(v, true) { hide(v) }) ?: hide(v)
        } else if (!isRemoved) {
            fragmentManager.beginTransaction().add(containId, v, v.javaClass.simpleName).hide(v).commit()
        }
    }

    override fun syncSelectState(selectId: String) {}

    override fun whenShowSameFragment(shownId: String) {}

    override fun whenShowNotSameFragment(shownId: String): Boolean {
        return true
    }

    private fun runInTransaction(isHidden: Boolean, fragment: F, run: (FragmentTransaction) -> Unit) {
        val transaction = fragmentManager.beginTransaction()
        try {
            beginTransaction(isHidden, transaction, fragment.javaClass)
            run(transaction)
        } finally {
            transaction.commit()
        }
    }

    protected fun clearFragments() {
        mFragments.clear()
        fragmentManager.popBackStack()
    }
}
