package com.example.lab10firebasecode;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class ShakeService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
        float delta = mAccelCurrent - mAccelLast;
        mAccel = mAccel * 0.9f + delta;

        // SHAKE THRESHOLD: Adjust 12 to higher (harder shake) or lower (easier shake)
        if (mAccel > 12) {
            // Shake Detected!
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (v != null) v.vibrate(500); // Vibrate to confirm

            sendSOS();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void sendSOS() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String p1 = sharedPreferences.getString("phone1", "");
        String p2 = sharedPreferences.getString("phone2", "");

        SmsManager smsManager = SmsManager.getDefault();
        String msg = "EMERGENCY! I detected a violent shake. I might be in trouble.";

        try {
            if (!p1.isEmpty()) smsManager.sendTextMessage(p1, null, msg, null, null);
            if (!p2.isEmpty()) smsManager.sendTextMessage(p2, null, msg, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Don't unregister listener so it keeps running,
        // or unregister if you want to stop it when app closes.
        // sensorManager.unregisterListener(this);
    }
}