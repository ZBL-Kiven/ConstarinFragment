package com.zj.cf.fragments

abstract class BaseTabFragment : BaseFragment() {
    /**
     * @exception IllegalArgumentException the fid It is not recommended to be overwritten. If it is overwritten,
     * it must be ensured that the FID produced by each instance is unique.
     */
    override val fId: String; get() = super.fId
}