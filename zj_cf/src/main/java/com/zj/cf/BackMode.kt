package com.zj.cf
/**
 * created by zjj on 19.05.14
 *
 * the lifecycle with fragment
 *
 * @property ONLY_ONCE the fragment has created only by used , and it'll destroy when close
 *
 * @property LASTING if the manager is running or activity was living , the fragment will exists in long at stack
 * */
object BackMode {
    const val ONLY_ONCE = 1
    const val LASTING = 2
}

