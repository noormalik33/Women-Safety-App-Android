package com.example.lab10firebasecode;

public class Complaint {
    public String userId;
    public String name;
    public String cnic;
    public String contact;
    public String message;
    public String timestamp;

    // Empty constructor is required for Firebase to read data back
    public Complaint() {
    }

    public Complaint(String userId, String name, String cnic, String contact, String message, String timestamp) {
        this.userId = userId;
        this.name = name;
        this.cnic = cnic;
        this.contact = contact;
        this.message = message;
        this.timestamp = timestamp;
    }
}