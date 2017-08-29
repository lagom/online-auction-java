package com.example.auction.user.impl;

import com.example.auction.user.impl.PUserCommand.CreatePUser;
import com.example.auction.user.impl.PUserCommand.GetPUser;
import com.example.auction.user.impl.PUserEvent.PUserCreated;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class PUserEntity extends PersistentEntity<PUserCommand, PUserEvent, Optional<PUser>> {

    @Override
    public Behavior initialBehavior(Optional<Optional<PUser>> snapshotState) {
        Optional<PUser> user = snapshotState.flatMap(Function.identity());

        if (user.isPresent()) {
            return created(user.get());
        } else {
            return notCreated();
        }
    }

    private Behavior created(PUser user) {
        BehaviorBuilder b = newBehaviorBuilder(Optional.of(user));

        b.setReadOnlyCommandHandler(GetPUser.class, (get, ctx) ->
                ctx.reply(state())
        );

        b.setReadOnlyCommandHandler(CreatePUser.class, (create, ctx) ->
                ctx.invalidCommand("User already exists.")
        );

        return b.build();
    }

    private Behavior notCreated() {
        BehaviorBuilder b = newBehaviorBuilder(Optional.empty());

        b.setReadOnlyCommandHandler(GetPUser.class, (get, ctx) ->
                ctx.reply(state())
        );

        b.setCommandHandler(CreatePUser.class, (create, ctx) -> {

            PUser user = new PUser(UUID.fromString(entityId()),  create.getName(), create.getEmail(), create.getPasswordHash());
            return ctx.thenPersist(new PUserCreated(user), (e) -> ctx.reply(Optional.ofNullable(user)));
        });

        b.setEventHandlerChangingBehavior(PUserCreated.class, user -> created(user.getUser()));

        return b.build();
    }
}
