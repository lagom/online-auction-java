package com.example.auction.user.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import java.util.Optional;
import java.util.function.Function;

public class CredentialEntity extends PersistentEntity<CredentialCommand, CredentialEvent, Optional<PCredential>> {

    @Override
    public Behavior initialBehavior(Optional<Optional<PCredential>> snapshotState) {

        Optional<PCredential> credential = snapshotState.flatMap(Function.identity());

        if (credential.isPresent()) {
            return created(credential.get());
        } else {
            return notCreated();
        }
    }

    private Behavior created(PCredential pCredential) {
        BehaviorBuilder b = newBehaviorBuilder(Optional.of(pCredential));

        b.setReadOnlyCommandHandler(CredentialCommand.Login.class, (get, ctx) ->
                ctx.reply(state())
        );

        b.setCommandHandler(CredentialCommand.UpdateCredential.class, (update, ctx) -> {
            PCredential a = new PCredential(update.getId(), update.getUsername(), update.getPassword());
            return ctx.thenPersist(new CredentialEvent.CredentialUpdated(a), (e) -> ctx.reply(Done.getInstance()));
        });
        return b.build();
    }

    private Behavior notCreated() {
        BehaviorBuilder b = newBehaviorBuilder(Optional.empty());

        b.setReadOnlyCommandHandler(CredentialCommand.Login.class, (get, ctx) ->
                ctx.invalidCommand("User does not exists.")
        );
        b.setCommandHandler(CredentialCommand.UpdateCredential.class, (update, ctx) -> {
            PCredential a = new PCredential(update.getId(), update.getUsername(), update.getPassword());
            return ctx.thenPersist(new CredentialEvent.CredentialUpdated(a), (e) -> ctx.reply(Done.getInstance()));
        });
        b.setEventHandlerChangingBehavior(CredentialEvent.CredentialUpdated.class, credUser -> created(credUser.getPCredential()));
        return b.build();
    }
}
