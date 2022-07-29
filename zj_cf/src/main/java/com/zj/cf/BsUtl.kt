package com.zj.cf

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager


internal object BsUtl {

    private val delegateCaches = mutableMapOf<String, BackStackDelegate>()
    private val managerIdTableForClass = mutableMapOf<String, String>()

    fun registerActivityCallBacks(act: FragmentActivity, managerId: String) {
        val key = "${act::class.java.name}-${act.hashCode()}"
        val delegateCache = delegateCaches[key]
        managerIdTableForClass[managerId] = key
        if (delegateCache != null) {
            delegateCache.updateNewManager(true, managerId)
        } else {
            delegateCaches[key] = BackStackDelegate.create(act, key, managerId)
        }
    }

    fun onManagerRemoving(managerId: String) {
        val value = managerIdTableForClass.remove(managerId)
        if (managerIdTableForClass.containsValue(value)) {
            delegateCaches[value]
        } else {
            delegateCaches.remove(value)
        }?.updateNewManager(false, managerId)
    }

    private class BackStackDelegate private constructor() {

        private var curIndex: Int = -1
        private var backStackEntryCount: Int = -1
        private var last: Int = -1
        private val managerIds = mutableListOf<String>()

        companion object {
            fun create(act: FragmentActivity, fromName: String, managerId: String): BackStackDelegate {
                var cls: Class<*> = act::class.java
                while (cls != androidx.activity.ComponentActivity::class.java) {
                    cls = cls.superclass
                    if (cls == null) throw java.lang.IllegalArgumentException("Parent of ${act::class.qualifiedName} should not be null.")
                }
                val field = cls.getDeclaredField("mOnBackPressedDispatcher")
                field.isAccessible = true
                val superBackDispatchers = field.get(act) as OnBackPressedDispatcher
                val callback = OnBackPressDelegate(fromName, superBackDispatchers)
                val clz = act as androidx.activity.ComponentActivity
                val backDispatcher = OnBackPressedDispatcher(callback)
                callback.isEnabled = true
                backDispatcher.addCallback(callback)
                field.set(clz, backDispatcher)
                val entity = BackStackDelegate()
                entity.managerIds.add(managerId)
                val onBackStackChanged = BackStackChangedListener(fromName, act.supportFragmentManager)
                act.supportFragmentManager.addOnBackStackChangedListener(onBackStackChanged)
                return entity
            }
        }

        fun updateNewManager(p: Boolean, managerId: String) {
            if (p) managerIds.add(managerId) else managerIds.remove(managerId)
        }

        fun onFragmentBackStackChanged(curIndex: Int, backStackEntryCount: Int, last: Int) {
            this.curIndex = curIndex
            this.backStackEntryCount = backStackEntryCount
            this.last = last
        }

        /**
         * return true : processed , else skipped.
         * */
        fun onBackPressed(): Boolean {
            val managerId = managerIds.last()
            if (curIndex < 0) {
                return if (backStackEntryCount <= 0) {
                    finishTopFragment(managerId)
                } else {
                    false
                }
            }
            return if (last == curIndex) {
                finishTopFragment(managerId)
            } else {
                false
            }
        }

        private fun finishTopFragment(managerId: String): Boolean {
            return FMStore.getTopConstrainFragment(managerId)?.let {
                if (it.onBackPressed()) {
                    it.finish()
                }
                return true
            } ?: false
        }
    }


    class OnBackPressDelegate(private val key: String, private val or: OnBackPressedDispatcher) : OnBackPressedCallback(true), Runnable {

        override fun handleOnBackPressed() {
            val d = delegateCaches[key]
            if (d != null && d.onBackPressed()) return
            or.onBackPressed()
        }

        override fun run() {
            or.onBackPressed()
        }
    }

    class BackStackChangedListener(private val key: String, private val fm: FragmentManager) : FragmentManager.OnBackStackChangedListener {

        private var fIds = mutableListOf<Int>()
        private var curMarkFId: Int = -1

        init {
            curMarkFId = updateFids(false)
            syncCurState()
        }

        override fun onBackStackChanged() {
            curMarkFId = updateFids(true)
            syncCurState()
        }

        private fun syncCurState() {
            val delegate = delegateCaches[key]
            val last = kotlin.runCatching {
                fm.getBackStackEntryAt(fm.backStackEntryCount - 1)
            }.getOrNull()?.id ?: -1
            delegate?.onFragmentBackStackChanged(curMarkFId, fm.backStackEntryCount, last)
        }

        /* record cur back stack*/
        private fun updateFids(chekTop: Boolean): Int {
            val fid = mutableListOf<Int>()
            repeat(fm.backStackEntryCount) {
                fid.add(fm.getBackStackEntryAt(it).id)
            }
            try {
                val last = fid.lastOrNull() ?: return -1
                if (!chekTop) {
                    return last
                } else {
                    if (last == curMarkFId) {
                        return last
                    } else {
                        val curI = fid.indexOf(curMarkFId)
                        if (curI == -1) {
                            val index = fIds.indexOf(curMarkFId)
                            for (i in (index - 1)..0) {
                                val l = fid.lastIndexOf(fIds[i])
                                if (l != -1) {
                                    return fIds[i]
                                }
                            }
                            val indexNext = fIds.indexOf(curMarkFId) + 1
                            if (indexNext !in 0..fIds.size) return -1
                            val next = fIds[indexNext]
                            val ln = fid.lastIndexOf(next)
                            if (ln != -1 && ((ln - 1) in 0..fid.size)) {
                                return fid[ln - 1]
                            }
                            return -1
                        }
                    }
                }
                return curMarkFId
            } finally {
                fIds = fid
            }
        }
    }
}