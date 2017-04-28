package controllers;

import play.data.validation.Constraints;

public class CreateUserForm {

    @Constraints.Required
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
