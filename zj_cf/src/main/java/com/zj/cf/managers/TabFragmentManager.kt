@file:Suppress("unused")

package com.zj.cf.managers

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.zj.cf.FMStore
import com.zj.cf.annotations.Constrain
import com.zj.cf.annotations.ConstrainHome
import com.zj.cf.annotations.LaunchMode
import com.zj.cf.annotations.parser.AnnotationParser
import com.zj.cf.fragments.BaseTabFragment
import java.lang.IllegalStateException

/**
 * created by zjj on 21.06.23
 *
 * used for linkage fragment tab manage, the parent properties see [BaseFragmentManager]
 */

abstract class TabFragmentManager<T : BaseTabFragment>(activity: FragmentActivity, container: ViewPager2, curIndex: Int, indicatorsParent: TabLayout, vararg fragments: T?) : FragmentHelper<T>(activity, container.id) {

    private var curSelectedId: String = ""

    abstract fun tabConfigurationStrategy(tab: TabLayout.Tab, position: Int)
    open fun onPageScrollStateChanged(state: Int) {}
    open fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    private val pageChangeListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {
            this@TabFragmentManager.onPageScrollStateChanged(state)
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            this@TabFragmentManager.onPageScrolled(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageSelected(position: Int) {
            fragments[position]?.fId?.let {
                curSelectedId = it
                this@TabFragmentManager.syncSelectState(it)
            }
        }
    }

    init {
        val fcs = fragments.size
        if (curIndex !in 0 until fcs) throw IllegalStateException("Index out of range!")
        fragments.forEach {
            if (it != null) {
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
        }
        addFragment(*fragments)
        container.adapter = TabFragmentAdapter(activity) { getFragments() ?: arrayListOf() }
        TabLayoutMediator(indicatorsParent, container, ::tabConfigurationStrategy).attach()
        curSelectedId = fragments[curIndex]?.fId ?: ""
        container.currentItem = curIndex
        container.registerOnPageChangeCallback(pageChangeListener)
    }

    override fun getCurrentFragment(): T? {
        return getFragmentById(curSelectedId)
    }

    override fun getCurrentItemId(): String {
        return curSelectedId
    }

    override fun beginTransaction(isHidden: Boolean, transaction: FragmentTransaction, frgCls: Class<T>) {
        transaction.setTransition(FragmentTransaction.TRANSIT_NONE)
    }

    inner class TabFragmentAdapter<T : BaseTabFragment>(activity: FragmentActivity, private val fIn: () -> List<T>) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int {
            return fIn().size
        }

        override fun createFragment(position: Int): T {
            return fIn()[position]
        }
    }
}