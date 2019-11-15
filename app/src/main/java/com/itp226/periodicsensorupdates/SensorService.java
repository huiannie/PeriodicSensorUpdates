package com.itp226.periodicsensorupdates;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

// This service runs periodically with or without activity.
// When this service is killed, it broadcasts a request to the
// RestartReceiver. The receiver will check the sensor setting
// and determine whether to restart the service.
// The sensor type is determined by the user in MainActivity.
// The service retrieves the sensor type, and registers itself
// as a listener for the sensor.
// When the sensor reports a value above the threshold determined
// by the user, an alert broadcast is made.
// References:
// https://stackoverflow.com/questions/11987134/how-to-measure-ambient-temperature-in-android
// https://stackoverflow.com/questions/43366795/update-foreground-service-notification-with-sensor-data
public class SensorService extends Service implements SensorEventListener {
    private static final String myId = SensorService.class.getSimpleName();

    SensorManager sensorManager;
    Sensor sensor;
    int sensorType;
    float threshold;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Read the sensor type.
        // Read the threshold for setting alert.
        threshold = getSharedPreferences(MainActivity.PreferenceFile, MODE_PRIVATE).getFloat(MainActivity.PreferenceKeyThreshold, 0);
        sensorType = getSharedPreferences(MainActivity.PreferenceFile, MODE_PRIVATE).getInt(MainActivity.PreferenceKeySensorType, 0);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(sensorType);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(myId, "onDestroy");
        sensorManager.unregisterListener(this);
        // send new broadcast to restart the service
        // when this service is destroyed.
        Intent broadcastIntent = new Intent("com.itp226.periodicsensorupdates.RestartReceiverIntent");
        sendBroadcast(broadcastIntent);
    }

    void sendAlert() {
        Intent broadcastIntent = new Intent("com.itp226.periodicsensorupdates.AlertReceiverIntent");
        sendBroadcast(broadcastIntent);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float value = sensorEvent.values[0];
        Log.i(myId, "Sensor " + sensorEvent.sensor.getName() + " value=" + value);
        if (value> threshold) {
            Log.i(myId, "Alert request sent.");
            sendAlert();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }
}
