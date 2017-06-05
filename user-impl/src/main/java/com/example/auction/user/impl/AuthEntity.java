package com.example.auction.user.impl;

import com.example.auction.user.api.Auth;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import java.util.Optional;
import java.util.function.Function;

public class AuthEntity extends PersistentEntity<AuthCommand, AuthEvent, Optional<Auth>> {

    @Override
    public Behavior initialBehavior(Optional<Optional<Auth>> snapshotState) {
        Optional<Auth> auth = snapshotState.flatMap(Function.identity());

        BehaviorBuilder b = newBehaviorBuilder(Optional.empty());

        b.setReadOnlyCommandHandler(AuthCommand.GetAuth.class, (get, ctx) ->
                ctx.reply(state())
        );

        b.setCommandHandler(AuthCommand.UpdateAuth.class, (update, ctx) -> {
            Auth a = new Auth(update.getId(), update.getUsername(), update.getPassword());
            return ctx.thenPersist(new AuthEvent.AuthUpdated(a), (e) -> ctx.reply(a));
        });

        return b.build();
    }

}
