package com.danamon.autochain.constant.invoice;

public enum InvoiceStatus {
    PENDING // waiting for approval
    , DISPUTED // Rejected by buyer
    , CANCELLED // Invoice cancel by seller
    , UNPAID // due date and created date but not paid
    , PAID // Invoice already paid
    , LATE_UNPAID // Past due date but not paid yet
    , LATE_PAID //Invoice already paid but late
}
