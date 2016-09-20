package com.example.auction.user.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;
import java.util.UUID;

public final class User {

    private final UUID id;
    private final String name;

    @JsonCreator
    private User(@JsonProperty("id") Optional<UUID> id, @JsonProperty("name") String name) {
        this.id = id.orElse(null);
        this.name = name;
    }

    public User(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Used when creating a new user.
     */
    public User(String name) {
        this.id = null;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != null ? !id.equals(user.id) : user.id != null) return false;
        return name.equals(user.name);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
