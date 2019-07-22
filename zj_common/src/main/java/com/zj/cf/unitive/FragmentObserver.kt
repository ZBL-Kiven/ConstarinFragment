package com.zj.cf.unitive

import android.support.v4.app.Fragment

/**
 * Created by zjj on 19.05.14.
 */

interface FragmentObserver {
    fun beforeHiddenChange(triggerFragment: Fragment, isHidden: Boolean, stateChange: () -> Unit)
}
