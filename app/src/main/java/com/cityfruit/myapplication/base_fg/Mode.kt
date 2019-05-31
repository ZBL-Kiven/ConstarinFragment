package com.cityfruit.myapplication.base_fg

import android.util.Log
import android.view.View
import com.cityfruit.myapplication.base_fg.fragments.BaseLinkageFragment
import com.cityfruit.myapplication.base_fg.managers.ConstrainFragmentManager
import kotlin.NullPointerException

/**
 * created by zjj on 19.05.14
 *
 * the mode of stack trace for open a fragment
 *
 * @property STACK this fragment will back by an ordered stack,for added, it'll remove all task stack on the top of self
 *
 * @property FOLLOW liked STACK , but it won't remove any task stack , although the same one in the bottom of self
 *
 * @property CLEAR_BACK_STACK never created backed stack , only back to home when it closed .@see ConstrainHome
 * */
object LaunchMode {
    const val STACK = 1
    const val FOLLOW = 2
    const val CLEAR_BACK_STACK = 3
}

/**
 * created by zjj on 19.05.14
 *
 * the lifecycle with fragment
 *
 * @property ONLY_ONCE this fragment only created when used , and'll destroyed when close
 *
 * @property LASTING if the manager is running or activity was living , the fragment will exists in long
 * */
object BackMode {
    const val ONLY_ONCE = 1
    const val LASTING = 2
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
internal fun generateId(id: String, manager: ConstrainFragmentManager): String {
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

fun log(s: String) {
    Log.e("----- ", s)
}

