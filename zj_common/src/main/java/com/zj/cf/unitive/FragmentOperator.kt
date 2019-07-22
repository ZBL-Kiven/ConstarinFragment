package com.zj.cf.unitive

import android.support.v4.app.FragmentTransaction
import com.zj.cf.fragments.BaseFragment

/**
 * Created by zjj on 19.05.14.
 */
interface FragmentOperator<F : BaseFragment> {

    fun beginTransaction(isHidden: Boolean, transaction: FragmentTransaction, frgCls: Class<F>)

    fun syncSelectState(selectId: String)

    fun whenShowSameFragment(shownId: String)

    fun whenShowNotSameFragment(shownId: String): Boolean
}
