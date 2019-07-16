package com.cityfruit.myapplication.base_fg

import com.cityfruit.myapplication.base_fg.fragments.BaseFragment
import com.cityfruit.myapplication.base_fg.managers.BaseFragmentManager
import com.cityfruit.myapplication.base_fg.managers.ConstrainFragmentManager
import com.cityfruit.myapplication.base_fg.managers.FragmentHelper

internal object FMStore {

    data class ManagerInfo<F : BaseFragment>(var nextId: String?, val pId: String?, val manager: FragmentHelper<F>)

    private val managers = mutableMapOf<String, ManagerInfo<*>>()

    /**
     * @param key if called object was a linkage fragment instance, the pid is managerId but the fragment id is the mapping keys
     * */
    fun <F : BaseFragment> putAManager(curManagerId: String?, manager: FragmentHelper<F>, key: String = "") {
        if (key.isEmpty()) {
            if (!curManagerId.isNullOrEmpty() && managers.contains(curManagerId)) managers[curManagerId]?.nextId = manager.managerId
            val managerInfo = ManagerInfo("", curManagerId, manager)
            managers[manager.managerId] = managerInfo
        } else {
            val managerInfo = ManagerInfo("", curManagerId, manager)
            managers[key] = managerInfo
        }
    }

    fun removeAManager(managerId: String?) {
        if (managers.contains(managerId)) {
            val curFinishingFrg = managers[managerId] ?: return
            managers[curFinishingFrg.pId]?.let {
                it.nextId = ""
            }
            managers.remove(managerId)
        }
    }

    /**
     * get topic for BaseFragmentManager and ConstrainFragmentManager
     *
     * @param ase don't set it in called ,it used by recursive
     */
    fun getTopConstrainFragment(managerId: String?, ase: Boolean = false): BaseFragment? {
        if (!managers.contains(managerId)) return null
        managers[managerId]?.let {
            when (it.manager) {
                is ConstrainFragmentManager -> {
                    val next = if (ase) it.pId else it.nextId
                    val nextFrg = managers[next]
                    if (nextFrg == null || ase) {
                        return@getTopConstrainFragment it.manager.getCurrentFragment()
                    }
                    return@getTopConstrainFragment getTopConstrainFragment(next)
                }
                is BaseFragmentManager -> {
                    val nextId = if (ase) it.pId else managers[it.manager.getCurrentItemId()]?.nextId
                    val nextFrg = managers[nextId]
                    if (!ase && nextFrg == null) {
                        return@getTopConstrainFragment getTopConstrainFragment(it.manager.managerId, true)
                    }
                    return if (ase && nextFrg == null) null
                    else getTopConstrainFragment(nextId)
                }
                else -> null
            }
        } ?: return null
    }
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
