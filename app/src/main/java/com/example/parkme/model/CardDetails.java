package com.example.parkme.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CardDetails implements Parcelable {
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

    // Parcelable implementation
    protected CardDetails(Parcel in) {
        last4Digits = in.readString();
        expMonth = in.readInt();
        expYear = in.readInt();
        cardId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(last4Digits);
        dest.writeInt(expMonth);
        dest.writeInt(expYear);
        dest.writeString(cardId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CardDetails> CREATOR = new Creator<CardDetails>() {
        @Override
        public CardDetails createFromParcel(Parcel in) {
            return new CardDetails(in);
        }

        @Override
        public CardDetails[] newArray(int size) {
            return new CardDetails[size];
        }
    };
}
