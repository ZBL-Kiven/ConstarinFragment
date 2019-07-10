package com.cityfruit.myapplication.base_fg.annotations;

import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.cityfruit.myapplication.base_fg.LaunchMode.CLEAR_BACK_STACK;
import static com.cityfruit.myapplication.base_fg.LaunchMode.FOLLOW;
import static com.cityfruit.myapplication.base_fg.LaunchMode.STACK;

/**
 * created by zjj on 19.05.14
 *
 * @see LaunchMode ,the start mode.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@IntDef(value = {STACK, FOLLOW, CLEAR_BACK_STACK})
@interface LaunchDef {
}