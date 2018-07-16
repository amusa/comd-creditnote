/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comd.creditnote.lib.v1;

import java.util.Date;

/**
 *
 * @author maliska
 */
public class CreditNote {

    private String creditNoteNo;
    private String documentNo;
    private Date dateIssue;
    private String customer;
    private String invoiceNo;
    private Date blDate;
    private double amount;

    public String getCreditNoteNo() {
        return creditNoteNo;
    }

    public void setCreditNoteNo(String creditNoteNo) {
        this.creditNoteNo = creditNoteNo;
    }

    public Date getDateIssue() {
        return dateIssue;
    }

    public void setDateIssue(Date dateIssue) {
        this.dateIssue = dateIssue;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public Date getBlDate() {
        return blDate;
    }

    public void setBlDate(Date blDate) {
        this.blDate = blDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDocumentNo() {
        return documentNo;
    }

    public void setDocumentNo(String documentNo) {
        this.documentNo = documentNo;
    }

}
