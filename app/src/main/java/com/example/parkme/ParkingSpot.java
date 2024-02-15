package com.example.parkme;

public class ParkingSpot {
    private String name;
    private String details;
    private double latitude;
    private double longitude;
    private String imageUrl; // URL to the image
    private double price;    // Price per hour
    private float rating;

    // Empty constructor needed for Firestore data retrieval
    public ParkingSpot() {
    }

    // Constructor with parameters
    public ParkingSpot(String name, String details, double latitude, double longitude) {
        this.name = name;
        this.details = details;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDetails() {
        return details;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getRating() {
        return longitude;
    }

    public double getPrice() {
        return longitude;
    }

    public double getImageUrl() {
        return longitude;
    }



    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

