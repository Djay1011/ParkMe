package com.example.parkme;

import android.os.Parcel;
import android.os.Parcelable;

public class ParkingSpot implements Parcelable {
    private String id;
    private String name;
    private String details;
    private double latitude;
    private double longitude;
    private double price;
    private String sizeType;
    private int capacity;
    private int bookedSpots;
    private boolean hasCCTV;
    private boolean hasDisabledAccess;
    private boolean hasElectricCharger;

    // Empty constructor needed for Firestore data retrieval
    public ParkingSpot() {
    }

    public ParkingSpot(String id, String name, String details, double latitude, double longitude, double price, String sizeType, int capacity, int bookedSpots, boolean hasCCTV, boolean hasDisabledAccess, boolean hasElectricCharger) {
        this.id = id;
        this.name = name;
        this.details = details;
        this.latitude = latitude;
        this.longitude = longitude;
        this.price = price;
        this.sizeType = sizeType;
        this.capacity = capacity;
        this.bookedSpots = bookedSpots;
        this.hasCCTV = hasCCTV;
        this.hasDisabledAccess = hasDisabledAccess;
        this.hasElectricCharger = hasElectricCharger;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getSizeType() {
        return sizeType;
    }

    public void setSizeType(String sizeType) {
        this.sizeType = sizeType;
    }


    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getBookedSpots() {
        return bookedSpots;
    }

    public void setBookedSpots(int bookedSpots) {
        this.bookedSpots = bookedSpots;
    }

    public boolean isAvailable() {
        return bookedSpots < capacity;
    }

    public boolean isHasCCTV() {
        return hasCCTV;
    }

    public void setHasCCTV(boolean hasCCTV) {
        this.hasCCTV = hasCCTV;
    }

    public boolean isHasDisabledAccess() {
        return hasDisabledAccess;
    }

    public void setHasDisabledAccess(boolean hasDisabledAccess) {
        this.hasDisabledAccess = hasDisabledAccess;
    }

    public boolean isHasElectricCharger() {
        return hasElectricCharger;
    }

    public void setHasElectricCharger(boolean hasElectricCharger) {
        this.hasElectricCharger = hasElectricCharger;
    }

    protected ParkingSpot(Parcel in) {
        id = in.readString();
        name = in.readString();
        details = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        price = in.readDouble();
        sizeType = in.readString();
        capacity = in.readInt();
        bookedSpots = in.readInt();
        hasCCTV = in.readByte() != 0;
        hasDisabledAccess = in.readByte() != 0;
        hasElectricCharger = in.readByte() != 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(details);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(price);
        dest.writeString(sizeType);
        dest.writeInt(capacity);
        dest.writeInt(bookedSpots);
        dest.writeByte((byte) (hasCCTV ? 1 : 0));
        dest.writeByte((byte) (hasDisabledAccess ? 1 : 0));
        dest.writeByte((byte) (hasElectricCharger ? 1 : 0));
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
