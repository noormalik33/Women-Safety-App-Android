package com.example.lab10firebasecode;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.nio.charset.StandardCharsets;

public class OfflineModeActivity extends AppCompatActivity {

    private Button btnBroadcast;
    private TextView tvStatus;
    private static final String SERVICE_ID = "com.example.women_safety_offline";
    private static final int REQUEST_CODE_PERMISSIONS = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_mode);

        btnBroadcast = findViewById(R.id.btnBroadcast);
        tvStatus = findViewById(R.id.tvStatus);

        btnBroadcast.setOnClickListener(v -> {
            if (checkPermissions()) {
                startAdvertising();
            } else {
                requestPermissions();
            }
        });
    }

    private void startAdvertising() {
        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build();

        Nearby.getConnectionsClient(this)
                .startAdvertising(
                        "SOS_USER",
                        SERVICE_ID,
                        connectionLifecycleCallback,
                        advertisingOptions)
                .addOnSuccessListener(
                        (Void unused) -> {
                            tvStatus.setText("Status: Broadcasting SOS Signal (Offline)...");
                            btnBroadcast.setEnabled(false);
                            btnBroadcast.setText("SIGNAL ACTIVE");
                            Toast.makeText(this, "Broadcasting via Bluetooth/WiFi...", Toast.LENGTH_LONG).show();
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            // Shows the exact error why it failed
                            tvStatus.setText("Error: " + e.getMessage());
                            e.printStackTrace();
                            Toast.makeText(this, "Broadcast Failed. Check Bluetooth/Location settings.", Toast.LENGTH_LONG).show();
                        });
    }

    // Callbacks for connection handling
    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    Nearby.getConnectionsClient(getApplicationContext()).acceptConnection(endpointId, payloadCallback);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    if (result.getStatus().getStatusCode() == ConnectionsStatusCodes.STATUS_OK) {
                        sendSOSPayload(endpointId);
                        tvStatus.setText("Status: Connected to a helper device!");
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    tvStatus.setText("Status: Disconnected.");
                }
            };

    private void sendSOSPayload(String endpointId) {
        String sosMessage = "HELP! SOS ALERT from " + android.os.Build.MODEL;
        Payload bytesPayload = Payload.fromBytes(sosMessage.getBytes(StandardCharsets.UTF_8));
        Nearby.getConnectionsClient(this).sendPayload(endpointId, bytesPayload);
    }

    private final PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {}
        @Override
        public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate update) {}
    };

    // --- CRITICAL PERMISSION FIX FOR ANDROID 12+ ---
    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.NEARBY_WIFI_DEVICES
            }, REQUEST_CODE_PERMISSIONS);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_CODE_PERMISSIONS);
        }
    }
}