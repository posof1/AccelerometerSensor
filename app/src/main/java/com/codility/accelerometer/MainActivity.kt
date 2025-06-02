@file:Suppress("DEPRECATION")

package com.codility.accelerometer

import android.content.Context
import android.os.Handler
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.text.DecimalFormat
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), SensorEventListener {

    private val countDownThreadHold = 30* 60 *1000 //倒數時間
    private val alertThreadHold = 10 *1000 //未重設時間
    private val resetThreadHold =1.7f

    private var sensorManager: SensorManager? = null

    private var initTime = System.currentTimeMillis()

    private var mAccMax:Float =0.0F
    private var mAccMin:Float =50.0F


    val mHandler = Handler()
    private lateinit var btn: Button
    private val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    private lateinit var vibrator: Vibrator
    private lateinit var ringtone: Ringtone


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
        ringtone= RingtoneManager.getRingtone(this.applicationContext, ringtoneUri)
        resetButton.setOnClickListener{
            initTime = System.currentTimeMillis()
            countDownTimer.setTextColor(getResources().getColor(R.color.colorPrimaryDark))
        }
        vibrator=getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        countDownStart()
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
                    countDownTimer.text = "CountDown: ${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}"
                    resetButton.setVisibility(View.INVISIBLE);

                    if (ringtone.isPlaying) {
                        ringtone.stop()
                        vibrator.cancel()
                    }

                }else{
                    if((timeDiff-countDownThreadHold) <= alertThreadHold){
                    resetButton.setVisibility(View.VISIBLE);
                    if (!ringtone.isPlaying) {

                        countDownTimer.setTextColor(getResources().getColor(R.color.red))
                        vibrator.vibrate(200)
                        ringtone.play()
                    }
                    }else{

                        vibrator.vibrate(200)
                        ringtone.play()
                        Toast.makeText(this@MainActivity, "逾時未重設!!!!", Toast.LENGTH_SHORT).show();
                    }
                }

                mHandler.postDelayed(this, 500)
            }

        }
        runnable.run()

    }
    private fun converStringToFormat(input: String):String{
        if(input.length>=6){
            return input.substring(0,6)
        }else{
            return input
        }
    }

    private fun getAccelerometer(event: SensorEvent) {
        // Movement
        val xVal = event.values[0]
        val yVal = event.values[1]
        val zVal = event.values[2]
        tvXAxiz.text = "X: ".plus(converStringToFormat(xVal.toString()))
        tvYAxis.text = "Y: ".plus(converStringToFormat(yVal.toString()))
        tvZAxis.text = "Z: ".plus(converStringToFormat(zVal.toString()))

        val accelerationSquareRoot = (xVal * xVal + yVal * yVal + zVal * zVal) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH)
        if(accelerationSquareRoot > mAccMax){
            mAccMax=accelerationSquareRoot
        }

        accMax.text = "最大加速度: ".plus(converStringToFormat(mAccMax.toString()))
        accMin.text = "加速度: ".plus(converStringToFormat(accelerationSquareRoot.toString()))

        if (accelerationSquareRoot >= resetThreadHold) {
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