package com.zj.cf

import com.zj.cf.fragments.BaseFragment
import com.zj.cf.managers.BaseFragmentManager
import com.zj.cf.managers.ConstrainFragmentManager
import com.zj.cf.managers.FragmentHelper
import com.zj.cf.managers.TabFragmentManager
import com.zj.cf.unitive.ManagerInfo
import java.lang.IllegalArgumentException
import java.lang.StringBuilder

object FMStore {

    private val managers = mutableMapOf<String, ManagerInfo<*>>()

    /**
     * @param key if called object was a linkage fragment instance, the pid is managerId but the fragment fId is the mapping keys
     * */
    fun <F : BaseFragment> putAManager(curManagerId: String?, manager: FragmentHelper<F>, key: String = "") {
        if (manager is BaseFragmentManager) {
            val last = managers[curManagerId]
            if (!last?.nextId.isNullOrEmpty()) {
                throw IllegalAccessException("too many BaseFragmentManagers declared in same container")
            }
        }
        if (key.isEmpty()) {
            if (!curManagerId.isNullOrEmpty() && managers.contains(curManagerId)) managers[curManagerId]?.nextId = manager.managerId
            val managerInfo = ManagerInfo("", curManagerId, manager)
            managers[manager.managerId] = managerInfo
        } else {
            val managerInfo = ManagerInfo("", curManagerId, manager)
            managers[key] = managerInfo
        }
        try {
            initLifecycle()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        println("a-----  key  =  $key   mid = ${manager.managerId}")
    }

    fun removeManager(managerId: String?) {

        val removeList = mutableSetOf<String>()

        fun remove(managerId: String?, layer: Int = 0) {

            fun removeLinkageManager(manager: BaseFragmentManager, nextId: String? = null) {
                nextId?.let { n -> removeList.add(n) }
                manager.getFragmentIds()?.forEach { s ->
                    manager.removeFragmentById(s)
                    managers[s]?.let { m2 ->
                        removeList.add(s)
                        remove(m2.nextId, layer + 1)
                    }
                }
            }

            fun removeTabManager(manager: TabFragmentManager<*, *>, nextId: String? = null) {
                nextId?.let { n -> removeList.add(n) }
                manager.getFragmentIds()?.forEach { s ->
                    manager.removeFragmentById(s)
                    managers[s]?.let { m2 ->
                        removeList.add(s)
                        remove(m2.nextId, layer + 1)
                    }
                }
            }

            fun removeConstrainManager(manager: ConstrainFragmentManager, nextId: String? = null) {
                manager.getFragmentIds()?.forEach {
                    manager.removeFragmentById(it)
                }
                remove(nextId, layer + 1)
            }

            managers[managerId]?.let {
                removeList.add(it.manager.managerId)
                when (it.manager) {
                    is BaseFragmentManager -> {
                        removeLinkageManager(it.manager, it.nextId)
                    }
                    is ConstrainFragmentManager -> {
                        removeConstrainManager(it.manager, it.nextId)
                    }
                    is TabFragmentManager<*, *> -> {
                        removeTabManager(it.manager, it.nextId)
                    }
                    else -> throw IllegalArgumentException("unknown type formatted !!")
                }
            }
        }
        remove(managerId)
        removeList.forEach {
            managers.remove(it)
        }
    }

    fun removeManageWithFrgId(fid: String) {
        managers.remove(fid)
    }

    fun hasManager(managerId: String): Boolean {
        return managers.containsKey(managerId)
    }

    fun getManagerByLevel(managerId: String?, level: Int): FragmentHelper<*>? {
        if (managers.contains(managerId)) {
            var curFinishingFrg: ManagerInfo<*>? = managers[managerId] ?: return null
            for (i in when {
                level > 0 -> 0 until level
                level < 0 -> level until 0
                else -> 0..0
            }) {
                val id = if (i > 0) curFinishingFrg?.nextId else curFinishingFrg?.pId
                if ((i > 0 && i == level - 1) || (i < 0 && i == -1)) {
                    return managers[id]?.manager
                }
                curFinishingFrg = managers[id] ?: return null
            }
        }
        return null
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
                    return@getTopConstrainFragment getTopConstrainFragment(next, ase)
                }
                is BaseFragmentManager -> {
                    val nextId = if (ase) it.pId else managers[it.manager.getCurrentItemId()]?.nextId
                    val nextFrg = managers[nextId]
                    if (!ase && nextFrg == null) {
                        return@getTopConstrainFragment getTopConstrainFragment(it.manager.managerId, true)
                    }
                    return if (ase && nextFrg == null) null
                    else getTopConstrainFragment(nextId, ase)
                }
                else -> null
            }
        } ?: return null
    }

    fun checkIsConstrainParent(id: String): Boolean {

        fun findLastConstrainOrNull(id: String?): Boolean {
            val curManager = managers[id]
            val manage = managers[curManager?.pId]
            val pid = if (manage?.manager is BaseFragmentManager) managers[manage.manager.managerId]?.pId
            else manage?.pId
            val lastFrag = managers[pid] ?: return false
            return when (val lastFragmentManager = lastFrag.manager) {
                is BaseFragmentManager -> {
                    findLastConstrainOrNull(pid)
                }
                is ConstrainFragmentManager -> {
                    val cf = lastFragmentManager.getCurrentFragment()
                    if (manage?.manager is BaseFragmentManager) cf?.finish()
                    return true
                }
                else -> false
            }
        }
        return findLastConstrainOrNull(id)
    }

    fun getManagersInfo(): String {
        try {
            val total = managers.size
            val sb = StringBuilder("{").append("\"totalManagers\":${total},\"fgs\":[")
            managers.forEach { (k, v) ->
                val vs = "{\"mId\":\"${v.manager.managerId}\",\"pid\":\"${v.pId}\",\"nextId\":\"${v.nextId}\"}"
                sb.append("{\"k\":\"$k\",\"v\":$vs},\n")
            }
            if (sb.endsWith(",")) sb.delete(sb.length - 1, sb.length)
            sb.append("]}")
            return sb.toString()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * the fragment fId generator , create a specially fId and only decrypt on this class
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
     * get a parsed fragment fId
     * */
    @Throws(NullPointerException::class)
    internal fun getSimpleId(id: String): Triple<String, Int, String> {
        val array = id.split("-I-|-M-".toRegex())
        try {
            return Triple(array[0], array[1].toInt(), array[2])
        } catch (e: Exception) {
            throw NullPointerException("can't parsed the fragment fId, may fId $id was wrong form generate")
        }
    }


    private fun initLifecycle() {

    }
}

