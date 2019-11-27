package com.zj.cf.fragments

import android.view.View

@Suppress("validFragment")
abstract class BaseLinkageFragment : BaseFragment() {

    var linkageView: View? = null

    final override val fId: String
        get() = super.fId
}