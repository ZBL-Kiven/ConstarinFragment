@file:Suppress("unused")

package com.cityfruit.myapplication.base_fg.managers

import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentTransaction
import android.view.View
import android.view.ViewGroup
import com.cityfruit.myapplication.base_fg.annotations.*
import com.cityfruit.myapplication.base_fg.fragments.BaseLinkageFragment
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

    constructor(activity: FragmentActivity, @IdRes containerId: Int, curItem: String, indicatorsParent: ViewGroup? = null, vararg fragments: BaseLinkageFragment?) : super(activity, containerId) {
        init(fragments.filterNotNull().toList(), indicatorsParent, curItem)
    }

    constructor(fragment: Fragment, @IdRes containerId: Int, curItem: String, indicatorsParent: ViewGroup? = null, vararg fragments: BaseLinkageFragment?) : super(fragment, containerId) {
        init(fragments.filterNotNull().toList(), indicatorsParent, curItem)
    }

    private fun init(fragments: List<BaseLinkageFragment>, indicatorsParent: ViewGroup?, curItem: String) {
        if (fragments.isNullOrEmpty()) throw NullPointerException("the empty fragments was no point")
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
        val canSeedByViews = indicatorsParent?.childCount == fragments.size
        if (indicatorsParent != null && !canSeedByViews) {
            throw IllegalArgumentException("if you aren't set the linkage view in your LinkageFragment, so the indicators size must equals the fragments size")
        }
        if (indicatorsParent != null && canSeedByViews) {
            for (i in 0 until indicatorsParent.childCount) {
                val v: View? = indicatorsParent.getChildAt(i)
                fragments[i].linkageView = v
                attachView(v)
            }
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
        getFragments()?.forEach {
            it.linkageView?.setOnClickListener { _ -> showFragment(it.id) }
        }
    }

    override fun beginTransaction(transaction: FragmentTransaction, curShowId: String, mFragments: MutableMap<String, BaseLinkageFragment>) {
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
    }

    override fun syncSelectState(selectId: String) {
        getFragments()?.forEach {
            onSelectState(it.linkageView, it.id == selectId)
        }
    }

    private fun onSelectState(v: View?, isSelected: Boolean) {
        if (v?.isSelected != isSelected) v?.isSelected = isSelected
    }
}