package com.cityfruit.myapplication.indecators

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.cityfruit.myapplication.R
import com.cityfruit.myapplication.ToastUtils
import com.zj.cf.annotations.Container
import com.zj.cf.fragments.BaseLinkageFragment
import com.zj.cf.startFragmentByNewTask
import com.cityfruit.myapplication.fragments.FragmentA
import com.cityfruit.myapplication.getBundle
import kotlinx.android.synthetic.main.bottom.*

class TabC : BaseLinkageFragment() {

    override fun getView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.bottom, container, false)
    }

    @Container
    var container: FrameLayout? = null

    override fun onCreate() {
        super.onCreate()
        container = bottom_fl
        startFragmentByNewTask(FragmentA::class.java, getBundle("bottomD 启动了 FrgA"), {
            ToastUtils.show(context, "it is already last in stack")
            false
        })
    }

}
