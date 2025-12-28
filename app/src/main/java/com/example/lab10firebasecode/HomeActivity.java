package com.example.lab10firebasecode;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    Button btnSignOut;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure this layout name matches the XML file you created
        setContentView(R.layout.activity_home);

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance();

        // Initialize the Sign Out Button
        btnSignOut = findViewById(R.id.btnSignOut);

        // Set the click listener for sign out
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutUser();
            }
        });
    }

    private void signOutUser() {
        // Sign out the current user from Firebase
        auth.signOut();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();

        // Navigate the user back to the Login screen
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        // Clear the activity stack so they can't go back to HomeActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}