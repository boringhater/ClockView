package com.apmolokanova.clockview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Button
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val clockView1 = findViewById<ClockView>(R.id.clockView1)
        val clockView2 = findViewById<ClockView>(R.id.clockView2)
        val clockView3 = findViewById<ClockView>(R.id.clockView3)
        findViewById<Button>(R.id.randomSizeBtn).setOnClickListener {
            resize(clockView1)
            resize(clockView2)
            resize(clockView3)
        }
    }

    fun resize(view: View) {
        view.setPadding(randomSize(),randomSize(),randomSize(),randomSize())
    }
    fun randomSize(): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Random.nextFloat()*50,resources.displayMetrics).toInt()
    }
}