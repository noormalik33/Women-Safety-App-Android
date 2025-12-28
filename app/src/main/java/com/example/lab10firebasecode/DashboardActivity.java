package com.example.lab10firebasecode;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    // UI Components (Removed cardOffline)
    private CardView cardSOS, cardContacts, cardFakeCall, cardLocation, cardHelpline, cardWalk, cardNearby, cardSiren, cardHiddenCamera, cardDangerMap, cardComplaint, cardShake;
    private Button btnSignOut;
    private ScrollView scrollDashboard, scrollInfo;
    private BottomNavigationView bottomNavigationView;
    private TextView tvGreeting;

    // Info Section Items (Included Layouts)
    private View infoSOS, infoContacts, infoFakeCall, infoLocation, infoSiren, infoSpy, infoDanger, infoComplaint, infoOffline, infoShake;

    // Logic Variables
    private FusedLocationProviderClient fusedLocationClient;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_CHECK_SETTINGS = 1001;

    // Feature: Siren
    private boolean isSirenPlaying = false;
    private ToneGenerator toneGenerator;

    // Feature: Audio Recorder
    private MediaRecorder mediaRecorder;
    private String audioFileName;

    // Feature: Tracking
    private LocationCallback locationCallback;

    // Feature: Battery Monitor
    private BatteryLevelReceiver batteryReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // --- Init System Services ---
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

        // --- Init Main Views (Navigation) ---
        scrollDashboard = findViewById(R.id.scrollDashboard);
        scrollInfo = findViewById(R.id.scrollInfo);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        tvGreeting = findViewById(R.id.tvGreeting);

        // --- Fetch User Name ---
        loadUserName();

        // --- Init Dashboard Cards ---
        cardSOS = findViewById(R.id.cardSOS);
        cardContacts = findViewById(R.id.cardContacts);
        cardFakeCall = findViewById(R.id.cardFakeCall);
        cardLocation = findViewById(R.id.cardLocation);
        cardHelpline = findViewById(R.id.cardHelpline);
        cardWalk = findViewById(R.id.cardWalk);
        cardNearby = findViewById(R.id.cardNearby);
        cardSiren = findViewById(R.id.cardSiren);
        cardHiddenCamera = findViewById(R.id.cardHiddenCamera);
        cardDangerMap = findViewById(R.id.cardDangerMap);
        cardComplaint = findViewById(R.id.cardComplaint);
        cardShake = findViewById(R.id.cardShake);

        btnSignOut = findViewById(R.id.btnSignOut);

        // --- Init Info Section Items ---
        infoSOS = findViewById(R.id.infoSOS);
        infoContacts = findViewById(R.id.infoContacts);
        infoFakeCall = findViewById(R.id.infoFakeCall);
        infoLocation = findViewById(R.id.infoLocation);
        infoSiren = findViewById(R.id.infoSiren);
        infoSpy = findViewById(R.id.infoSpy);
        infoDanger = findViewById(R.id.infoDanger);
        infoComplaint = findViewById(R.id.infoComplaint);
        infoOffline = findViewById(R.id.infoOffline);
        infoShake = findViewById(R.id.infoShake);

        // --- Setup Info Descriptions ---
        setupInfoItem(infoSOS, "SOS Alert", "Sends emergency SMS with live location, records audio, and calls contacts.");
        setupInfoItem(infoContacts, "Contacts", "Manage emergency numbers.");
        setupInfoItem(infoFakeCall, "Fake Call", "Simulate a call to escape awkward situations.");
        setupInfoItem(infoLocation, "Live Location", "Share real-time GPS location via WhatsApp.");
        setupInfoItem(infoSiren, "Insta-Siren", "Play a loud alarm sound.");
        setupInfoItem(infoSpy, "Spy Detector", "Detect hidden cameras using magnetic sensor.");
        setupInfoItem(infoDanger, "Danger Zone", "View heatmap of unsafe areas.");
        setupInfoItem(infoComplaint, "Notes", "Save incident details and private notes securely.");
        setupInfoItem(infoOffline, "Offline Mode", "Broadcast distress signals to nearby devices via Bluetooth/Wi-Fi without internet.");
        setupInfoItem(infoShake, "Violent Shake", "Shake your phone vigorously to trigger an immediate SOS alert.");

        // --- Start Background Services ---
        try {
            Intent intent = new Intent(this, ShakeService.class);
            startService(intent);
        } catch (Exception e) { e.printStackTrace(); }

        batteryReceiver = new BatteryLevelReceiver();
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));

        // --- Setup Listeners & Navigation ---
        setupClickListeners();
        setupBottomNav();
    }

    private void loadUserName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.hasChild("name")) {
                        String name = snapshot.child("name").getValue(String.class);
                        if (name != null && !name.isEmpty()) {
                            tvGreeting.setText("Hello, " + name + " \uD83D\uDC4B");
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    // --- BOTTOM NAVIGATION LOGIC ---
    private void setupBottomNav() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Show Home Grid, Hide Info
                scrollDashboard.setVisibility(View.VISIBLE);
                scrollInfo.setVisibility(View.GONE);
                return true;
            }
            else if (id == R.id.nav_info) {
                // Show Info Screen, Hide Home
                scrollDashboard.setVisibility(View.GONE);
                scrollInfo.setVisibility(View.VISIBLE);
                return true;
            }
            else if (id == R.id.nav_offline) {
                // Launch Offline Mode Activity directly
                startActivity(new Intent(DashboardActivity.this, OfflineModeActivity.class));
                return false; // Don't switch tab selection visually
            }
            else if (id == R.id.nav_signout) {
                performSignOut();
                return true;
            }
            return false;
        });
    }

    private void performSignOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // --- CLICK LISTENERS ---
    private void setupClickListeners() {
        // SOS Logic
        cardSOS.setOnClickListener(v -> {
            if (checkPermissions()) {
                sendEmergencySOS();   // SMS only
                startAudioRecording();
            } else {
                requestPermissions();
            }
        });

        // Violent Shake Button
        cardShake.setOnClickListener(v -> {
            if (checkPermissions()) {
                sendEmergencySOS(); // Trigger SMS
                Toast.makeText(this, "Violent Shake Alert Sent!", Toast.LENGTH_SHORT).show();
            } else {
                requestPermissions();
            }
        });

        cardSiren.setOnClickListener(v -> toggleSiren());
        cardContacts.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, ContactsActivity.class)));
        cardFakeCall.setOnClickListener(v -> {
            Toast.makeText(this, "Call in 3 sec...", Toast.LENGTH_SHORT).show();
            new android.os.Handler().postDelayed(() -> startActivity(new Intent(DashboardActivity.this, FakeCallActivity.class)), 3000);
        });

        cardLocation.setOnClickListener(v -> {
            if (checkPermissions()) {
                shareLiveLocation();
            } else {
                requestPermissions();
            }
        });

        cardHelpline.setOnClickListener(v -> showHelplineDialog());
        cardWalk.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, WalkWithMeActivity.class)));
        cardNearby.setOnClickListener(v -> openNearbyMap());
        cardHiddenCamera.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, HiddenCameraActivity.class)));
        cardDangerMap.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, DangerMapActivity.class)));
        cardComplaint.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, FeedbackActivity.class)));

        if(btnSignOut != null) {
            btnSignOut.setOnClickListener(v -> {
                performSignOut();
            });
        }
    }

    // --- FEATURE LOGIC ---
    private void setupInfoItem(View view, String title, String desc) {
        if (view != null) {
            TextView tvTitle = view.findViewById(R.id.tvTitle);
            if (tvTitle != null) tvTitle.setText(title);
            view.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle(title).setMessage(desc).setPositiveButton("OK", null).show());
        }
    }

    private void autoCallGuardian() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String p1 = sharedPreferences.getString("phone1", "");

        if (!p1.isEmpty()) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + p1));
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                startActivity(callIntent);
            }
        }
    }

    private void vibratePhone() {
        android.os.Vibrator v = (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (v != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                v.vibrate(android.os.VibrationEffect.createOneShot(500, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(500);
            }
        }
    }

    private void startContinuousTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 15000) // Update every 15s
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;
                for (android.location.Location location : locationResult.getLocations()) {
                    saveLocationToFirebase(location.getLatitude(), location.getLongitude());
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        Toast.makeText(this, "Tracking Mode ON: Updating every 15s", Toast.LENGTH_LONG).show();
    }

    // --- FEATURE: INSTA-SIREN ---
    private void toggleSiren() {
        if (isSirenPlaying) {
            toneGenerator.stopTone();
            Toast.makeText(this, "Siren OFF", Toast.LENGTH_SHORT).show();
            isSirenPlaying = false;
        } else {
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 20000); // Play for 20 sec
            Toast.makeText(this, "Siren ON! Loud noise!", Toast.LENGTH_SHORT).show();
            isSirenPlaying = true;
        }
    }

    // --- FEATURE: AUDIO EVIDENCE RECORDING ---
    private void startAudioRecording() {
        if (getExternalCacheDir() == null) return;
        audioFileName = getExternalCacheDir().getAbsolutePath() + "/evidence_audio.3gp";
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(audioFileName);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            Toast.makeText(this, "Recording Audio Evidence...", Toast.LENGTH_LONG).show();
            new android.os.Handler().postDelayed(this::stopRecordingAndUpload, 15000);
        } catch (Exception e) {
            Toast.makeText(this, "Recording Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecordingAndUpload() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                uploadAudioToFirebase();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void uploadAudioToFirebase() {
        Uri file = Uri.fromFile(new File(audioFileName));
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("evidence/" + System.currentTimeMillis() + ".3gp");

        storageRef.putFile(file)
                .addOnSuccessListener(taskSnapshot -> Toast.makeText(this, "Evidence Uploaded to Cloud!", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Upload Failed.", Toast.LENGTH_SHORT).show());
    }

    // --- HELPERS (Maps, Location, SOS) ---
    private void openNearbyMap() {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=police+station");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    // Modified to automatically prompt for GPS if disabled
    private void shareLiveLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
            return;
        }

        // Check if GPS is enabled, if not prompt user
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build();
        com.google.android.gms.location.LocationSettingsRequest.Builder builder = new com.google.android.gms.location.LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        com.google.android.gms.location.SettingsClient client = LocationServices.getSettingsClient(this);
        com.google.android.gms.tasks.Task<com.google.android.gms.location.LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            // All location settings are satisfied. The client can initialize location requests here.
            fetchLocationAndShare();
        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof com.google.android.gms.common.api.ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    com.google.android.gms.common.api.ResolvableApiException resolvable = (com.google.android.gms.common.api.ResolvableApiException) e;
                    resolvable.startResolutionForResult(DashboardActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (android.content.IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            } else {
                Toast.makeText(this, "Location settings not met. Please turn on GPS.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Helper method to actually fetch and share location
    private void fetchLocationAndShare() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        Toast.makeText(this, "Fetching location...", Toast.LENGTH_SHORT).show();

        CancellationTokenSource tokenSource = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        String mapLink = "https://www.google.com/maps/search/?api=1&query=" + location.getLatitude() + "," + location.getLongitude();
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, "My Live Location: " + mapLink);
                        startActivity(Intent.createChooser(shareIntent, "Share Location via"));
                    } else {
                        Toast.makeText(this, "Unable to get location. Trying again...", Toast.LENGTH_SHORT).show();
                        // Retry logic or just ask user to wait
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Handle the result from the "Turn on GPS" dialog
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                // User agreed to make required location settings changes.
                fetchLocationAndShare();
            } else {
                // User chose not to make required location settings changes.
                Toast.makeText(this, "GPS is required to share location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendEmergencySOS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationTokenSource().getToken())
                .addOnSuccessListener(location -> {
                    String link = (location != null) ? "https://www.google.com/maps/search/?api=1&query=" + location.getLatitude() + "," + location.getLongitude() : "Unknown Loc";
                    sendSMSToContacts("HELP! I am in danger. Track me: " + link);
                });
    }

    private void sendSMSToContacts(String message) {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String p1 = sharedPreferences.getString("phone1", "");
        String p2 = sharedPreferences.getString("phone2", "");
        if (!p1.isEmpty()) SmsManager.getDefault().sendTextMessage(p1, null, message, null, null);
        if (!p2.isEmpty()) SmsManager.getDefault().sendTextMessage(p2, null, message, null, null);
        Toast.makeText(this, "SOS Sent!", Toast.LENGTH_SHORT).show();
    }

    private void saveLocationToFirebase(double lat, double lng) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("UserLocations").child(user.getUid());
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("latitude", lat);
            locationData.put("longitude", lng);
            locationData.put("timestamp", System.currentTimeMillis());
            ref.setValue(locationData);
        }
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CALL_PHONE
        }, PERMISSION_REQUEST_CODE);
    }

    private void showHelplineDialog() {
        String[] options = {"Police (15)", "Rescue (1122)", "Women Helpline (1043)"};
        final String[] numbers = {"15", "1122", "1043"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Helplines");
        builder.setItems(options, (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + numbers[which]));
            startActivity(intent);
        });
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryReceiver);
        if (locationCallback != null) fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}