package com.cityfruit.myapplication.base_fg.annotations

import android.os.Bundle
import com.cityfruit.myapplication.base_fg.BackMode
import com.cityfruit.myapplication.base_fg.LaunchMode.FOLLOW
import com.cityfruit.myapplication.base_fg.fragments.ConstrainFragment
import com.cityfruit.myapplication.base_fg.generateId
import com.cityfruit.myapplication.base_fg.managers.ConstrainFragmentManager
import com.cityfruit.myapplication.base_fg.unitive.ProxyManager
import java.lang.NullPointerException

internal object ConstrainFragmentAnnotationParser {

    @Throws(NullPointerException::class)
    fun <T : ConstrainFragment> parseAnnotations(fragmentCls: Class<T>, bundle: Bundle?, fragmentManager: ConstrainFragmentManager?): ProxyManager<T> {
        val launchModeParse = AnnotationParser.parseCls<LaunchMode>(fragmentCls)
        val constrainHomeParse = AnnotationParser.parseCls<ConstrainHome>(fragmentCls)
        val launchMode = launchModeParse?.mode ?: FOLLOW
        val isHome = constrainHomeParse != null
        val constrainParser = AnnotationParser.parseCls<Constrain>(fragmentCls)
        val backMode = constrainParser?.backMode ?: BackMode.ONLY_ONCE
        val fid = generateId(constrainParser?.id ?: throw NullPointerException("the fragment id was not found,did you forgot annotation : Constrain( id = \"XXX\") in your ${fragmentCls.simpleName}.class?"), fragmentManager ?: throw NullPointerException("bad request ! you still haven’t a manager yet!"))
        return ProxyManager(fragmentCls, fid, backMode, launchMode, isHome, bundle)
    }
}


