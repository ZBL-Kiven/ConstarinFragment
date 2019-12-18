package com.zj.cf.annotations;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * created by zjj on 19.05.14
 *
 * @see ConstrainMode ,the start mode.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@IntDef(value = {3, 4, 5})
@interface LaunchDef {
}