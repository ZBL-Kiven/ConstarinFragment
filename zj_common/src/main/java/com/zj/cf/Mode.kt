package com.zj.cf

import android.util.Log
import com.zj.cf.BackMode.LASTING
import com.zj.cf.BackMode.ONLY_ONCE
import com.zj.cf.LaunchMode.CLEAR_BACK_STACK
import com.zj.cf.LaunchMode.FOLLOW
import com.zj.cf.LaunchMode.STACK

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
 * @property LASTING if the manager is running or activity was living , the fragment will exists in long at stack
 * */
object BackMode {
    const val ONLY_ONCE = 1
    const val LASTING = 2
}

fun log(s: String) {
    Log.e("----- ", s)
}

