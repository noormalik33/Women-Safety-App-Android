package com.example.lab10firebasecode;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ContactsActivity extends AppCompatActivity {

    EditText phone1, phone2, phone3;
    Button btnSave;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        phone1 = findViewById(R.id.phone1);
        phone2 = findViewById(R.id.phone2);
        phone3 = findViewById(R.id.phone3);
        btnSave = findViewById(R.id.btnSave);

        // We use SharedPreferences to save numbers locally on the phone
        sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);

        // Load saved numbers if they exist
        phone1.setText(sharedPreferences.getString("phone1", ""));
        phone2.setText(sharedPreferences.getString("phone2", ""));
        phone3.setText(sharedPreferences.getString("phone3", ""));

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("phone1", phone1.getText().toString());
                editor.putString("phone2", phone2.getText().toString());
                editor.putString("phone3", phone3.getText().toString());
                editor.apply();

                Toast.makeText(ContactsActivity.this, "Numbers Saved!", Toast.LENGTH_SHORT).show();
                finish(); // Go back to Dashboard
            }
        });
    }
}