/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comd.creditnote.lib.v1.request;

/**
 *
 * @author maliska
 */
public class PostCreditNoteRequest {

    private String blDate;
    private String vesselId;
    private String customerId;
    private String invoice;
    private double amount;

    public PostCreditNoteRequest() {
    }

    public String getBlDate() {
        return blDate;
    }

    public void setBlDate(String blDate) {
        this.blDate = blDate;
    }

    public String getVesselId() {
        return vesselId;
    }

    public void setVesselId(String vesselId) {
        this.vesselId = vesselId;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

}
