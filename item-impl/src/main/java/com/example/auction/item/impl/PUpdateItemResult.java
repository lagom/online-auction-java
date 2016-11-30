package com.example.auction.item.impl;


import com.example.auction.item.api.UpdateItemResultCodes;
import com.lightbend.lagom.serialization.Jsonable;

public final class PUpdateItemResult implements Jsonable{
    private final UpdateItemResultCodes code;
    private final PItem pItem;

    public PUpdateItemResult(UpdateItemResultCodes code, PItem pItem) {
        this.code = code;
        this.pItem = pItem;
    }

    public UpdateItemResultCodes getCode() {
        return code;
    }

    public PItem getItem() {
        return pItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PUpdateItemResult that = (PUpdateItemResult) o;

        if (code != that.code) return false;
        return pItem != null ? pItem.equals(that.pItem) : that.pItem == null;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (pItem != null ? pItem.hashCode() : 0);
        return result;
    }
}

