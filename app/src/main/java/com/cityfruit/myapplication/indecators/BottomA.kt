package com.cityfruit.myapplication.indecators

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.cityfruit.myapplication.R
import com.zj.cf.fragments.BaseLinkageFragment

class BottomA : BaseLinkageFragment(), LifecycleEventObserver {


    override fun onCreate() {
        super.onCreate()

    }

    override fun getView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.fragment_empty, container, false)
    }

    override fun onReStart() {
        super.onReStart()
    }

    override fun onStarted() {
        super.onStarted()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        Log.e("------ ", "onLifecycleChanged ===> $source   ${event.name}")
    }
}
