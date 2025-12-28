package com.example.lab10firebasecode;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HiddenCameraActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor magnetometer;
    private TextView tvReading, tvStatus;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hidden_camera);

        tvReading = findViewById(R.id.tvReading);
        tvStatus = findViewById(R.id.tvStatus);
        progressBar = findViewById(R.id.progressBar);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (magnetometer != null) {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Calculate total magnetic field strength
        double magnitude = Math.sqrt(x * x + y * y + z * z);
        int strength = (int) magnitude;

        tvReading.setText(strength + " µT");
        progressBar.setProgress(strength);

        // Typical electronic devices emit > 70 µT close up
        if (strength > 70) {
            tvStatus.setText("⚠️ POTENTIAL CAMERA DETECTED!");
            tvStatus.setTextColor(0xFFFF0000); // Red
        } else {
            tvStatus.setText("Scanning... Safe range.");
            tvStatus.setTextColor(0xFF00FF00); // Green
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}