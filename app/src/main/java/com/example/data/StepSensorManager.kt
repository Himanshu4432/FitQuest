package com.example.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class StepSensorManager(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepCounterSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    val isHardwareSensorAvailable: Boolean
        get() = stepCounterSensor != null

    /**
     * Emits positive incremental step deltas from the Android hardware step counter.
     */
    fun observeStepDeltas(): Flow<Long> = callbackFlow {
        val manager = sensorManager
        val sensor = stepCounterSensor
        if (manager == null || sensor == null) {
            close()
            return@callbackFlow
        }
        var baseline: Float? = null
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                val raw = event?.values?.firstOrNull() ?: return
                val previous = baseline
                if (previous == null) {
                    baseline = raw
                } else {
                    val delta = (raw - previous).toLong()
                    if (delta > 0L) {
                        baseline = raw
                        trySend(delta)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        awaitClose { manager.unregisterListener(listener) }
    }
}
