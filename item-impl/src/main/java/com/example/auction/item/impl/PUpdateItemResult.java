package com.example.auction.item.impl;


import com.example.auction.item.api.UpdateItemResultCodes;
import com.lightbend.lagom.serialization.Jsonable;

public final class PUpdateItemResult implements Jsonable{
    private UpdateItemResultCodes code;

    public PUpdateItemResult(UpdateItemResultCodes code) {
        this.code = code;
    }


    public UpdateItemResultCodes getCode() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PUpdateItemResult that = (PUpdateItemResult) o;

        return code == that.code;
    }

    @Override
    public int hashCode() {
        return code != null ? code.hashCode() : 0;
    }

}

