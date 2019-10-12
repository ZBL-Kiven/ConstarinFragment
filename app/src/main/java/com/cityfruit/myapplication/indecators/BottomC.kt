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
import com.zj.cf.log
import kotlinx.android.synthetic.main.bottom.*

class BottomC : BaseLinkageFragment() {
    override fun getView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.bottom, container, false)
    }

    @Container
    var container: FrameLayout? = null

    override fun onCreate() {
        super.onCreate()
        container = bottom_fl

        container?.setOnClickListener {
            initData()
        }
        initData()
    }

    private fun initData() {
        startFragmentByNewTask(FragmentA::class.java, getBundle("bottomC 启动了 FrgA"), {
            log("----- stack is empty")
            true
        })
    }
}
