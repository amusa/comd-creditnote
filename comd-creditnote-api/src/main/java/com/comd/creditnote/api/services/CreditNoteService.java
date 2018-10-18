/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comd.creditnote.api.services;

import com.comd.creditnote.lib.v1.CreditNote;
import com.sap.conn.jco.JCoException;
import java.util.List;

/**
 *
 * @author maliska
 */
public interface CreditNoteService {

    CreditNote creditNoteOfDelivery(String blDate, String customerId, String invoiceNo) throws JCoException;

    List<CreditNote> creditNotesOfCustomer(String customerId) throws JCoException;

    void utilize(String documentNumber);
    
    String post(String blDate, String vesselId, String customerId, String invoice, double amount) throws JCoException;

}
