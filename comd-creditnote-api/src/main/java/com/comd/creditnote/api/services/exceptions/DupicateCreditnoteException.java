package com.comd.creditnote.api.services.exceptions;

public class DupicateCreditnoteException extends RuntimeException {

    private String blDate;
    private String customerId;
    private String invoice;

    public DupicateCreditnoteException(String blDate, String customerId, String invoice) {
        this.blDate = blDate;
        this.customerId = customerId;
        this.invoice = invoice;
    }

    @Override
    public String getMessage() {
        return String.format("Creditnote already exist for customer: %s, B/L date: %s and Invoice#: %s",
                customerId, blDate, invoice);
    }

    public String getBlDate() {
        return blDate;
    }

    public void setBlDate(String blDate) {
        this.blDate = blDate;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getInvoice() {
        return invoice;
    }

    public void setInvoice(String invoice) {
        this.invoice = invoice;
    }

}
