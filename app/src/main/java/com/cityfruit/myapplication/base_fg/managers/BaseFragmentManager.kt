@file:Suppress("unused")

package com.cityfruit.myapplication.base_fg.managers

import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentTransaction
import android.view.View
import android.view.ViewGroup
import com.cityfruit.myapplication.base_fg.annotations.AnnotationParser
import com.cityfruit.myapplication.base_fg.annotations.Constrain
import com.cityfruit.myapplication.base_fg.annotations.ConstrainHome
import com.cityfruit.myapplication.base_fg.annotations.LaunchMode
import com.cityfruit.myapplication.base_fg.fragments.BaseLinkageFragment

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

    constructor(fragment: Fragment, @IdRes containerId: Int, curIndex: Int, indicatorsParent: ViewGroup, vararg fragments: BaseLinkageFragment?) : super(fragment, containerId) {
        init(fragments.filterNotNull().toList(), getViewsByViewGroup(indicatorsParent), curIndex)
    }

    constructor(activity: FragmentActivity, @IdRes containerId: Int, curIndex: Int, indicatorViews: List<View>, vararg fragments: BaseLinkageFragment?) : super(activity, containerId) {
        init(fragments.filterNotNull().toList(), indicatorViews, curIndex)
    }

    constructor(fragment: Fragment, @IdRes containerId: Int, curIndex: Int, indicatorViews: List<View>, vararg fragments: BaseLinkageFragment?) : super(fragment, containerId) {
        init(fragments.filterNotNull().toList(), indicatorViews, curIndex)
    }


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
            throw IllegalArgumentException("if you aren't set the linkage view in your LinkageFragment, so the indicators size must equals the fragments size")
        }
    }

    private fun init(fragments: List<BaseLinkageFragment>, indicatorViews: List<View>, curIndex: Int) {
        checkChildValidate(fragments.size, indicatorViews)
        if (fragments.isNullOrEmpty()) throw NullPointerException("the empty fragments was no point")
        if (curIndex !in 0..fragments.lastIndex) throw IndexOutOfBoundsException("current index was $curIndex but fragments size was ${fragments.size}")
        val curItem = fragments[curIndex].id
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
        }
        indicatorViews.forEachIndexed { i, v ->
            fragments[i].linkageView = v
            attachView(v)
        }
        addFragments(fragments)
        show(curItem, true)
    }

    private fun attachView(v: View?) {
        v?.let { onViewAttach(it) }
    }

    /**
     * override this method to build your childView
     *
     * in mind ,the views may disorder or defect
     * */
    open fun onViewAttach(v: View) {}

    /**
     *  prevent the quick click event jitter
     * */
    private var clickTime: Long = 0

    private fun show(curItem: String, ignoreFastClick: Boolean = false) {
        if (!ignoreFastClick && System.currentTimeMillis() - clickTime < 300) {
            return
        }
        clickTime = System.currentTimeMillis()
        showFragment(curItem)
        initViews()
    }

    fun selectedFragment(id: String) {
        getFragmentById(id)?.linkageView?.callOnClick()
    }

    private fun initViews() {
        getFragments()?.forEach { frg ->
            frg.linkageView?.setOnClickListener { showFragment(frg.id) }
        }
    }

    override fun beginTransaction(transaction: FragmentTransaction, curShowId: String, mFragments: MutableMap<String, BaseLinkageFragment>) {
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
    }

    override fun syncSelectState(selectId: String) {

        fun onSelectState(v: View?, isSelected: Boolean) {
            if (v?.isSelected != isSelected) v?.isSelected = isSelected
        }
        getFragments()?.forEach {
            onSelectState(it.linkageView, it.id == selectId)
        }
    }
}