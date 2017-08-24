package com.example.auction.transaction.impl;

import com.example.auction.transaction.api.PaymentInfoStatus;

public enum PaymentStatus {
    APPROVED(PaymentInfoStatus.APPROVED), //{
//        @Override
        //PaymentInfoStatus toPaymentInfoStatus() {
  //          return PaymentInfoStatus.APPROVED;
//        }
//    },
    REJECTED(PaymentInfoStatus.REJECTED); //{
        //@Override
        //PaymentInfoStatus toPaymentInfoStatus() {
//            return PaymentInfoStatus.REJECTED;
//        }
//    };

    public final PaymentInfoStatus paymentInfoStatus;

    PaymentStatus(PaymentInfoStatus paymentInfoStatus) {
        this.paymentInfoStatus = paymentInfoStatus;
    }

    //abstract PaymentInfoStatus toPaymentInfoStatus();
}
