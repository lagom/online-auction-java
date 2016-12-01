package com.example.auction.item.impl;

public class UpdateFailureException extends RuntimeException {

    private String message;

    public UpdateFailureException(String message) {
        super(message);
        this.message = message;
    }


    // ------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UpdateFailureException that = (UpdateFailureException) o;

        return message != null ? message.equals(that.message) : that.message == null;
    }

    @Override
    public int hashCode() {
        return message != null ? message.hashCode() : 0;
    }
}
