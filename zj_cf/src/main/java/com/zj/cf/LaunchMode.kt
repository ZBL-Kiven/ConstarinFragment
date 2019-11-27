package com.zj.cf
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