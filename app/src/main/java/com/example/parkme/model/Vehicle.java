package com.example.parkme.model;

public class Vehicle {
    private String plateNumber;

    public Vehicle() {
    }

    public Vehicle(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }
}

