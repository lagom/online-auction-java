package controllers;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ItemForm {

    private String id;
    @Constraints.Required
    private String title;
    @Constraints.Required
    private String description;
    @Constraints.Required
    private String currency = "USD";
    @Constraints.Required
    private BigDecimal increment = BigDecimal.valueOf(0.5);
    @Constraints.Required
    private BigDecimal reserve = BigDecimal.ZERO;
    @Constraints.Required
    private int duration = 10;
    @Constraints.Required
    private String durationUnits;

    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();
        
        try {
            Currency c = Currency.valueOf(currency);
            if (!c.isValidStep(increment.doubleValue())) {
                errors.add(new ValidationError("increment", "invalid.step"));
            }
            if (!c.isValidStep(reserve.doubleValue())) {
                errors.add(new ValidationError("reserve", "invalid.step"));
            }
            if (!c.isValidIncrement(increment.doubleValue())) {
                errors.add(new ValidationError("increment", "invalid.increment"));
            }
        } catch (IllegalArgumentException e) {
            errors.add(new ValidationError("currency", "invalid.currency"));
        }

        try {
            int reserveInt = reserve.multiply(BigDecimal.valueOf(2)).intValueExact();
            if (reserveInt < 0) {
                errors.add(new ValidationError("reserve", "invalid.reserve"));
            } else if (reserveInt >= 2000000) {
                errors.add(new ValidationError("reserve", "invalid.reserve"));
            }
        } catch (ArithmeticException e) {
            errors.add(new ValidationError("reserve", "invalid.reserve"));
        }

        try {
            ChronoUnit chrono = ChronoUnit.valueOf(durationUnits);
            Duration d = Duration.of(duration, chrono);
            if (d.compareTo(Duration.ofDays(7)) > 0) {
                errors.add(new ValidationError("duration", "invalid.duration"));
            }
            if (d.compareTo(Duration.ofSeconds(10)) < 0) {
                errors.add(new ValidationError("duration", "invalid.duration"));
            }
        } catch (IllegalArgumentException e) {
            errors.add(new ValidationError("durationUnits", "invalid.units"));
        }

        if (errors.isEmpty()) {
            return null;
        } else {
            return errors;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getIncrement() {
        return increment;
    }

    public void setIncrement(BigDecimal increment) {
        this.increment = increment;
    }

    public BigDecimal getReserve() {
        return reserve;
    }

    public void setReserve(BigDecimal reserve) {
        this.reserve = reserve;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDurationUnits() {
        return durationUnits;
    }

    public void setDurationUnits(String durationUnits) {
        this.durationUnits = durationUnits;
    }
}
