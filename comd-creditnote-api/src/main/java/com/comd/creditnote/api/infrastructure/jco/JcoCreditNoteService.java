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
import com.sap.conn.jco.JCoStructure;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private static final Logger logger = Logger.getLogger(JcoCreditNoteService.class.getName());

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

        logger.log(Level.INFO, "Service initialized... {0}", connectProperties);
    }

    private void createDestinationDataFile(String destinationName, Properties connectProperties) {
        File destCfg = new File(destinationName + ".jcoDestination");

        try {
            FileOutputStream fos = new FileOutputStream(destCfg, false);
            connectProperties.store(fos, "SAP jco destination config");
            fos.close();
        } catch (IOException e) {
            logger.log(Level.INFO, "Unable to create the destination files");
            throw new RuntimeException("Unable to create the destination files", e);
        }
    }

    @Override
    public List<CreditNote> creditNotesOfDelivery(String blDate, String customerId) throws JCoException {
        List<CreditNote> creditNotes = new ArrayList<>();

        JCoDestination destination = JCoDestinationManager.getDestination(sapRfcDestination);
        JCoFunction function = destination.getRepository().getFunction("BAPI_ACC_DOCUMENT_POST");
        if (function == null) {
            throw new RuntimeException("BAPI_ACC_DOCUMENT_POST not found in SAP.");
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

    }

    @Override
    public String post(String blDate, String vesselId, String customerId, String invoice, double amount) throws JCoException {
        logger.log(Level.INFO, 
                "Service invoked with parameters: blDate={0}, vesselId={1}, customerId={2}, Invoice#={3}, Amount={4}",
                new Object[]{blDate, vesselId, customerId, invoice, amount});

        String returnMessage = null;
        JCoDestination destination = JCoDestinationManager.getDestination(sapRfcDestination);
        JCoFunction function = destination.getRepository().getFunction("BAPI_ACC_DOCUMENT_POST");

        if (function == null) {
            logger.log(Level.INFO, "BAPI_ACC_DOCUMENT_POST not found in SAP.");
            throw new RuntimeException("BAPI_ACC_DOCUMENT_POST not found in SAP.");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        //HEADER
        JCoStructure header = function.getImportParameterList().getStructure("DOCUMENTHEADER");
        header.setValue("USERNAME", "amusa"); //TODO:use login user name
        header.setValue("HEADER_TXT",
                String.format("42CREDIT NOTE FOR CUSTOMER %s INVOICE %s",
                        new Object[]{customerId, invoice}));
        header.setValue("COMP_CODE", "0140");
        header.setValue("DOC_DATE", blDate);
        header.setValue("PSTNG_DATE", sdf.format(new Date()));
        header.setValue("DOC_TYPE", "DG");
        header.setValue("REF_DOC_NO", invoice);

        logger.log(Level.INFO, "BAPI_ACC_DOCUMENT_POST header set {0}.", header);

        //ACCTOUNT GL
        JCoTable accountGl = function.getTableParameterList().getTable("ACCOUNTGL");
        accountGl.appendRow();
        accountGl.setValue("ITEMNO_ACC", "001");
        accountGl.setValue("GL_ACCOUNT", "0005140002");
        accountGl.setValue("ITEM_TEXT",
                String.format("CREDIT NOTE FOR CUSTOMER %s INVOICE %s",
                        new Object[]{customerId, invoice}));
        accountGl.setValue("DOC_TYPE", "DG");
        accountGl.setValue("COMP_CODE", "0140");
        accountGl.setValue("PSTNG_DATE", sdf.format(new Date()));
        accountGl.setValue("VALUE_DATE", blDate);
        accountGl.setValue("CUSTOMER", customerId);
        accountGl.setValue("COSTCENTER", "C00214A000");
        accountGl.setValue("ALLOC_NMBR", invoice);

        logger.log(Level.INFO, "BAPI_ACC_DOCUMENT_POST Account GL set {0}.", accountGl);

        //ACCOUNT RECEIVABLE
        JCoTable accountReceivable = function.getTableParameterList().getTable("ACCOUNTRECEIVABLE");
        accountReceivable.appendRow();
        accountReceivable.setValue("ITEMNO_ACC", "002");
        accountReceivable.setValue("CUSTOMER", customerId);
        accountReceivable.setValue("ITEM_TEXT",
                String.format("CREDIT NOTE FOR CUSTOMER %s INVOICE %s",
                        new Object[]{customerId, invoice}));
        accountReceivable.setValue("TAX_CODE", "V0");
        accountReceivable.setValue("PMTMTHSUPL", "42");
        accountReceivable.setValue("PYMT_METH", "L");

        logger.log(Level.INFO, "BAPI_ACC_DOCUMENT_POST Account Receivable set {0}.", accountReceivable);

        //CURRENCY AMOUNT
        JCoTable currencyAmount = function.getTableParameterList().getTable("CURRENCYAMOUNT");
        //Debit leg
        currencyAmount.appendRow();
        currencyAmount.setValue("ITEMNO_ACC", "001");
        currencyAmount.setValue("CURRENCY", "USD.");
        currencyAmount.setValue("AMT_DOCCUR", Math.abs(amount) * -1);

        logger.log(Level.INFO, "BAPI_ACC_DOCUMENT_POST Debit CurrencyAmount set {0}.", currencyAmount);

        //Credit leg
        currencyAmount.appendRow();
        currencyAmount.setValue("ITEMNO_ACC", "002");
        currencyAmount.setValue("CURRENCY", "USD.");
        currencyAmount.setValue("AMT_DOCCUR", Math.abs(amount));

        logger.log(Level.INFO, "BAPI_ACC_DOCUMENT_POST Credit CurrencyAmount set {0}.", currencyAmount);

        try {
            function.execute(destination);
        } catch (AbapException e) {
            logger.log(Level.SEVERE, "Error executing BAPI_ACC_DOCUMENT_POST.");
        }

        JCoTable returnTable = function.getTableParameterList().getTable("RETURN");

        StringBuilder sb = new StringBuilder();
        boolean isError = false;

        for (int i = 0; i < returnTable.getNumRows(); i++) {
            if (!(returnTable.getString("TYPE").equals("") || returnTable.getString("TYPE").equals("S"))) {
                isError = true;
            }
            sb.append(returnTable.getString("MESSAGE")).append("\n");
        }

        returnMessage = sb.toString();

        if (isError) {
            logger.log(Level.SEVERE, returnMessage);
            throw new RuntimeException(returnMessage);
        }

        logger.log(Level.INFO, returnMessage);

        return returnMessage;
    }

}
