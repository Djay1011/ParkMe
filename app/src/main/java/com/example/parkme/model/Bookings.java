package com.example.parkme.model;

import java.util.Calendar;
import java.util.Date;

public class Bookings {
    private String bookingId;
    private String userId;
    private String parkingSpotId;
    private String cardId;
    private Date startTime;
    private int durationInHours; // Duration in hours
    private double totalPrice;

    public Bookings() {
    }

    public Bookings(String bookingId, String userId, String parkingSpotId, String cardId, Date startTime, int durationInHours, double totalPrice) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.parkingSpotId = parkingSpotId;
        this.cardId = cardId;
        this.startTime = startTime;
        this.durationInHours = durationInHours;
        this.totalPrice = totalPrice;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getParkingSpotId() {
        return parkingSpotId;
    }

    public void setParkingSpotId(String parkingSpotId) {
        this.parkingSpotId = parkingSpotId;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public int getDurationInHours() {
        return durationInHours;
    }

    public void setDurationInHours(int durationInHours) {
        this.durationInHours = durationInHours;
    }

    public Date getEndTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        calendar.add(Calendar.HOUR_OF_DAY, durationInHours);
        return calendar.getTime();
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
