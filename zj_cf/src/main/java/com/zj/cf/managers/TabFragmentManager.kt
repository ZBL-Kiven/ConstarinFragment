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

abstract class TabFragmentManager<T : TabFragmentManager.TabDataIn, F : BaseTabFragment>(activity: FragmentActivity, container: ViewPager2, curIndex: Int, indicatorsParent: TabLayout, vararg data: T?) : FragmentHelper<F>(activity, container.id) {

    private var curSelectedId: String = ""
    private var curData: ArrayList<T> = arrayListOf()
    private val adapter: TabFragmentAdapter
    private val onDestroyCallBack = { f: BaseTabFragment ->
        removeOnly(f.fId)
    }

    abstract fun tabConfigurationStrategy(tab: TabLayout.Tab, position: Int)
    abstract fun onCreateFragment(d: T, p: Int): F

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
            data[position]?.fragmentId?.let {
                curSelectedId = it
                this@TabFragmentManager.syncSelectState(it)
            }
        }
    }

    fun add(d: T?) {
        if (d == null) return
        curData.add(d)
        adapter.notifyDataSetChanged()
    }

    fun remove(d: T?): Boolean {
        val b = curData.remove(d)
        adapter.notifyDataSetChanged()
        return b
    }

    fun addAll(d: List<T?>?) {
        if (d.isNullOrEmpty()) return
        curData.addAll(d.filterNotNull())
        adapter.notifyDataSetChanged()
    }

    init {
        curData.addAll(data.filterNotNull())
        val fcs = curData.size
        if (curIndex !in 0 until fcs) throw IllegalStateException("Index out of range!")
        adapter = TabFragmentAdapter(activity) { curData }
        (0..curIndex).forEach {
            val f = adapter.initFrags(it, true)
            if (curIndex == it) curSelectedId = f.fId
        }
        container.adapter = adapter
        TabLayoutMediator(indicatorsParent, container, ::tabConfigurationStrategy).attach()
        container.currentItem = curIndex
        container.registerOnPageChangeCallback(pageChangeListener)
    }

    override fun getCurrentFragment(): F? {
        return getFragmentById(curSelectedId)
    }

    override fun getCurrentItemId(): String {
        return curSelectedId
    }

    override fun beginTransaction(isHidden: Boolean, transaction: FragmentTransaction, frgCls: Class<F>) {
        transaction.setTransition(FragmentTransaction.TRANSIT_NONE)
    }

    /**
     * use it when context is activity.
     *
     * but no if you started it by a ConstrainFragment context.
     * */
    fun clear() {
        FMStore.removeManager(this.managerId)
    }

    inner class TabFragmentAdapter(activity: FragmentActivity, private val fIn: () -> List<T>) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int {
            return fIn().size
        }

        override fun createFragment(position: Int): F {
            return initFrags(position)
        }

        fun initFrags(position: Int, needsToUpdateStore: Boolean = false): F {
            val d = fIn()[position]
            val f = this@TabFragmentManager.onCreateFragment(d, position)
            d.fragmentId = f.fId
            val hasHome = AnnotationParser.parseCls<ConstrainHome>(f::class.java) != null
            val hasLaunchMode = AnnotationParser.parseCls<LaunchMode>(f::class.java) != null
            val hasConstrain = AnnotationParser.parseCls<Constrain>(f::class.java) != null
            if (hasConstrain) {
                throw IllegalStateException("the base fragment manager was not supported by Constrain annotation")
            }
            if (hasHome) {
                throw IllegalStateException("the base fragment manager was not supported by ConstrainHome annotation")
            }
            if (hasLaunchMode) {
                throw IllegalStateException("the base fragment manager was not supported by LaunchMode annotation")
            }
            addFragment(f)
            f.setOnDestroyCallback(onDestroyCallBack)
            if (needsToUpdateStore) FMStore.putAManager(f.managerId, this@TabFragmentManager, f.fId)
            return f
        }
    }

    interface TabDataIn {
        var fragmentId: String
    }
}