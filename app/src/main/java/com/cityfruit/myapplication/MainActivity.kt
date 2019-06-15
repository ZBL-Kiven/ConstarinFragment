package com.cityfruit.myapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.FragmentTransaction
import android.view.View
import com.cityfruit.myapplication.base_fg.fragments.BaseLinkageFragment
import com.cityfruit.myapplication.base_fg.managers.BaseFragmentManager
import com.cityfruit.myapplication.base_fg.startFragment
import com.cityfruit.myapplication.fragments.FragmentA
import com.cityfruit.myapplication.indecators.BottomA
import com.cityfruit.myapplication.indecators.BottomB
import com.cityfruit.myapplication.indecators.BottomC
import com.cityfruit.myapplication.indecators.BottomD
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    var s = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBA = BottomA()
        setContentView(R.layout.activity_main)
        object : BaseFragmentManager(this, R.id.fragment_container, 0, ll, mBA, BottomB(), BottomC(), BottomD()){}

    }


}
