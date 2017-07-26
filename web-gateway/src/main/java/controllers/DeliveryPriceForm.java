package controllers;

import lombok.Data;
import play.data.validation.Constraints;

@Data
public class DeliveryPriceForm {
    @Constraints.Required
    private int deliveryPrice;
}
