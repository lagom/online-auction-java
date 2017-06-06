package controllers;

import play.data.validation.Constraints;

public class CreateUserForm {

    @Constraints.Required
    private String username;
    @Constraints.Required
    private String name;
    @Constraints.Required
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
