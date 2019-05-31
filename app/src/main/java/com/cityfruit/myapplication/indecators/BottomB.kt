package com.cityfruit.myapplication.indecators

import android.widget.FrameLayout
import com.cityfruit.myapplication.R
import com.cityfruit.myapplication.base_fg.annotations.Container
import com.cityfruit.myapplication.base_fg.fragments.BaseLinkageFragment
import com.cityfruit.myapplication.base_fg.startFragmentByNewTask
import com.cityfruit.myapplication.fragments.FragmentA
import com.cityfruit.myapplication.getBundle
import kotlinx.android.synthetic.main.bottom.*

class BottomB : BaseLinkageFragment() {
    override val layoutId: Int
        get() = R.layout.bottom

    @Container
    var container: FrameLayout? = null

    override fun initView() {
        container = bottom_fl
    }

    override fun initData() {
        startFragmentByNewTask(FragmentA::class.java, getBundle("bottomB 启动了 FrgA"), {
            container?.removeAllViews()
        })
    }

}
