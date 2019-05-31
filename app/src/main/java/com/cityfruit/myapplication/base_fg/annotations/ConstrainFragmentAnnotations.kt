package com.cityfruit.myapplication.base_fg.annotations

import android.support.annotation.IntDef
import com.cityfruit.myapplication.base_fg.BackMode
import com.cityfruit.myapplication.base_fg.BackMode.LASTING
import com.cityfruit.myapplication.base_fg.BackMode.ONLY_ONCE
import com.cityfruit.myapplication.base_fg.LaunchMode
import com.cityfruit.myapplication.base_fg.LaunchMode.CLEAR_BACK_STACK
import com.cityfruit.myapplication.base_fg.LaunchMode.FOLLOW
import com.cityfruit.myapplication.base_fg.LaunchMode.STACK
import java.lang.annotation.Inherited

/**
 * created by zjj on 19.05.14
 *
 * the home of constrain,
 *
 * it was finally and unique in the same ConstrainFragmentManager,
 *
 * Multiple statements constrain Home maybe conflict and crash
 * */
@Inherited
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ConstrainHome

/**
 * created by zjj on 19.05.14
 *
 * the Constrain for ConstrainFragmentManager
 *
 * @param id the id for this ,and it must be unique for the the same manager
 *
 * @see BackDef
 * */
@Inherited
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Constrain(val id: String, @BackDef val backMode: Int)

/**
 * created by zjj on 19.05.14
 *
 * @param mode the stack type for this fragment in manager
 * @see LaunchDef
 * */
@Inherited
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class LaunchMode(@LaunchDef val mode: Int)

/**
 * created by zjj on 19.05.14
 *
 * the fragments container ,
 *
 * it was finally and Unique in the same ConstrainFragmentManager,
 *
 * Multiple statements constrain Home maybe conflict and crash
 * */
@Inherited
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Container

/**
 * created by zjj on 19.05.14
 *
 * @see BackMode ,the backed mode.
 * */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
@IntDef(LASTING, ONLY_ONCE)
annotation class BackDef

/**
 * created by zjj on 19.05.14
 *
 * @see LaunchMode ,the start mode.
 * */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
@IntDef(STACK, FOLLOW, CLEAR_BACK_STACK)
annotation class LaunchDef