package controllers;

import java.text.NumberFormat;
import java.util.Locale;

public enum Currency {

    USD(50, Locale.US),
    EUR(50, Locale.GERMANY),
    GBP(50, Locale.UK),
    JPY(50, Locale.JAPAN),
    CNY(100, Locale.CHINA),
    CAD(50, Locale.CANADA),
    AUD(50, Locale.forLanguageTag("en-AU"));

    private final int step;
    private final java.util.Currency currency;
    private final NumberFormat format;

    Currency(int step, Locale locale) {
        this.step = step;
        this.currency = java.util.Currency.getInstance(name());
        this.format = NumberFormat.getCurrencyInstance(locale);
    }

    public String format(int value) {
        return format.format(toDecimal(value));
    }

    public String getDisplayName() {
        return currency.getDisplayName();
    }

    private double toDecimal(int value) {
        if (currency.getDefaultFractionDigits() > 0) {
            return ((double) value) / Math.pow(10, currency.getDefaultFractionDigits());
        } else {
            return value;
        }
    }

    public double getDecimalStep() {
        return toDecimal(step);
    }

    public int toPriceUnits(double value) {
        if (currency.getDefaultFractionDigits() > 0) {
            return (int) Math.round(value * Math.pow(10, currency.getDefaultFractionDigits()));
        } else {
            return (int) Math.round(value);
        }
    }

    public boolean isValidStep(double value) {
        int price = toPriceUnits(value);
        return price % step == 0;
    }
}
