package com.cityfruit.myapplication.indecators

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.cityfruit.myapplication.R
import com.zj.cf.fragments.BaseLinkageFragment
import com.cityfruit.myapplication.tabs.FragmentTab
import com.cityfruit.myapplication.tabs.FragmentTab1
import com.cityfruit.myapplication.tabs.FragmentTab2
import com.google.android.material.tabs.TabLayout
import com.zj.cf.fragments.BaseTabFragment
import com.zj.cf.managers.TabFragmentManager
import kotlinx.android.synthetic.main.tab.*

class BottomD : BaseLinkageFragment() {

    override val fId: String; get() = "bottom_D" + super.fId

    override fun getView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.tab, container, false)
    }

    override fun onFragmentStarted() {
        super.onFragmentStarted()
        vp2.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        vp2.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        object : TabFragmentManager<BaseTabFragment>(requireActivity(), vp2, 0, tab, FragmentTab(), FragmentTab1(), FragmentTab2()) {

            override fun tabConfigurationStrategy(tab: TabLayout.Tab, position: Int) {
                tab.text = "TAB-$position"
            }
        }
    }

}
