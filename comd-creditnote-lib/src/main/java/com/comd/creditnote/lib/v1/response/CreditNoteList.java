/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comd.creditnote.lib.v1.response;

import com.comd.creditnote.lib.v1.CreditNote;
import java.util.List;

/**
 *
 * @author maliska
 */
public class CreditNoteList {

    private List<CreditNote> creditNotes;

    public List<CreditNote> getCreditNotes() {
        return creditNotes;
    }

    public void setCreditNotes(List<CreditNote> creditNotes) {
        this.creditNotes = creditNotes;
    }

}
