package com.example.lab10firebasecode;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser; // IMPORTANT: Need this import for FirebaseUser

public class LoginActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button btnLogin;
    TextView txtSignup;
    FirebaseAuth auth;

    ImageView iconYoutube, iconInstagram, iconFacebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- CHANGE 1: Direct already logged-in users to DashboardActivity ---
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // User is already signed in, check verification status before redirecting
            FirebaseUser user = auth.getCurrentUser();
            if (user.isEmailVerified()) {
                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Please verify your email to continue.", Toast.LENGTH_LONG).show();
                auth.signOut(); // Force sign out if session exists but email is unverified
            }
        }
        // -------------------------------------------------------------------

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignup = findViewById(R.id.txtSignup);

        iconYoutube = findViewById(R.id.iconYoutube);
        iconInstagram = findViewById(R.id.iconInstagram);
        iconFacebook = findViewById(R.id.iconFacebook);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        txtSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        iconYoutube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink("https://www.youtube.com/channel/UCMzACb-lwCjc7GGw6vsYUdQ");
            }
        });

        iconInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink("https://www.instagram.com/coreit.tech");
            }
        });

        iconFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink("https://www.facebook.com/share/1AmgLDUnc9/");
            }
        });
    }

    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Enter a valid email");
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Enter password");
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();

                            // ******************************************************
                            // 5b. CHECK EMAIL VERIFICATION STATUS
                            // ******************************************************
                            if (user != null && user.isEmailVerified()) {
                                // SUCCESS: Email is verified, proceed to dashboard.
                                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();

                            } else if (user != null && !user.isEmailVerified()) {
                                // FAILURE: Email is NOT verified.
                                Toast.makeText(LoginActivity.this, "Please verify your email address before logging in.", Toast.LENGTH_LONG).show();
                                auth.signOut(); // Sign out the unverified user to prevent partial access

                            } else {
                                // Catch-all failure
                                Toast.makeText(LoginActivity.this, "Authentication failed. User not found.", Toast.LENGTH_LONG).show();
                            }
                            // ******************************************************

                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void openLink(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open link: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}