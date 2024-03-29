@file:Suppress("unused")

package com.zj.cf.managers

import android.annotation.SuppressLint
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.zj.cf.FMStore
import com.zj.cf.annotations.Constrain
import com.zj.cf.annotations.ConstrainHome
import com.zj.cf.annotations.LaunchMode
import com.zj.cf.annotations.parser.AnnotationParser
import com.zj.cf.cv.TabFragmentStateAdapter
import com.zj.cf.cv.TabFragmentViewHolder
import com.zj.cf.fragments.BaseTabFragment

/**
 * created by zjj on 21.06.23
 *
 * used for linkage fragment tab manage, the parent properties see [BaseFragmentManager]
 */

@Suppress("MemberVisibilityCanBePrivate")
abstract class TabFragmentManager<T, F : BaseTabFragment>(activity: FragmentActivity, private val container: ViewPager2, curIndex: Int, indicatorsParent: TabLayout, vararg data: T?) : FragmentHelper<F>(activity, -1) {

    protected var curSelectedId: String = ""
    private var curData: ArrayList<DataWrapInfo> = arrayListOf()
    private val adapter: TabFragmentAdapter
    private var tlm: TabLayoutMediator? = null

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
            val d = getCurData()[position]
            val fid = d.fid
            if (fid.isNotEmpty()) {
                curSelectedId = fid
                this@TabFragmentManager.syncSelectState(fid)
            } else {
                d.fid = warpPendingFid(position)
            }
        }
    }

    private fun warpPendingFid(position: Int): String {
        return "WAITING_FOR_PENDING_$position"
    }

    fun add(d: T?) {
        if (d == null) return
        curData.add(DataWrapInfo(d))
        adapter.notifyItemChanged(curData.size)
    }

    fun remove(d: T?): Boolean {
        val size = curData.size
        val b = curData.removeAll { it.d == d }
        adapter.notifyItemRangeChanged(0, size)
        return b
    }

    fun addAll(d: List<T?>?) {
        if (d.isNullOrEmpty()) return
        curData.addAll(d.mapNotNull { if (it != null) DataWrapInfo(it) else null })
        val size = curData.size
        adapter.notifyItemRangeChanged(0, size)
    }

    fun getCurData(): List<DataWrapInfo> {
        return curData
    }

    init {
        adapter = TabFragmentAdapter(activity, ::getCurData)
        addAll(data.asList())
        val fcs = curData.size
        if (curIndex !in 0 until fcs) throw IllegalStateException("Index out of range!")
        container.adapter = adapter
        tlm = TabLayoutMediator(indicatorsParent, container, ::tabConfigurationStrategy)
        tlm?.attach()
        container.currentItem = curIndex
        container.registerOnPageChangeCallback(pageChangeListener)
    }

    override fun getCurrentFragment(): F? {
        return getFragmentByInternalFId(curSelectedId)
    }

    override fun getCurrentItemId(): String {
        return curSelectedId
    }

    override fun beginTransaction(isHidden: Boolean, transaction: FragmentTransaction, frgCls: Class<F>) {
        transaction.setTransition(FragmentTransaction.TRANSIT_NONE)
    }

    override fun removeOnly(id: String) {
        super.removeOnly(id)
        curData.forEach { if (id.isNotEmpty() && it.fid == id) it.fid = "" }
    }

    /**
     * use it when context is activity.
     *
     * but no if you started it by a ConstrainFragment context.
     * */
    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        container.unregisterOnPageChangeCallback(pageChangeListener)
        container.removeAllViews()
        adapter.notifyDataSetChanged()
        FMStore.removeManager(this.managerId)
        tlm?.detach()
        clearFragments()
        curData.clear()
        tlm = null
    }

    internal inner class TabFragmentAdapter(activity: FragmentActivity, private val fIn: () -> List<DataWrapInfo>) : TabFragmentStateAdapter<F>(activity) {

        override fun getItemCount(): Int {
            return fIn().size
        }

        override fun onBindViewHolder(holder: TabFragmentViewHolder, position: Int, payloads: MutableList<Any>) {
            super.onBindViewHolder(holder, position, payloads)
        }

        override fun createFragment(position: Int): F {
            return if (FMStore.hasManager(this@TabFragmentManager.managerId)) initFrags(position)
            else throw IllegalArgumentException("new TabFragment should not create after manager destroyed!!")
        }

        override fun addViewToContainer(f: F, v: View, container: FrameLayout) {
            super.addViewToContainer(f, v, container)
            addFragment(f)
            FMStore.putAManager(f.managerId, this@TabFragmentManager, f.fId)
        }

        fun initFrags(position: Int): F {
            val d = fIn()[position]
            val f = this@TabFragmentManager.onCreateFragment(d.d, position)
            val reSyncNeeded = warpPendingFid(position) == d.fid
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
            d.fid = f.fId
            if (reSyncNeeded) {
                curSelectedId = f.fId
                this@TabFragmentManager.syncSelectState(f.fId)
            }
            return f
        }
    }

    inner class DataWrapInfo(val d: T, var fid: String = "")
}