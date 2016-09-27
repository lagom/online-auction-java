package controllers;

import java.math.BigDecimal;
import java.time.Instant;

public class AnonymousBid {

    private final Instant bidTime;
    private final int bidPrice;
    private final int bidder;
    private final boolean isYou;

    public AnonymousBid(Instant bidTime, int bidPrice, int bidder, boolean isYou) {
        this.bidTime = bidTime;
        this.bidPrice = bidPrice;
        this.bidder = bidder;
        this.isYou = isYou;
    }

    public Instant getBidTime() {
        return bidTime;
    }

    public int getBidPrice() {
        return bidPrice;
    }

    public int getBidder() {
        return bidder;
    }

    public boolean isYou() {
        return isYou;
    }
}
