package com.cityfruit.myapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.cityfruit.myapplication.base_fg.managers.BaseFragmentManager
import com.cityfruit.myapplication.indecators.BottomA
import com.cityfruit.myapplication.indecators.BottomB
import com.cityfruit.myapplication.indecators.BottomC
import com.cityfruit.myapplication.indecators.BottomD
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBA = BottomA()
        setContentView(R.layout.activity_main)
        object : BaseFragmentManager(this, R.id.fragment_container, 0, ll, mBA, BottomB(), BottomC(), BottomD()) {}

    }
}
