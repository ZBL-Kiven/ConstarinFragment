package com.zj.cf.fragments

import android.view.View

@Suppress("validFragment")
abstract class BaseLinkageFragment : BaseFragment() {

    var linkageView: View? = null

    /**
     * @exception IllegalArgumentException the fid It is not recommended to be overwritten. If it is overwritten,
     * it must be ensured that the FID produced by each instance is unique.
     */
    override val fId: String; get() = super.fId
}