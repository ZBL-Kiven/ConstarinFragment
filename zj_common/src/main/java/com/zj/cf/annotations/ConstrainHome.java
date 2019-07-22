package com.zj.cf.annotations;

import java.lang.annotation.*;

/**
 * created by zjj on 19.05.14
 * <p>
 * the home of constrain,
 * <p>
 * it was finally and unique in the same ConstrainFragmentManager,
 * <p>
 * Multiple statements constrain Home maybe conflict and crash
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConstrainHome {
}