package controllers;

import com.example.auction.user.api.User;
import org.pcollections.PSequence;

import java.util.Optional;

public class Nav {

    private final PSequence<User> users;
    private final Optional<User> user;

    public Nav(PSequence<User> users, Optional<User> user) {
        this.users = users;
        this.user = user;
    }

    public PSequence<User> getUsers() {
        return users;
    }

    public Optional<User> getUser() {
        return user;
    }
}
