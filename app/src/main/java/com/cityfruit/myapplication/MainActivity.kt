package com.cityfruit.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.zj.cf.FMStore
import com.zj.cf.setConstrainFragmentLifecycleCallBack
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val txt = "the top of stack : %s"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main_frg.setOnClickListener {
            startActivity(Intent(this, SecondAct::class.java))
        }
        main_print.setOnClickListener {
            val s = FMStore.getManagersInfo()
            Log.e("-----==", s)
        }
        setConstrainFragmentLifecycleCallBack { lifecycle, from, s ->
            main_frg.text = s
//            Log.e("------ ", "cf lifecycle changed :form $from \n lifecycle = $lifecycle \n")
        }
    }
}
