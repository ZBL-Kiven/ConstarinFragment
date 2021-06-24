package com.cityfruit.myapplication.tabs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cityfruit.myapplication.R
import com.zj.cf.fragments.BaseTabFragment

class FragmentTab1 : BaseTabFragment() {

    override val fId: String; get() = "TAB_2"

    override fun getView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.tab_item, container, false)
    }
}