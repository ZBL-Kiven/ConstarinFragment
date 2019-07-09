package com.cityfruit.myapplication.indecators

import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.cityfruit.myapplication.R
import com.cityfruit.myapplication.base_fg.annotations.Container
import com.cityfruit.myapplication.base_fg.fragments.BaseLinkageFragment
import com.cityfruit.myapplication.base_fg.managers.BaseFragmentManager
import com.cityfruit.myapplication.base_fg.startFragmentByNewTask
import com.cityfruit.myapplication.fragments.FragmentD
import kotlinx.android.synthetic.main.fragment_b.*

class BottomA : BaseLinkageFragment() {

    @Container
    var contrainer: ViewGroup? = null

    override fun getView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.fragment_a, container, false)
    }

    override fun initView() {
        contrainer = fragment_container
        btn01.setOnClickListener {
            contrainer?.visibility = VISIBLE
            startFragmentByNewTask(FragmentD::class.java, null, { contrainer?.removeAllViews()})
        }
    }

    override fun initData() {
        object : BaseFragmentManager(this, R.id.fragment_container, 0, ll, BottomB(), BottomC(), BottomD()) {}
    }
}
