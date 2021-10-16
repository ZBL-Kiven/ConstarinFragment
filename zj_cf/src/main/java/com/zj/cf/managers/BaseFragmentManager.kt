@file:Suppress("unused")

package com.zj.cf.managers

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import android.view.View
import android.view.ViewGroup
import com.zj.cf.FMStore
import com.zj.cf.annotations.*
import com.zj.cf.annotations.parser.AnnotationParser
import com.zj.cf.fragments.BaseFragment
import com.zj.cf.fragments.BaseLinkageFragment
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.lang.NullPointerException

/**
 * created by zjj on 19.05.14
 *
 * used for linkage fragment manage,the clickable view will bind a linkageFragment,
 *
 * automatic selected the fragment when the view called click
 *
 * support lifecycle annotation , but the activity will finish when all fragments finished
 */

abstract class BaseFragmentManager : FragmentHelper<BaseLinkageFragment> {

    constructor(activity: FragmentActivity, @IdRes containerId: Int, curIndex: Int, indicatorsParent: ViewGroup, vararg fragments: BaseLinkageFragment?) : super(activity, containerId) {
        init(fragments.filterNotNull().toList(), getViewsByViewGroup(indicatorsParent), curIndex)
    }

    constructor(fragment: BaseFragment, @IdRes containerId: Int, curIndex: Int, indicatorsParent: ViewGroup, vararg fragments: BaseLinkageFragment?) : super(fragment, containerId) {
        init(fragments.filterNotNull().toList(), getViewsByViewGroup(indicatorsParent), curIndex)
    }

    constructor(activity: FragmentActivity, @IdRes containerId: Int, curIndex: Int, indicatorViews: List<View>, vararg fragments: BaseLinkageFragment?) : super(activity, containerId) {
        init(fragments.filterNotNull().toList(), indicatorViews, curIndex)
    }

    constructor(fragment: BaseFragment, @IdRes containerId: Int, curIndex: Int, indicatorViews: List<View>, vararg fragments: BaseLinkageFragment?) : super(fragment, containerId) {
        init(fragments.filterNotNull().toList(), indicatorViews, curIndex)
    }

    /**
     * override this method to build your childView
     *
     * in mind ,the views may disorder or defect
     * */
    open fun onViewAttach(v: View) {}

    private fun getViewsByViewGroup(indicatorsParent: ViewGroup?): List<View> {
        if (indicatorsParent == null || indicatorsParent.childCount <= 0) {
            throw IllegalArgumentException("if you aren't set the linkage view in your LinkageFragment, so the indicators parent must not be empty")
        }
        val children = mutableListOf<View>()
        for (i in 0 until indicatorsParent.childCount) {
            children.add(indicatorsParent.getChildAt(i))
        }
        return children
    }

    private fun checkChildValidate(fragmentSize: Int, indicators: List<View>) {
        val canSeedByViews = indicators.size == fragmentSize
        if (!canSeedByViews) {
            throw IllegalArgumentException("the indicator views size must equals the fragments size")
        }
    }

    private fun init(fragments: List<BaseLinkageFragment>, indicatorViews: List<View>, curIndex: Int) {
        checkChildValidate(fragments.size, indicatorViews)
        if (fragments.isNullOrEmpty()) throw NullPointerException("the empty fragments was no point")
        if (curIndex !in 0..fragments.lastIndex) throw IndexOutOfBoundsException("current index was $curIndex but fragments size was ${fragments.size}")
        val curItem = fragments[curIndex].fId
        fragments.forEach {
            val hasHome = AnnotationParser.parseCls<ConstrainHome>(it::class.java) != null
            val hasLaunchMode = AnnotationParser.parseCls<LaunchMode>(it::class.java) != null
            val hasConstrain = AnnotationParser.parseCls<Constrain>(it::class.java) != null
            if (hasConstrain) {
                throw IllegalStateException("the base fragment manager was not supported by Constrain annotation")
            }
            if (hasHome) {
                throw IllegalStateException("the base fragment manager was not supported by ConstrainHome annotation")
            }
            if (hasLaunchMode) {
                throw IllegalStateException("the base fragment manager was not supported by LaunchMode annotation")
            }
            FMStore.putAManager(it.managerId, this, it.fId)
        }
        indicatorViews.forEachIndexed { i, v ->
            fragments[i].linkageView = v
            attachView(v)
        }
        addFragments(fragments)
        show(curItem, false)
    }

    private fun attachView(v: View?) {
        v?.let { onViewAttach(it) }
    }

    @Suppress("SameParameterValue")
    private fun show(curItem: String, fromUser: Boolean) {
        showFragment(curItem, fromUser)
        getFragments()?.forEach { frg ->
            frg.linkageView?.setOnClickListener {
                showFragment(frg.fId)
            }
        }
    }

    fun selectedFragment(id: String) {
        getFragmentById(id)?.linkageView?.callOnClick()
    }

    /**
     * use it when context is activity.
     *
     * but no if you started it by a ConstrainFragment context.
     * */
    fun clear() {
        FMStore.removeManager(this.managerId)
    }

    override fun beginTransaction(isHidden: Boolean, transaction: FragmentTransaction, frgCls: Class<BaseLinkageFragment>) {
        transaction.setTransition(FragmentTransaction.TRANSIT_NONE)
    }

    override fun syncSelectState(selectId: String) {

        fun onSelectState(v: View?, isSelected: Boolean) {
            if (v?.isSelected != isSelected) v?.isSelected = isSelected
        }
        getFragments()?.forEach {
            onSelectState(it.linkageView, it.fId == selectId)
        }
    }
}