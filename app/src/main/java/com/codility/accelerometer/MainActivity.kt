@file:Suppress("DEPRECATION")

package com.codility.accelerometer

import android.content.Context
import android.os.Handler
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var color = false
    private var isInit = false
    private var initTime = System.currentTimeMillis()
    private var runCounter = 0
    private var mAccelCurrent:Float =0.0F
    private var mAccMax:Float =0.0F
    private var countDownThreadHold = 1 * 30 *1000
    val mHandler = Handler()
    private lateinit var btn: Button




    override fun onAccuracyChanged(s: Sensor?, i: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        resetButton.setVisibility(View.INVISIBLE)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        countDownStart()

        resetButton.setOnClickListener{
            initTime = System.currentTimeMillis()
        }
    }


    private fun countDownStart(){

        val runnable = object : Runnable{
            override fun run() {
                mHandler.removeCallbacksAndMessages(null)
                val currentTime = System.currentTimeMillis()
                val timeDiff = currentTime-initTime
                if(timeDiff <= countDownThreadHold){
                    val min = ((countDownThreadHold- timeDiff) /1000) /60
                    val sec = ((countDownThreadHold- timeDiff )/ 1000) %60
                    countDownTimer.text = "CountDown: ".plus(min.toString()).plus(":").plus(sec.toString())
                    resetButton.setVisibility(View.INVISIBLE);

                }else{
                    resetButton.setVisibility(View.VISIBLE);
                }

                mHandler.postDelayed(this, 500)
            }

        }
        runnable.run()

    }

    private fun getAccelerometer(event: SensorEvent) {
        // Movement

        val xVal = event.values[0]
        val yVal = event.values[1]
        val zVal = event.values[2]
        tvXAxiz.text = "X Value: ".plus(xVal.toString())
        tvYAxis.text = "Y Value: ".plus(yVal.toString())
        tvZAxis.text = "Z Value: ".plus(zVal.toString())

        val accelerationSquareRoot = (xVal * xVal + yVal * yVal + zVal * zVal) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH)
        if(accelerationSquareRoot > mAccMax){
            mAccMax=accelerationSquareRoot
        }
        accSum.text = "Max Acc: ".plus(mAccMax.toString())

        if (accelerationSquareRoot >= 1.7) {
            initTime = System.currentTimeMillis()
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager!!.registerListener(this, sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(this)
    }
}