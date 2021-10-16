package com.zj.cf.unitive

import androidx.fragment.app.FragmentTransaction
import com.zj.cf.fragments.BaseFragment

/**
 * Created by zjj on 19.05.14.
 */
interface FragmentOperator<F : BaseFragment> {

    fun beginTransaction(isHidden: Boolean, transaction: FragmentTransaction, frgCls: Class<F>)

    fun syncSelectState(selectId: String)

    fun whenShowSameFragment(formUser: Boolean, shownId: String)

    fun whenShowNotSameFragment(formUser: Boolean, shownId: String, lastId: String): Boolean
}
