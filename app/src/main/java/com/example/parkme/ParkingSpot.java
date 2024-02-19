package com.example.parkme;

import android.os.Parcel;
import android.os.Parcelable;

public class ParkingSpot implements Parcelable {
    private String id;
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
    public ParkingSpot(String id, String name, String details, double latitude, double longitude, String imageUrl, double price, float rating) {
        this.id = id;
        this.name = name;
        this.details = details;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
        this.price = price;
        this.rating = rating;
    }

    // Getters
    public String getId() {
        return id;
    }
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

    public String getImageUrl() {
        return imageUrl;
    }

    public double getPrice() {
        return price;
    }

    public float getRating() {
        return rating;
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

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    protected ParkingSpot(Parcel in) {
        id = in.readString(); // Add this line
        name = in.readString();
        details = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        imageUrl = in.readString();
        price = in.readDouble();
        rating = in.readFloat();
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id); // Add this line
        dest.writeString(name);
        dest.writeString(details);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(imageUrl);
        dest.writeDouble(price);
        dest.writeFloat(rating);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ParkingSpot> CREATOR = new Parcelable.Creator<ParkingSpot>() {
        @Override
        public ParkingSpot createFromParcel(Parcel in) {
            return new ParkingSpot(in);
        }

        @Override
        public ParkingSpot[] newArray(int size) {
            return new ParkingSpot[size];
        }
    };
}
