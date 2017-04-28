package controllers;

import play.data.validation.Constraints;

import java.math.BigDecimal;

public class BidForm {

    private BigDecimal bid;
    @Constraints.Required
    private String currency;

    public BigDecimal getBid() {
        return bid;
    }

    public void setBid(BigDecimal bid) {
        this.bid = bid;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
