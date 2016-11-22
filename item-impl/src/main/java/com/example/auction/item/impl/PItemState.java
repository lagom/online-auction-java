package com.example.auction.item.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.Jsonable;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class PItemState implements Jsonable {

    private final Optional<PItem> item;

    @JsonCreator
    private PItemState(Optional<PItem> item) {
        this.item = item;
    }

    public static PItemState empty() {
        return new PItemState(Optional.empty());
    }

    public static PItemState create(PItem item) {
        return new PItemState(Optional.of(item));
    }

    private PItemState update(Function<PItem, PItem> updateFunction) {
        assert item.isPresent();
        return new PItemState(item.map(updateFunction));
    }

    public PItemState start(Instant startTime) {
        return update(i -> i.start(startTime));
    }

    public PItemState end(Optional<UUID> winner, int price) {
        return update(i -> i.end(winner, price));
    }

    public PItemState updatePrice(int price) {
        return update(i -> i.updatePrice(price));
    }

    public PItemState cancel() {
        return update(i -> i.cancel());
    }

    public Optional<PItem> getItem() {
        return item;
    }

    public PItemStatus getStatus() {
        return item.map(PItem::getStatus).orElse(PItemStatus.NOT_CREATED);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PItemState that = (PItemState) o;

        return item != null ? item.equals(that.item) : that.item == null;

    }

    @Override
    public int hashCode() {
        return item != null ? item.hashCode() : 0;
    }
}
