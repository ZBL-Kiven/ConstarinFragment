package com.cityfruit.myapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import com.zj.cf.fragments.ConstrainFragment
import com.zj.cf.managers.BaseFragmentManager
import com.cityfruit.myapplication.indecators.BottomA
import com.cityfruit.myapplication.indecators.BottomB
import com.cityfruit.myapplication.indecators.BottomC
import com.cityfruit.myapplication.indecators.BottomD
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val txt = "the top of stack : %s"

    var manager:BaseFragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBA = BottomA()
        setContentView(R.layout.activity_main)
        manager = object : BaseFragmentManager(this, R.id.fragment_container, 0, ll, mBA, BottomB(), BottomC(), BottomD()) {}
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            manager?.let {
                (getTop(it) as? ConstrainFragment)?.finish()
                return false
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
