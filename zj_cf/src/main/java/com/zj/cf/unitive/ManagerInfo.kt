package com.zj.cf.unitive

import com.zj.cf.fragments.BaseFragment
import com.zj.cf.managers.FragmentHelper

internal data class ManagerInfo<F : BaseFragment>(var nextId: String?, val pId: String?, val manager: FragmentHelper<F>) {
    override fun toString(): String {
        return "{\"pid\" = \"$pId\", \"nextId\" = \"$nextId\",  \"fhi\" = \"${manager.managerId}\",  \"fhCount\" = \"${manager.getFragments()?.size}\"}"
    }
}