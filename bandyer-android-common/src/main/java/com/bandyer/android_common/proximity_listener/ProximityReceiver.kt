package com.bandyer.android_common.proximity_listener

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.bandyer.android_common.*


/**
 * ProximitySensorReceiver is implemented as a boolean-sensor.
 * It returns just two values "NEAR" or "FAR". Threshold is done on the LUX
 * value i.e. the LUX value of the light sensor is compared with a threshold.
 * A LUX-value more than the threshold means the proximity sensor returns "FAR".
 * Anything less than the threshold value and the sensor  returns "NEAR".
 *
 * @param proximitySensorListener proximity sensor listener to notify when a change occurred.
 */
internal class ProximityReceiver constructor(var context: Context?, private var proximitySensorListener: ProximitySensorListener, override var debounceMillis: Long = 500) : ProximitySensor, SensorEventListener, LifecycleEvents {

    private val DEFAULT_BATCH_LATENCY = 190000

    private var lastEvent: SensorEvent? = null
    private var isTimerRunning = false

    internal var sensorManager: SensorManager? = null

    private var proximitySensor: Sensor? = null

    private var lastStateIsNear = false

    /**
     * Activate the proximity sensor
     */
    init {
        val hasProximitySensor = context?.packageManager?.hasSystemFeature(PackageManager.FEATURE_SENSOR_PROXIMITY) ?: false
        if (hasProximitySensor) sensorManager = context!!.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        if (context is AppCompatActivity) LifecyleBinder.bind(context as AppCompatActivity, this)
        else resume()
    }

    override fun create() {}

    override fun start() {}

    override fun resume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            sensorManager?.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL, DEFAULT_BATCH_LATENCY)
        } else {
            sensorManager?.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun stop() {}

    override fun destroy() {
        checkMainThread()
        sensorManager?.unregisterListener(this)
        context = null
        sensorManager = null
        proximitySensor = null
        timer.cancel()
    }

    override fun pause() {
        sensorManager?.unregisterListener(this, proximitySensor)
    }

    override fun isNear(): Boolean {
        checkMainThread()
        return lastStateIsNear
    }

    /**
     * Notifies changes in sensor accuracy.
     *
     * @param sensor the sensor which received changes.
     * @param accuracy new accuracy value.
     */
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        checkMainThread()
        assertIsTrue(sensor.type == Sensor.TYPE_PROXIMITY)
    }

    /**
     *  Notifies changes of sensor event.
     */
    override fun onSensorChanged(event: SensorEvent) {
        checkMainThread()
        assertIsTrue(event.sensor.type == Sensor.TYPE_PROXIMITY)

        lastEvent = event

        if (debounceMillis <= 0)
            evaluateEvent()
        else if (!isTimerRunning) {
            isTimerRunning = true
            timer.start()
        }
    }


    private fun evaluateEvent() {
        val distanceInCentimeters = lastEvent?.values?.get(0) ?: 0f

        val state = distanceInCentimeters < proximitySensor?.maximumRange ?: 0f
        if (state == lastStateIsNear)
            return

        lastStateIsNear = state
        // Report about new state to listening client. Client can then call
        // isNear() to query the current state (NEAR or FAR).
        proximitySensorListener.onProximitySensorChanged(lastStateIsNear)
    }

    private val timer = object : CountDownTimer(debounceMillis, debounceMillis / 2) {

        override fun onFinish() {
            evaluateEvent()
            isTimerRunning = false
        }

        override fun onTick(millisUntilFinished: Long) {}
    }
}
