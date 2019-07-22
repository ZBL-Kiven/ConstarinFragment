package com.zj.cf.annotations;

import java.lang.annotation.*;

/**
 * created by zjj on 19.05.14
 * <p>
 * the Constrain for ConstrainFragmentManager
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Constrain {

    /**
     * the id for this ,and it must be unique for the the same manager
     */
    String id();

    /**
     * @see BackDef
     */
    @BackDef int backMode();
}