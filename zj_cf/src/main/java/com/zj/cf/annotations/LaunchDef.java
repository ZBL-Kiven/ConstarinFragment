package com.zj.cf.annotations;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.zj.cf.LaunchMode.CLEAR_BACK_STACK;
import static com.zj.cf.LaunchMode.FOLLOW;
import static com.zj.cf.LaunchMode.STACK;

/**
 * created by zjj on 19.05.14
 *
 * @see com.zj.cf.LaunchMode ,the start mode.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@IntDef(value = {STACK, FOLLOW, CLEAR_BACK_STACK})
@interface LaunchDef {
}