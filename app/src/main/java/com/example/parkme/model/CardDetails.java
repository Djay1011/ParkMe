package com.example.parkme.model;

public class CardDetails {
    private String last4Digits;
    private int expMonth;
    private int expYear;
    private String cardId;

    public CardDetails() {
    }

    public CardDetails(String last4Digits, int expMonth, int expYear, String cardId) {
        this.last4Digits = last4Digits;
        this.expMonth = expMonth;
        this.expYear = expYear;
        this.cardId = cardId;
    }


    public String getLast4Digits() {
        return last4Digits;
    }

    public void setLast4Digits(String last4Digits) {
        this.last4Digits = last4Digits;
    }

    public int getExpMonth() {
        return expMonth;
    }

    public void setExpMonth(int expMonth) {
        this.expMonth = expMonth;
    }

    public int getExpYear() {
        return expYear;
    }

    public void setExpYear(int expYear) {
        this.expYear = expYear;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }
}
