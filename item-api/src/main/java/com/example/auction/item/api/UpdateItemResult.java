package com.example.auction.item.api;


public final class UpdateItemResult {
    private UpdateItemResultCodes code;
    private Item item;

    public UpdateItemResult(UpdateItemResultCodes code, Item item) {
        this.code = code;
        this.item = item;
    }

    public UpdateItemResultCodes getCode() {
        return code;
    }

    public Item getItem() {
        return item;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UpdateItemResult that = (UpdateItemResult) o;

        if (code != that.code) return false;
        return item != null ? item.equals(that.item) : that.item == null;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (item != null ? item.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UpdateItemResult{" +
                "code=" + code +
                ", item=" + item +
                '}';
    }
}

