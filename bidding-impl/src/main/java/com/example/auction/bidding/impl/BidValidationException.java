package com.example.auction.bidding.impl;

import com.lightbend.lagom.javadsl.api.transport.TransportErrorCode;
import com.lightbend.lagom.javadsl.api.transport.TransportException;

/**
 * Exception thrown when a bid fails validation.
 */
public class BidValidationException extends TransportException {

    private static final long serialVersionUID = 1L;

    public BidValidationException(String message) {
        super(TransportErrorCode.PolicyViolation, message);
    }
}
