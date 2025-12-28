package com.example.lab10firebasecode;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    EditText edtName, edtContact, edtEmail, edtPassword;
    Button btnSignup;
    TextView txtLoginLink;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        edtName = findViewById(R.id.edtName);
        edtContact = findViewById(R.id.edtContact);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnSignup = findViewById(R.id.btnSignup);
        txtLoginLink = findViewById(R.id.txtLoginLink);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        txtLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser() {
        final String name = edtName.getText().toString().trim();
        final String contact = edtContact.getText().toString().trim();
        final String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // 1. Validation: Empty Fields
        if (name.isEmpty()) {
            edtName.setError("Name is required");
            edtName.requestFocus();
            return;
        }

        if (contact.isEmpty() || contact.length() != 11) {
            edtContact.setError("Valid 11-digit contact required");
            edtContact.requestFocus();
            return;
        }

        // 2. Validation: Email Format
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Please enter a valid email address");
            edtEmail.requestFocus();
            return;
        }

        // 3. Validation: Password Length
        if (password.isEmpty() || password.length() < 6) {
            edtPassword.setError("Password must be at least 6 characters");
            edtPassword.requestFocus();
            return;
        }

        // 4. Create User in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // User created successfully
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();

                            if (firebaseUser != null) {
                                // 5. Send Email Verification Link
                                firebaseUser.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> emailTask) {
                                                if (emailTask.isSuccessful()) {

                                                    // 6. Save User Data to Database
                                                    saveUserToDatabase(firebaseUser.getUid(), name, contact, email);

                                                    // 7. Show Message & Redirect to Login (NOT Dashboard)
                                                    Toast.makeText(SignupActivity.this, "Registered! Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show();

                                                    // Force logout so they can't enter without verifying
                                                    mAuth.signOut();

                                                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                    finish();

                                                } else {
                                                    Toast.makeText(SignupActivity.this, "Failed to send verification email: " + emailTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }

                        } else {
                            // Handle Specific Registration Errors
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                edtPassword.setError("Password is too weak. Mix letters & numbers.");
                                edtPassword.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                edtEmail.setError("This email is already registered.");
                                edtEmail.requestFocus();
                            } catch (Exception e) {
                                Toast.makeText(SignupActivity.this, "Registration Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void saveUserToDatabase(String uid, String name, String contact, String email) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("contact", contact);
        userMap.put("email", email);
        userMap.put("userId", uid);
        userMap.put("is_email_verified", "false"); // Initially false until they verify

        mDatabase.child(uid).setValue(userMap);
    }
}