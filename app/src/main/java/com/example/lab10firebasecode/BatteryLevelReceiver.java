package com.example.lab10firebasecode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.telephony.SmsManager;
import android.widget.Toast;

public class BatteryLevelReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BATTERY_LOW.equals(intent.getAction())) {
            sendLowBatterySOS(context);
        }
    }

    private void sendLowBatterySOS(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        String p1 = sharedPreferences.getString("phone1", "");
        String p2 = sharedPreferences.getString("phone2", "");

        if (p1.isEmpty() && p2.isEmpty()) return;

        SmsManager smsManager = SmsManager.getDefault();
        String msg = "EMERGENCY: My battery is critical (Low). Last known status: Safe.";

        try {
            if (!p1.isEmpty()) smsManager.sendTextMessage(p1, null, msg, null, null);
            if (!p2.isEmpty()) smsManager.sendTextMessage(p2, null, msg, null, null);
            Toast.makeText(context, "Low Battery Alert Sent!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}