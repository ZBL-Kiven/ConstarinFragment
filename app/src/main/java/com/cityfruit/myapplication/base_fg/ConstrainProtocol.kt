@file:Suppress("unused")

package com.cityfruit.myapplication.base_fg

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.view.ViewGroup
import com.cityfruit.myapplication.base_fg.annotations.parser.AnnotationParser
import com.cityfruit.myapplication.base_fg.annotations.parser.ConstrainFragmentAnnotationParser
import com.cityfruit.myapplication.base_fg.annotations.Container
import com.cityfruit.myapplication.base_fg.fragments.BaseFragment
import com.cityfruit.myapplication.base_fg.fragments.BaseLinkageFragment
import com.cityfruit.myapplication.base_fg.fragments.ConstrainFragment
import com.cityfruit.myapplication.base_fg.managers.ConstrainFragmentManager
import java.security.InvalidParameterException

@Throws(InvalidParameterException::class, NullPointerException::class)
fun <T : ConstrainFragment> FragmentActivity.startFragment(fragmentCls: Class<T>, bundle: Bundle? = null, clearWhenEmptyStack: () -> Boolean, overrideTransaction: ((isHidden: Boolean, transaction: FragmentTransaction, curFragCls: Class<ConstrainFragment>) -> FragmentTransaction)? = null) {
    startFrag("", this, javaClass, supportFragmentManager, fragmentCls, bundle, clearWhenEmptyStack, overrideTransaction)
}

@Throws(InvalidParameterException::class, NullPointerException::class)
fun <T : ConstrainFragment> BaseFragment.startFragmentByNewTask(fragmentCls: Class<T>, bundle: Bundle? = null, clearWhenEmptyStack: () -> Boolean, overrideTransaction: ((isHidden: Boolean, transaction: FragmentTransaction, curFragCls: Class<ConstrainFragment>) -> FragmentTransaction)? = null) {
    val pid = if (this is BaseLinkageFragment) id else managerId
    startFrag(pid, this, javaClass, childFragmentManager, fragmentCls, bundle, clearWhenEmptyStack, overrideTransaction)
}


@Throws(InvalidParameterException::class, NullPointerException::class)
fun <T : ConstrainFragment> FragmentActivity.startFragment(fragmentCls: Class<T>, container: ViewGroup, bundle: Bundle? = null, clearWhenEmptyStack: () -> Boolean, overrideTransaction: ((isHidden: Boolean, transaction: FragmentTransaction, curFragCls: Class<ConstrainFragment>) -> FragmentTransaction)? = null) {
    startFrag("", container, supportFragmentManager, fragmentCls, bundle, clearWhenEmptyStack, overrideTransaction)
}

@Throws(InvalidParameterException::class, NullPointerException::class)
fun <T : ConstrainFragment> BaseFragment.startFragmentByNewTask(fragmentCls: Class<T>, container: ViewGroup, bundle: Bundle? = null, clearWhenEmptyStack: () -> Boolean, overrideTransaction: ((isHidden: Boolean, transaction: FragmentTransaction, curFragCls: Class<ConstrainFragment>) -> FragmentTransaction)? = null) {
    val pid = if (this is BaseLinkageFragment) id else managerId
    startFrag(pid, container, childFragmentManager, fragmentCls, bundle, clearWhenEmptyStack, overrideTransaction)
}

@Throws(InvalidParameterException::class, NullPointerException::class)
private fun <T : ConstrainFragment, R> startFrag(fragmentId: String, instance: R, cls: Class<R>, fragmentManager: FragmentManager, fragmentCls: Class<T>, bundle: Bundle? = null, clearWhenEmptyStack: () -> Boolean, overrideTransaction: ((isHidden: Boolean, transaction: FragmentTransaction, curFragCls: Class<ConstrainFragment>) -> FragmentTransaction)? = null) {
    val containers = AnnotationParser.parseField<Container>(cls)
    if (containers.size > 1) throw InvalidParameterException("the container must be only one in this class ,but ${containers.size} declared fond")
    val container = containers.firstOrNull() ?: throw java.lang.NullPointerException("the container was not found in the ")
    val view = container.first.get(instance) as? ViewGroup ?: throw InvalidParameterException("the container must be used in a ViewGroup")
    startFrag(fragmentId, view, fragmentManager, fragmentCls, bundle, clearWhenEmptyStack, overrideTransaction)
}

private fun <T : ConstrainFragment> startFrag(fragmentId: String, container: ViewGroup, fragmentManager: FragmentManager, fragmentCls: Class<T>, bundle: Bundle? = null, clearWhenEmptyStack: () -> Boolean, overrideTransaction: ((isHidden: Boolean, transaction: FragmentTransaction, curFragCls: Class<ConstrainFragment>) -> FragmentTransaction)? = null) {
    val manager = object : ConstrainFragmentManager(fragmentId, fragmentManager, container.id, clearWhenEmptyStack) {
        override fun beginTransaction(isHidden: Boolean, transaction: FragmentTransaction, frgCls: Class<ConstrainFragment>) {
            overrideTransaction?.invoke(isHidden, transaction, frgCls)
        }
    }
    val frg = ConstrainFragmentAnnotationParser.parseAnnotations(fragmentCls, bundle, manager)
    try {
        frg.setFragmentManager(manager).startFragment(frg)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
