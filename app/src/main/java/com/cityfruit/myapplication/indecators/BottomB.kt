package com.cityfruit.myapplication.indecators

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cityfruit.myapplication.R
import com.cityfruit.myapplication.ToastUtils
import com.zj.cf.annotations.Container
import com.zj.cf.fragments.BaseLinkageFragment
import com.zj.cf.startFragmentByNewTask
import com.cityfruit.myapplication.fragments.FragmentA
import com.cityfruit.myapplication.getBundle

class BottomB : BaseLinkageFragment() {

    override val fId: String; get() = "bottom_B" + super.fId

    override fun getView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.bottom, container, false)
    }


    @Container
    var container: ViewGroup? = null

    override fun onFragmentCreated() {
        super.onFragmentCreated()
        container = rootView as? ViewGroup
        startFragmentByNewTask(FragmentA::class.java, getBundle("bottomB 启动了 FrgA"), {
            ToastUtils.show(context, "it is already last in stack")
            false
        })
    }
}
