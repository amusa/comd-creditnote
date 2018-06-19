/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comd.creditnote.api.infrastructure.jco;

import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;

/**
 *
 * @author maliska
 */
public class JCoTableSelectOption {

    private final JCoFunction function;
    private final JCoTable record;   
    private String field;
    private String value;
    private String sign;
    private String option;

    public JCoTableSelectOption(JCoFunction func, String tab) {
        this.function = func;       
        this.record = function.getTableParameterList().getTable(tab);
    }

    public JCoTableSelectOption withSign(String sgn) {
        this.sign = sgn;
        return this;
    }

    public JCoTableSelectOption withOption(String opt) {
        this.option = opt;
        return this;
    }

    public JCoTableSelectOption withField(String fld) {
        this.field = fld;
        return this;
    }

    public JCoTableSelectOption withValue(String val) {
        this.value = val;
        return this;
    }

    public JCoTable build() {
        record.appendRow();
        record.setValue("SIGN", this.sign);
        record.setValue("OPTION", this.option);
        record.setValue(field, this.value);

        return record;
    }
}
