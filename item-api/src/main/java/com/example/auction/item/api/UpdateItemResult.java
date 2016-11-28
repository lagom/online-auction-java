package com.example.auction.item.api;


public final class UpdateItemResult {
    private UpdateItemResultCodes code;

    public UpdateItemResult(UpdateItemResultCodes code) {
        this.code = code;
    }

    public UpdateItemResultCodes getCode() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UpdateItemResult that = (UpdateItemResult) o;

        return code == that.code;
    }

    @Override
    public int hashCode() {
        return code != null ? code.hashCode() : 0;
    }

}

