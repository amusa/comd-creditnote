/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comd.creditnote.api.infrastructure.jco;

import com.comd.creditnote.lib.v1.CreditNote;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author maliska
 */
public class TestCreditNoteService {
    
    public List<CreditNote> creditNotesOfDelivery(String blDate, String customerId) throws ParseException {
        List<CreditNote> creditNotes = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        CreditNote creditNote = new CreditNote();
        creditNote.setCustomer(customerId);
        creditNote.setBlDate(sdf.parse(blDate));
        creditNote.setCreditNoteNo("010000900");
        creditNote.setDateIssue(sdf.parse("2016-06-19"));
        creditNote.setAmount(218000);
        creditNotes.add(creditNote);
        
        return creditNotes;
    }
}
