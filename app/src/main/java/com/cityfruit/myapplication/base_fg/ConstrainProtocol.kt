@file:Suppress("unused")

package com.cityfruit.myapplication.base_fg

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.view.ViewGroup
import com.cityfruit.myapplication.base_fg.annotations.AnnotationParser
import com.cityfruit.myapplication.base_fg.annotations.ConstrainFragmentAnnotationParser
import com.cityfruit.myapplication.base_fg.annotations.Container
import com.cityfruit.myapplication.base_fg.fragments.BaseFragment
import com.cityfruit.myapplication.base_fg.fragments.ConstrainFragment
import com.cityfruit.myapplication.base_fg.managers.ConstrainFragmentManager
import java.lang.NullPointerException
import java.security.InvalidParameterException

@Throws(InvalidParameterException::class, NullPointerException::class)
fun <T : ConstrainFragment> FragmentActivity.startFragment(fragmentCls: Class<T>, bundle: Bundle? = null, whenEmptyStack: () -> Unit, overrideTransaction: ((transaction: FragmentTransaction, curShowId: String, mFragments: MutableMap<String, ConstrainFragment>) -> Unit)? = null) {
    startFrag(this, javaClass, supportFragmentManager, fragmentCls, bundle, whenEmptyStack, overrideTransaction)
}

@Throws(InvalidParameterException::class, NullPointerException::class)
fun <T : ConstrainFragment> BaseFragment.startFragmentByNewTask(fragmentCls: Class<T>, bundle: Bundle? = null, whenEmptyStack: () -> Unit, overrideTransaction: ((transaction: FragmentTransaction, curShowId: String, mFragments: MutableMap<String, ConstrainFragment>) -> Unit)? = null) {
    startFrag(this, javaClass, childFragmentManager, fragmentCls, bundle, whenEmptyStack, overrideTransaction)
}

@Throws(InvalidParameterException::class, NullPointerException::class)
private fun <T : ConstrainFragment, R> startFrag(instance: R, cls: Class<R>, fragmentManager: FragmentManager, fragmentCls: Class<T>, bundle: Bundle? = null, whenEmptyStack: () -> Unit, overrideTransaction: ((transaction: FragmentTransaction, curShowId: String, mFragments: MutableMap<String, ConstrainFragment>) -> Unit)? = null) {
    val containers = AnnotationParser.parseField<Container>(cls)
    if (containers.size > 1) throw InvalidParameterException("the container must be only one in this class ,but ${containers.size} declared fond")
    val container = containers.firstOrNull()
    val view = container?.first?.get(instance) as? ViewGroup ?: throw InvalidParameterException("the container must be used in a ViewGroup")
    val manager = object : ConstrainFragmentManager(fragmentManager, view.id, whenEmptyStack) {
        override fun beginTransaction(transaction: FragmentTransaction, curShowId: String, mFragments: MutableMap<String, ConstrainFragment>) {
            overrideTransaction?.invoke(transaction, curShowId, mFragments)
        }
    }
    val frg = ConstrainFragmentAnnotationParser.parseAnnotations(fragmentCls, bundle, manager)
    try {
        frg.setFragmentManager(manager).startFragment(frg)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}