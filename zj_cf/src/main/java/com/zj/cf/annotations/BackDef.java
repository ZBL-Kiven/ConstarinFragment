package com.zj.cf.annotations;

import androidx.annotation.IntDef;
import com.zj.cf.BackMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.zj.cf.BackMode.LASTING;
import static com.zj.cf.BackMode.ONLY_ONCE;

/**
 * created by zjj on 19.05.14
 *
 * @see BackMode ,the backed mode.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@IntDef(value = {LASTING, ONLY_ONCE})
@interface BackDef {
}