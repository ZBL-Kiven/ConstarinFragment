package com.cityfruit.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import com.cityfruit.myapplication.indecators.*
import com.zj.cf.fragments.ConstrainFragment
import com.zj.cf.managers.BaseFragmentManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val txt = "the top of stack : %s"

    private var manager: BaseFragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBA = BottomA()
        setContentView(R.layout.activity_main)
        manager = object : BaseFragmentManager(this, R.id.fragment_container, 0, ll, mBA, BottomB(), BottomC(), BottomD()) {

        }
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
