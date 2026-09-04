package com.priyanshu.RecoverAi.enums;

public enum RecoveryAction {

    RETRY_PAYMENT,
    SEND_REMINDER,
    SEND_PAYMENT_LINK,
    OFFER_ALTERNATIVE_PAYMENT_METHOD,
    OFFER_DISCOUNT,
    WAIT_AND_RETRY,
    ESCALATE_TO_HUMAN,
    STOP
}
