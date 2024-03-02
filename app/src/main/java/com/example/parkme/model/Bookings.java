package com.example.parkme.model;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Bookings {
    private String bookingId;
    private String userId;
    private String parkingSpotId;
    private String parkingSpotName;
    private String cardId;
    private Date startTime;
    private Date endTime;
    private int duration;
    private double totalPrice;
    private String status;
    public enum BookingStatus {
        UPCOMING, INPROGRESS, COMPLETED, CANCELLED
    }

    public Bookings() {
    }

    public Bookings(String bookingId, String userId, String parkingSpotId, String parkingSpotName, String cardId, Date startTime, Date endTime, int duration, double totalPrice, String status) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.parkingSpotId = parkingSpotId;
        this.parkingSpotName = parkingSpotName;
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

    public String getParkingSpotName() {
        return parkingSpotName;
    }

    public void setParkingSpotName(String parkingSpotName) {
        this.parkingSpotName = parkingSpotName;
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

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("bookingId", bookingId);
        result.put("userId", userId);
        result.put("parkingSpotId", parkingSpotId);
        result.put("parkingSpotName", parkingSpotName);
        result.put("cardId", cardId);
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        result.put("duration", duration);
        result.put("totalPrice", totalPrice);
        result.put("status", status);
        return result;
    }
}
