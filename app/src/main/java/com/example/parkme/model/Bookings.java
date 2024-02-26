package com.example.parkme.model;

import java.util.Calendar;
import java.util.Date;

public class Bookings {
    private String bookingId;
    private String userId;
    private String parkingSpotId;
    private String cardId;
    private Date startTime;
    private Date endTime; // endTime field
    private int duration; // Duration in hours
    private double totalPrice;
    private String status; // Status field

    public enum BookingStatus {
        UPCOMING, INPROGRESS, COMPLETED
    }

    public Bookings() {
    }

    public Bookings(String bookingId, String userId, String parkingSpotId, String cardId, Date startTime, Date endTime, int duration, double totalPrice, String status) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.parkingSpotId = parkingSpotId;
        this.cardId = cardId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.totalPrice = totalPrice;
        this.status = status;
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

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int durationInHours) {
        this.duration = durationInHours;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
