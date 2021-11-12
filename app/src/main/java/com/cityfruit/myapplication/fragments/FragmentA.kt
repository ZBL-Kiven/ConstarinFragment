package com.cityfruit.myapplication.fragments

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.cityfruit.myapplication.R
import com.cityfruit.myapplication.addOnClickListener
import com.zj.cf.annotations.Constrain
import com.zj.cf.annotations.Container
import com.zj.cf.fragments.ConstrainFragment
import com.zj.cf.startFragmentByNewTask
import com.cityfruit.myapplication.getBundle
import kotlinx.android.synthetic.main.fragment_a.*

@Constrain(id = "FragmentA", backMode = 1)
class FragmentA : ConstrainFragment(), LifecycleObserver {

    init {
        lifecycle.addObserver(this)
    }

    @Container var frgContainer: FrameLayout? = null

    override fun getView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.fragment_a, container, false)
    }

    override fun onCreate() {
        super.onCreate()
        frgContainer = fragment_container
        a_btn_new_task?.addOnClickListener {
            startFragmentByNewTask(FragmentB::class.java, getBundle("frgA ==> frgB by new Task"), {
                true
            })
        }

        a_btn_finish?.addOnClickListener {
            finish()
        }

        a_btn_next?.addOnClickListener {
            startFragment(FragmentB::class.java, getBundle("frgA ==> frgB"))
        }

        a_btn_new_linkage?.addOnClickListener {
            startFragmentByNewTask(FragmentH::class.java, getBundle("frgA ==> frg-Linkage"), {
                true
            })
        }
        val text = javaClass.simpleName
        txt?.text = text
        bg?.setBackgroundColor(ContextCompat.getColor(activity ?: return, R.color.c1))
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_CREATE)
    fun onLifecycleCreate() {
        Log.e("------ ", "onLifecycleCreate")
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_START)
    fun onLifecycleStart() {
        Log.e("------ ", "onLifecycleStart")
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_RESUME)
    fun onLifecycleResume() {
        Log.e("------ ", "onLifecycleResume")
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_PAUSE)
    fun onLifecyclePaused() {
        Log.e("------ ", "onLifecyclePaused")
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_STOP)
    fun onLifecycleStop() {
        Log.e("------ ", "onLifecycleStop")
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_DESTROY)
    fun onLifecycleDestroy() {
        Log.e("------ ", "onLifecycleDestroy")
    }
}