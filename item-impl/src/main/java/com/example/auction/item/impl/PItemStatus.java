package com.example.auction.item.impl;

import com.example.auction.item.api.ItemStatus;

public enum PItemStatus {
    NOT_CREATED {
        @Override
        ItemStatus toItemStatus() {
            throw new IllegalStateException("Publicly exposed item can't be not created");
        }
    },
    CREATED {
        @Override
        ItemStatus toItemStatus() {
            return ItemStatus.CREATED;
        }
    },
    AUCTION {
        @Override
        ItemStatus toItemStatus() {
            return ItemStatus.AUCTION;
        }
    },
    COMPLETED {
        @Override
        ItemStatus toItemStatus() {
            return ItemStatus.COMPLETED;
        }
    },
    CANCELLED {
        @Override
        ItemStatus toItemStatus() {
            return ItemStatus.CANCELLED;
        }
    };

    abstract ItemStatus toItemStatus();
}
