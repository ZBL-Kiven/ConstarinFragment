package com.cityfruit.myapplication.tabs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cityfruit.myapplication.R
import com.zj.cf.fragments.BaseTabFragment

class FragmentTab(private val p: Int) : BaseTabFragment() {

    override val fId: String; get() = "TAB_$p"

    override fun getView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.tab_item, container, false)
    }

    override fun onCreate() {

    }
}