package com.zj.cf.annotations;

@SuppressWarnings({"WeakerAccess", "unused"})
public class ConstrainMode {
    /**
     * created by zjj on 19.05.14
     * <p>
     * the lifecycle with fragment
     *
     * @see #CONST_ONLY_ONCE the fragment has created only by used , and it'll destroy when close
     * @see #CONST_LASTING if the manager is running or activity was living , the fragment will exists in long at stack
     */
    public static int CONST_ONLY_ONCE = 1;
    public static int CONST_LASTING = 2;

    /**
     * created by zjj on 19.05.14
     * <p>
     * the mode of stack trace for open a fragment
     *
     * @see #STACK this fragment will back by an ordered stack,for added, it'll remove all task stack on the top of self
     * @see #FOLLOW liked STACK , but it won't remove any task stack , although the same one in the bottom of self
     * @see #CLEAR_BACK_STACK never created backed stack , only back to home when it closed .@see ConstrainHome
     */
    public static int STACK = 3;
    public static int FOLLOW = 4;
    public static int CLEAR_BACK_STACK = 5;
}
