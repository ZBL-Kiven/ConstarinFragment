package com.cityfruit.myapplication

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.cityfruit.myapplication.indecators.BottomA
import com.cityfruit.myapplication.indecators.BottomB
import com.cityfruit.myapplication.indecators.BottomC
import com.cityfruit.myapplication.indecators.BottomD
import com.zj.cf.fragments.ConstrainFragment
import com.zj.cf.managers.BaseFragmentManager
import com.zj.cf.setConstrainFragmentLifecycleCallBack
import kotlinx.android.synthetic.main.activity_second.*

class SecondAct : AppCompatActivity() {

    val txt = "the top of stack : %s"

    private var manager: BaseFragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBA = BottomA()
        setContentView(R.layout.activity_second)
        manager = object : BaseFragmentManager(this, R.id.fragment_container, 0, ll, mBA, BottomD()) {}
        Log.e("new manager created ", "id  = ${manager?.managerId}")
        setConstrainFragmentLifecycleCallBack { lifecycle, from, s ->
//            Log.e("------ ", "cf lifecycle changed :form $from \n lifecycle = $lifecycle \n")
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            manager?.let {
                (getTop(it) as? ConstrainFragment)?.let { cf ->
                    cf.finish()
                    return false
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}