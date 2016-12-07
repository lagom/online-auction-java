package controllers;

import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

public class SearchItemForm {

    private String itemStatus;
    private String category;

    private String keywords;

    private int pageNumber = 0;

    private String currency; // ???
    private int maximumPrice; // ???
    private String username; // ???


    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (errors.isEmpty()) {
            return null;
        } else {
            return errors;
        }
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getMaximumPrice() {
        return maximumPrice;
    }

    public void setMaximumPrice(int maximumPrice) {
        this.maximumPrice = maximumPrice;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

}
