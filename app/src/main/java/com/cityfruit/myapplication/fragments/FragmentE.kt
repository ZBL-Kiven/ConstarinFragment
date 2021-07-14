package com.cityfruit.myapplication.fragments

import androidx.core.content.ContextCompat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.cityfruit.myapplication.R
import com.cityfruit.myapplication.addOnClickListener

import com.zj.cf.annotations.Constrain
import com.zj.cf.annotations.Container
import com.zj.cf.fragments.ConstrainFragment
import com.zj.cf.startFragmentByNewTask
import com.cityfruit.myapplication.getBundle
import com.cityfruit.myapplication.printBundle
import kotlinx.android.synthetic.main.fragment_a.*

@Constrain(id = "FragmentE", backMode = 1)
class FragmentE : ConstrainFragment() {

    @Container var frgContainer: FrameLayout? = null

    override fun getView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.fragment_a, container, false)
    }

    override fun onPostValue(bundle: Bundle?) {
        printBundle(bundle, false)
    }

    override fun onCreate() {
        super.onCreate()
        frgContainer = fragment_container
        a_btn_new_task?.addOnClickListener {
            startFragmentByNewTask(FragmentF::class.java, getBundle("frgE ==> frgF by new Task"), {
                true
            })
        }

        a_btn_finish?.addOnClickListener {
            finish()
        }

        a_btn_next?.addOnClickListener {
            startFragment(FragmentF::class.java, getBundle("frgE ==> frgF"))
        }
        a_btn_new_linkage?.addOnClickListener {
            startFragmentByNewTask(FragmentH::class.java, getBundle("frgE ==> frg-Linkage"), {
                true
            })
        }
        val text = javaClass.simpleName
        txt?.text = text
        bg?.setBackgroundColor(ContextCompat.getColor(activity ?: return, R.color.c5))
    }
}