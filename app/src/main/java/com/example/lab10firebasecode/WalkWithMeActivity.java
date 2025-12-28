package com.example.lab10firebasecode;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

public class WalkWithMeActivity extends AppCompatActivity {

    TextView tvTimer;
    Button btnImSafe;
    CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_with_me);

        tvTimer = findViewById(R.id.tvTimer);
        btnImSafe = findViewById(R.id.btnImSafe);

        // Start a 5 minute timer (300,000 milliseconds)
        startTimer(300000);

        btnImSafe.setOnClickListener(v -> {
            if (timer != null) timer.cancel();
            Toast.makeText(this, "Walk Mode Ended. You are safe.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void startTimer(long duration) {
        timer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                sendSOS();
                Toast.makeText(WalkWithMeActivity.this, "Timer ended! Sending SOS...", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    private void sendSOS() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String p1 = sharedPreferences.getString("phone1", "");
        SmsManager smsManager = SmsManager.getDefault();
        try {
            if (!p1.isEmpty()) smsManager.sendTextMessage(p1, null, "ALERT: My Safe Walk timer expired! Check on me!", null, null);
        } catch (Exception e) { e.printStackTrace(); }
    }
}