package controllers;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Constraints.Validate
public class SearchItemForm implements Constraints.Validatable<List<ValidationError>> {


    @Constraints.Required
    private int pageNumber = 0;
    private String keywords;
    @Constraints.Required
    private String maximumPriceCurrency = Currency.USD.name();
    @Constraints.Required
    private BigDecimal maximumPrice = BigDecimal.ZERO;


    private String itemStatus; // ???
    private String category; // ???
    private String username; // ???


    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();


        if (maximumPrice.doubleValue() < 0) {
            errors.add(new ValidationError("increment", "invalid.maxPrice.positive.or.zero"));
        }
        try {
            Currency.valueOf(maximumPriceCurrency);
        } catch (IllegalArgumentException e) {
            errors.add(new ValidationError("currency", "invalid.currency"));
        }

        if (errors.isEmpty()) {
            return null;
        } else {
            return errors;
        }
    }


    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getMaximumPriceCurrency() {
        return maximumPriceCurrency;
    }

    public void setMaximumPriceCurrency(String maximumPriceCurrency) {
        this.maximumPriceCurrency = maximumPriceCurrency;
    }

    public BigDecimal getMaximumPrice() {
        return maximumPrice;
    }

    public void setMaximumPrice(BigDecimal maximumPrice) {
        this.maximumPrice = maximumPrice;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

}
