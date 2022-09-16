@file:Suppress("unused")

package com.zj.cf

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.zj.cf.annotations.Container
import com.zj.cf.annotations.parser.AnnotationParser
import com.zj.cf.annotations.parser.ConstrainFragmentAnnotationParser
import com.zj.cf.fragments.BaseFragment
import com.zj.cf.fragments.BaseLinkageFragment
import com.zj.cf.fragments.ConstrainFragment
import com.zj.cf.managers.ConstrainFragmentManager
import com.zj.cf.unitive.Lifecycle
import java.security.InvalidParameterException

@Throws(InvalidParameterException::class, NullPointerException::class)
fun <T : ConstrainFragment> AppCompatDialogFragment.startFragment(fragmentCls: Class<T>, container: ViewGroup, bundle: Bundle? = null, clearWhenEmptyStack: () -> Boolean, overrideTransaction: ((isHidden: Boolean, transaction: FragmentTransaction, curFragCls: Class<ConstrainFragment>) -> FragmentTransaction)? = null): ConstrainFragmentManager {
    return startFrag(requireActivity(), "", container, javaClass, childFragmentManager, fragmentCls, bundle, clearWhenEmptyStack, overrideTransaction)
}

@Throws(InvalidParameterException::class, NullPointerException::class)
fun <T : ConstrainFragment> AppCompatDialogFragment.startFragment(fragmentCls: Class<T>, bundle: Bundle? = null, clearWhenEmptyStack: () -> Boolean, overrideTransaction: ((isHidden: Boolean, transaction: FragmentTransaction, curFragCls: Class<ConstrainFragment>) -> FragmentTransaction)? = null): ConstrainFragmentManager {
    return startFrag(requireActivity(), "", this, javaClass, childFragmentManager, fragmentCls, bundle, clearWhenEmptyStack, overrideTransaction)
}

@Throws(InvalidParameterException::class, NullPointerException::class)
fun <T : ConstrainFragment> FragmentActivity.startFragment(fragmentCls: Class<T>, bundle: Bundle? = null, clearWhenEmptyStack: () -> Boolean, overrideTransaction: ((isHidden: Boolean, transaction: FragmentTransaction, curFragCls: Class<ConstrainFragment>) -> FragmentTransaction)? = null): ConstrainFragmentManager {
    return startFrag(this, "", this, javaClass, supportFragmentManager, fragmentCls, bundle, clearWhenEmptyStack, overrideTransaction)
}

@Throws(InvalidParameterException::class, NullPointerException::class)
fun <T : ConstrainFragment> BaseFragment.startFragmentByNewTask(fragmentCls: Class<T>, bundle: Bundle? = null, clearWhenEmptyStack: () -> Boolean, overrideTransaction: ((isHidden: Boolean, transaction: FragmentTransaction, curFragCls: Class<ConstrainFragment>) -> FragmentTransaction)? = null): ConstrainFragmentManager {
    val pid = if (this is BaseLinkageFragment) fId else managerId
    return startFrag(requireActivity(), pid, this, javaClass, childFragmentManager, fragmentCls, bundle, clearWhenEmptyStack, overrideTransaction)
}

@Throws(InvalidParameterException::class, NullPointerException::class)
fun <T : ConstrainFragment> FragmentActivity.startFragment(fragmentCls: Class<T>, container: ViewGroup, bundle: Bundle? = null, clearWhenEmptyStack: () -> Boolean, overrideTransaction: ((isHidden: Boolean, transaction: FragmentTransaction, curFragCls: Class<ConstrainFragment>) -> FragmentTransaction)? = null): ConstrainFragmentManager {
    return startFrag(this, "", container, supportFragmentManager, fragmentCls, bundle, clearWhenEmptyStack, overrideTransaction)
}

@Throws(InvalidParameterException::class, NullPointerException::class)
fun <T : ConstrainFragment> BaseFragment.startFragmentByNewTask(fragmentCls: Class<T>, container: ViewGroup, bundle: Bundle? = null, clearWhenEmptyStack: () -> Boolean, overrideTransaction: ((isHidden: Boolean, transaction: FragmentTransaction, curFragCls: Class<ConstrainFragment>) -> FragmentTransaction)? = null): ConstrainFragmentManager {
    val pid = if (this is BaseLinkageFragment) fId else managerId
    return startFrag(requireActivity(), pid, container, childFragmentManager, fragmentCls, bundle, clearWhenEmptyStack, overrideTransaction)
}

@Throws(InvalidParameterException::class, NullPointerException::class)
private fun <T : ConstrainFragment, R> startFrag(act: FragmentActivity, fragmentId: String, instance: R, cls: Class<R>, fragmentManager: FragmentManager, fragmentCls: Class<T>, bundle: Bundle? = null, clearWhenEmptyStack: () -> Boolean, overrideTransaction: ((isHidden: Boolean, transaction: FragmentTransaction, curFragCls: Class<ConstrainFragment>) -> FragmentTransaction)? = null): ConstrainFragmentManager {
    val containers = AnnotationParser.parseField<Container>(cls)
    if (containers.size > 1) throw InvalidParameterException("the container must be only one in this class ,but ${containers.size} declared fond")
    val container = containers.firstOrNull() ?: throw java.lang.NullPointerException("the container was not found in the ")
    val view = container.first.get(instance) as? ViewGroup ?: throw InvalidParameterException("the container must be used in a ViewGroup")
    return startFrag(act, fragmentId, view, fragmentManager, fragmentCls, bundle, clearWhenEmptyStack, overrideTransaction)
}

private fun <T : ConstrainFragment> startFrag(act: FragmentActivity, fragmentId: String, container: ViewGroup, fragmentManager: FragmentManager, fragmentCls: Class<T>, bundle: Bundle? = null, clearWhenEmptyStack: () -> Boolean, overrideTransaction: ((isHidden: Boolean, transaction: FragmentTransaction, curFragCls: Class<ConstrainFragment>) -> FragmentTransaction)? = null): ConstrainFragmentManager {
    val manager = object : ConstrainFragmentManager(act, fragmentId, fragmentManager, container.id, clearWhenEmptyStack) {
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
    return manager
}

internal var lifecycleCallback: ((Lifecycle, String, String) -> Unit)? = null

fun setConstrainFragmentLifecycleCallBack(callBack: (Lifecycle, String, String) -> Unit) {
    lifecycleCallback = callBack
}
