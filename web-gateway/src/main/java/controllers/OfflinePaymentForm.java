package controllers;

import lombok.Data;
import play.data.validation.Constraints;

@Data
public class OfflinePaymentForm {
    @Constraints.Required
    private String comment;
}
