package com.example.lab10firebasecode;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class FakeCallActivity extends AppCompatActivity {

    Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_call);

        // Play default Ringtone
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
        ringtone.play();

        // Stop ringtone when buttons clicked
        findViewById(R.id.btnDecline).setOnClickListener(v -> {
            ringtone.stop();
            finish();
        });

        findViewById(R.id.btnAccept).setOnClickListener(v -> {
            ringtone.stop();
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        if(ringtone != null && ringtone.isPlaying()){
            ringtone.stop();
        }
        super.onDestroy();
    }
}