package com.cityfruit.myapplication.base_fg

import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.view.ViewGroup
import com.cityfruit.myapplication.base_fg.FMStore.getTopConstrainFragment
import com.cityfruit.myapplication.base_fg.annotations.AnnotationParser
import com.cityfruit.myapplication.base_fg.annotations.ConstrainFragmentAnnotationParser
import com.cityfruit.myapplication.base_fg.annotations.Container
import com.cityfruit.myapplication.base_fg.fragments.BaseFragment
import com.cityfruit.myapplication.base_fg.fragments.ConstrainFragment
import com.cityfruit.myapplication.base_fg.managers.BaseFragmentManager
import com.cityfruit.myapplication.base_fg.managers.ConstrainFragmentManager
import com.cityfruit.myapplication.base_fg.managers.FragmentHelper
import java.security.InvalidParameterException

internal object FMStore {

    data class ManagerInfo<F : BaseFragment>(var nextId: String?, val pId: String?, val manager: FragmentHelper<F>)

    val managers = mutableMapOf<String, ManagerInfo<*>>()

    fun <F : BaseFragment> putAManager(curManagerId: String?, manager: FragmentHelper<F>) {
        if (managers.contains(curManagerId)) managers[curManagerId]?.nextId = manager.managerId
        val managerInfo = ManagerInfo("", curManagerId, manager)
        managers[manager.managerId] = managerInfo
    }

    fun getTopConstrainFragment(managerId: String?): BaseFragment? {
        if (!managers.contains(managerId)) return null
        managers[managerId]?.let {
            val nextId = it.nextId
            val pid = it.pId
            val nextFrg = managers[nextId]
            when (it.manager) {
                is ConstrainFragmentManager -> {
                    nextFrg ?: return@getTopConstrainFragment it.manager.getCurrentFragment()
                    return@getTopConstrainFragment getTopConstrainFragment(nextId)
                }
                is BaseFragmentManager -> {
                    val previous = managers[pid]
                    if (nextFrg == null) {
                        return@getTopConstrainFragment when (previous) {
                            is ConstrainFragmentManager -> {
                                previous.getCurrentFragment()
                            }
                            is BaseFragmentManager -> {
                                getTopConstrainFragment(pid)
                            }
                            else -> null
                        }
                    } else {
                        return@getTopConstrainFragment getTopConstrainFragment(nextId)
                    }
                }
                else -> null
            }
        } ?: return null
    }
}

fun getTopFragment(managerId: String?): BaseFragment? {
    return getTopConstrainFragment(managerId)
}

/**
 * the fragment id generator , create a specially id and only decrypt on this class
 *
 * the I means index
 *
 * the M means manager_id
 *
 * */
private const val ID_DOT = "%s-I-%d-M-%s"

@Throws(NullPointerException::class)
internal fun <F : BaseFragment> generateId(id: String, manager: FragmentHelper<F>): String {
    var indexOfLast = -1
    manager.getFragmentIds()?.asReversed()?.forEach {
        if (it.contains(id)) {
            val simpled = getSimpleId(it)
            if (simpled.first == id && indexOfLast <= simpled.second) {
                indexOfLast = simpled.second + 1
            }
        }
    }
    return String.format(ID_DOT, id, indexOfLast, manager.managerId)
}

/**
 * get a parsed fragment id
 * */
@Throws(NullPointerException::class)
internal fun getSimpleId(id: String): Triple<String, Int, String> {
    val array = id.split("-I-|-M-".toRegex())
    try {
        return Triple(array[0], array[1].toInt(), array[2])
    } catch (e: Exception) {
        throw NullPointerException("can't parsed the fragment id, may id $id was wrong form generate")
    }
}


private fun <T : ConstrainFragment> startFrag(container: ViewGroup, fragmentManager: FragmentManager, fragmentCls: Class<T>, bundle: Bundle? = null, whenEmptyStack: () -> Unit, overrideTransaction: ((isHidden: Boolean, transaction: FragmentTransaction, curFragCls: Class<ConstrainFragment>) -> FragmentTransaction)? = null) {
    val manager = object : ConstrainFragmentManager(fragmentManager, container.id, whenEmptyStack) {
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

@Throws(InvalidParameterException::class, NullPointerException::class)
private fun <T : ConstrainFragment, R> startFrag(instance: R, cls: Class<R>, fragmentManager: FragmentManager, fragmentCls: Class<T>, bundle: Bundle? = null, whenEmptyStack: () -> Unit, overrideTransaction: ((isHidden: Boolean, transaction: FragmentTransaction, curFragCls: Class<ConstrainFragment>) -> FragmentTransaction)? = null) {
    val containers = AnnotationParser.parseField<Container>(cls)
    if (containers.size > 1) throw InvalidParameterException("the container must be only one in this class ,but ${containers.size} declared fond")
    val container = containers.firstOrNull()
    val view = container?.first?.get(instance) as? ViewGroup ?: throw InvalidParameterException("the container must be used in a ViewGroup")
    startFrag(view, fragmentManager, fragmentCls, bundle, whenEmptyStack, overrideTransaction)
}
