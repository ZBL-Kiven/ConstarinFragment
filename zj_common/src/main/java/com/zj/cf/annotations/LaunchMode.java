package com.zj.cf.annotations;

import java.lang.annotation.*;

/**
 * created by zjj on 19.05.14
 *
 * @code mode the stack type for this fragment in manager
 * @see LaunchDef
 */

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LaunchMode {
    @LaunchDef int mode();
}
