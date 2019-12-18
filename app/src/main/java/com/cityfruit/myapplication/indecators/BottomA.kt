package com.cityfruit.myapplication.indecators

import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.cityfruit.myapplication.R
import com.cityfruit.myapplication.ToastUtils
import com.cityfruit.myapplication.addOnClickListener
import com.zj.cf.annotations.Container
import com.zj.cf.fragments.BaseLinkageFragment
import com.zj.cf.managers.BaseFragmentManager
import com.zj.cf.startFragmentByNewTask
import com.cityfruit.myapplication.fragments.FragmentD
import com.cityfruit.myapplication.tabs.TabA
import com.cityfruit.myapplication.tabs.TabB
import com.cityfruit.myapplication.tabs.TabC
import kotlinx.android.synthetic.main.fragment_b.*

class BottomA : BaseLinkageFragment() {

    @Container
    var container: ViewGroup? = null

    override fun getView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.fragment_b, container, false)
    }

    override fun onCreate() {
        super.onCreate()
        container = fragment_container
        btn01.addOnClickListener {
            container?.visibility = VISIBLE
            startFragmentByNewTask(FragmentD::class.java, null, {
                ToastUtils.show(context, "it is already last in stack")
                false
            })
        }
//        object : BaseFragmentManager(this, R.id.fragment_container, 0, ll, TabA(), TabB(), TabC()) {
//
//        }
    }
}
