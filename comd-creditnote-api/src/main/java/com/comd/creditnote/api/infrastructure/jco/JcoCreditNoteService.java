/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comd.creditnote.api.infrastructure.jco;

import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.comd.creditnote.api.services.CreditNoteService;
import com.comd.creditnote.lib.v1.CreditNote;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 *
 * @author maliska
 */
@ApplicationScoped
public class JcoCreditNoteService implements CreditNoteService {

    @Inject
    @ConfigProperty(name = "SAP_RFC_DESTINATION")
    private String sapRfcDestination;

    @Inject
    @ConfigProperty(name = "JCO_ASHOST")
    private String jcoHost;

    @Inject
    @ConfigProperty(name = "JCO_SYSNR")
    private String jcoSysNr;

    @Inject
    @ConfigProperty(name = "JCO_CLIENT")
    private String jcoClient;

    @Inject
    @ConfigProperty(name = "JCO_USER")
    private String jcoUser;

    @Inject
    @ConfigProperty(name = "JCO_PASSWD")
    private String jcoPassword;

    @Inject
    @ConfigProperty(name = "JCO_LANG")
    private String jcoLang;

    @Inject
    @ConfigProperty(name = "JCO_POOL_CAPACITY")
    private String jcoPoolCapacity;

    @Inject
    @ConfigProperty(name = "JCO_PEAK_LIMIT")
    private String jcoPeakLimit;

    @PostConstruct
    public void init() {
        Properties connectProperties = new Properties();
        connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, jcoHost);
        connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, jcoSysNr);
        connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, jcoClient);
        connectProperties.setProperty(DestinationDataProvider.JCO_USER, jcoUser);
        connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, jcoPassword);
        connectProperties.setProperty(DestinationDataProvider.JCO_LANG, jcoLang);
        connectProperties.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, jcoPoolCapacity);
        connectProperties.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, jcoPeakLimit);
        createDestinationDataFile(sapRfcDestination, connectProperties);
    }

    private void createDestinationDataFile(String destinationName, Properties connectProperties) {
        File destCfg = new File(destinationName + ".jcoDestination");

        try {
            FileOutputStream fos = new FileOutputStream(destCfg, false);
            connectProperties.store(fos, "SAP jco destination config");
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException("Unable to create the destination files", e);
        }
    }

    @Override
    public List<CreditNote> creditNotesOfDelivery(String blDate, String customerId) throws JCoException {
        List<CreditNote> creditNotes = new ArrayList<>();

        JCoDestination destination = JCoDestinationManager.getDestination(sapRfcDestination);
        JCoFunction function = destination.getRepository().getFunction("BAPI_DELIVERY_GETLIST");
        if (function == null) {
            throw new RuntimeException("BAPI_DELIVERY_GETLIST not found in SAP.");
        }

        if (customerId != null) {
            new JCoTableSelectOption(function, "IT_KUNNR")
                    .withField("CUSTOMER_VENDOR_LOW")
                    .withValue(customerId)
                    .withSign("I")
                    .withOption("EQ")
                    .build();
        }

        if (blDate != null) {
            new JCoTableSelectOption(function, "IT_WADAT")
                    .withField("CGI_DATE_LOW")
                    .withValue(blDate)
                    .withSign("I")
                    .withOption("EQ")
                    .build();
        }

        try {
            function.execute(destination);
        } catch (AbapException e) {
            System.out.println(e.toString());
            return null; //TODO:fix
        }

        JCoTable returnTable = function.getTableParameterList().getTable("RETURN");

        for (int i = 0; i < returnTable.getNumRows(); i++) {
            if (!(returnTable.getString("TYPE").equals("") || returnTable.getString("TYPE").equals("S"))) {
                throw new RuntimeException(returnTable.getString("MESSAGE"));
            }
        }

        JCoTable codes = function.getTableParameterList().getTable("ET_DELIVERY_HEADER");

        for (int i = 0; i < codes.getNumRows(); i++, codes.nextRow()) {
            CreditNote creditNote = new CreditNote();
            

            creditNotes.add(creditNote);

            System.out.println(codes.getString("VBELN") + '\t'
                    + codes.getString("INCO2") + '\t'
                    + codes.getString("KUNNR") + '\t'
                    + codes.getString("WADAT_IST") + '\t'
                    + codes.getString("WAERK") + '\t'
                    + codes.getString("NETWR"));
        }//for

        return creditNotes;
    }

    @Override
    public List<CreditNote> creditNotesOfCustomer(String customerId) throws JCoException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void utilize(String documentNumber) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
