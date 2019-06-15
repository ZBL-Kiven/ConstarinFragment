package com.cityfruit.myapplication.indecators

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.cityfruit.myapplication.R
import com.cityfruit.myapplication.base_fg.annotations.Container
import com.cityfruit.myapplication.base_fg.fragments.BaseLinkageFragment
import com.cityfruit.myapplication.base_fg.startFragmentByNewTask
import com.cityfruit.myapplication.fragments.FragmentA
import com.cityfruit.myapplication.getBundle
import kotlinx.android.synthetic.main.bottom.*

class BottomB : BaseLinkageFragment() {

    override fun getView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.bottom, container, false)
    }

    @Container
    var container: ViewGroup? = null

    override fun initView() {
        container = rootView as? ViewGroup
    }

    override fun initData() {
        startFragmentByNewTask(FragmentA::class.java, getBundle("bottomB 启动了 FrgA"), {
            container?.removeAllViews()
        })
    }

}
