package vibe.bizplan.bizplan_be_inclass.invoice.entity;

/**
 * Invoice status enumeration.
 */
public enum InvoiceStatus {
    DRAFT,    // Not yet sent
    SENT,     // Sent to customer
    PAID,     // Payment received
    OVERDUE   // Past due date
}

