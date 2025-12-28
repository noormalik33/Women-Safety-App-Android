package com.example.lab10firebasecode;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {

    private EditText edtTitle, edtDate, edtContent;
    private Button btnSaveNote;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        auth = FirebaseAuth.getInstance();
        // Save under "UserNotes" -> [UserID] -> [NoteID]
        databaseReference = FirebaseDatabase.getInstance().getReference("UserNotes");

        edtTitle = findViewById(R.id.edtTitle);
        edtDate = findViewById(R.id.edtDate);
        edtContent = findViewById(R.id.edtContent);
        btnSaveNote = findViewById(R.id.btnSaveNote);

        // Auto-fill today's date
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        edtDate.setText(currentDate);

        btnSaveNote.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = edtTitle.getText().toString().trim();
        String date = edtDate.getText().toString().trim();
        String content = edtContent.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Title and Content are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : "Anonymous";
        String noteId = databaseReference.push().getKey();

        Map<String, Object> noteMap = new HashMap<>();
        noteMap.put("title", title);
        noteMap.put("date", date);
        noteMap.put("content", content);
        noteMap.put("timestamp", System.currentTimeMillis());

        if (noteId != null) {
            databaseReference.child(userId).child(noteId).setValue(noteMap)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(FeedbackActivity.this, "Note Saved Successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(FeedbackActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }
}