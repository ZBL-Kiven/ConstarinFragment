@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.zj.cf.managers

import androidx.annotation.UiThread
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.zj.cf.FMStore
import com.zj.cf.fragments.BaseFragment
import com.zj.cf.fragments.ConstrainFragment
import com.zj.cf.unitive.FragmentObserver
import com.zj.cf.unitive.FragmentOperator
import java.lang.Exception
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

    constructor(fragment: BaseFragment, containId: Int) : this(if (fragment is ConstrainFragment) fragment.managerId else fragment.fId, fragment.childFragmentManager, containId)

    constructor(act: FragmentActivity, containId: Int) : this("", act.supportFragmentManager, containId)

    constructor(managerId: String, f: FragmentManager, c: Int) {
        FMStore.putAManager(managerId, getManager());fragmentManager = f;containId = c
    }

    private fun getManager(): FragmentHelper<*> {
        return this
    }

    private var currentItem = ""
    private var oldItem = ""
    private var fragmentObserver: FragmentObserver? = null
    protected val mFragments = mutableMapOf<String, F>()

    open fun getCurrentItemId(): String {
        return currentItem
    }

    @UiThread
    fun getFragments(): List<F>? {
        return if (mFragments.isNullOrEmpty()) null else mFragments.mapTo(arrayListOf()) {
            it.value
        }
    }

    open fun getFragmentIds(): List<String>? {
        return if (mFragments.isNullOrEmpty()) null else mFragments.mapTo(arrayListOf()) {
            it.key
        }
    }

    /**
     * it may useless when the activity inserted,
     *
     * only supported by fragmentHelper extensions.
     * */
    open fun getTopOfStack(): BaseFragment? {
        return FMStore.getTopConstrainFragment(managerId)
    }

    @UiThread
    open fun getFragmentById(id: String): F? {
        return mFragments[id]
    }

    @UiThread
    open fun getCurrentFragment(): F? {
        return mFragments[currentItem]
    }

    /**
     * it could be ANR，when the fragment was transaction by some long tasks
     * setFragmentObserver can pause the transaction,call observer's method : StateChange to resume this operation when it was finalized
     */
    @UiThread
    open fun setFragmentObserver(observer: FragmentObserver) {
        this.fragmentObserver = observer
    }

    @UiThread
    fun addFragment(vararg fragment: F?) {
        addFragments(fragment.filterNotNull().toList())
    }

    @UiThread
    fun addFragments(fragments: List<F>?) {
        if (!fragments.isNullOrEmpty()) fragments.forEach {
            mFragments[it.fId] = it.apply {
                this.managerId = this@FragmentHelper.managerId
            }
        }
    }

    @UiThread
    internal fun removeFragmentById(id: String, onRemoved: (() -> Unit)? = null) {
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
     * @param showId the shown fragment fId
     */
    @UiThread
    fun showFragment(showId: String) {
        if (showId == currentItem) {
            whenShowSameFragment(showId)
        } else {
            if (whenShowNotSameFragment(showId, currentItem)) {
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
                onShown?.invoke(frg.fId)
            }
            if (frg.isExists()) {
                (fragmentObserver?.beforeHiddenChange(frg, false) { shown() }) ?: shown()
            } else {
                frg.let { f ->
                    runInTransaction(null, f) {
                        it.add(containId, f, f.fId).show(f)
                    }
                    onShown?.invoke(frg.fId)
                }
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
            if (isRemoved) v.destroyFragment()
            runInTransaction(true, v) { it.hide(v);onHidden?.invoke() }
        }

        if (v == null) {
            onHidden?.invoke();return
        }
        if (v.isExists()) {
            if (!v.isHidden) (fragmentObserver?.beforeHiddenChange(v, true) { hide(v) }) ?: hide(v)
        } else if (!isRemoved) {
            runInTransaction(null, v) { it.add(containId, v, v.fId).hide(v) }
        }
    }

    override fun syncSelectState(selectId: String) {}

    override fun whenShowSameFragment(shownId: String) {}

    override fun whenShowNotSameFragment(shownId: String, lastId: String): Boolean {
        return true
    }

    private fun F.isExists(): Boolean {
        return isAdded
    }

    /**
     * running a transaction form manager
     * @param isHidden it may ignore with null,else it call overridden to set a transaction type
     * */
    private fun runInTransaction(isHidden: Boolean?, fragment: F, run: (FragmentTransaction) -> Unit) {
        val transaction = fragmentManager.beginTransaction()
        try {
            if (isHidden != null) beginTransaction(isHidden, transaction, fragment.javaClass)
            run(transaction)
        } finally {
            try {
                transaction.commitNowAllowingStateLoss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    internal fun clearFragments() {
        mFragments.clear()
    }
}
