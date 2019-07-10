package com.cityfruit.myapplication.base_fg.annotations;

import java.lang.annotation.*;

/**
 * created by zjj on 19.05.14
 * <p>
 * the fragments container ,
 * <p>
 * it was finally and Unique in the same ConstrainFragmentManager,
 * <p>
 * Multiple statements constrain Home maybe conflict and crash
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Container {
}