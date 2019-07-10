package com.cityfruit.myapplication.base_fg.annotations;

import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.cityfruit.myapplication.base_fg.BackMode.LASTING;
import static com.cityfruit.myapplication.base_fg.BackMode.ONLY_ONCE;

/**
 * created by zjj on 19.05.14
 *
 * @see com.cityfruit.myapplication.base_fg.BackMode ,the backed mode.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@IntDef(value = {LASTING, ONLY_ONCE})
@interface BackDef {
}