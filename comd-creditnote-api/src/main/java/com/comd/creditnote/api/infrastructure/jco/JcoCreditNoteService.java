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
import com.comd.creditnote.api.services.exceptions.DupicateCreditnoteException;
import com.comd.creditnote.api.services.exceptions.EmptyPayloadException;
import com.comd.creditnote.lib.v1.CreditNote;
import com.sap.conn.jco.JCoContext;
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
    private CreditNoteNumberGenerator generator;

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
    public CreditNote creditNoteOfDelivery(String blDate, String customerId, String invoiceNo) throws JCoException {

        JCoDestination destination = JCoDestinationManager.getDestination(sapRfcDestination);
        JCoFunction function = destination.getRepository().getFunction("ZCREDITNOTE_GETDETAIL");
        if (function == null) {
            throw new RuntimeException("ZCREDITNOTE_GETDETAIL not found in SAP.");
        }

        function.getImportParameterList().setValue("KUNNR", customerId);
        function.getImportParameterList().setValue("BLDAT", blDate);
        function.getImportParameterList().setValue("REF_DOC", invoiceNo);

        try {
            function.execute(destination);
        } catch (AbapException e) {
            System.out.println(e.toString());
            return null; //TODO:fix
        }

//        JCoStructure returnStructure = function.getExportParameterList().getStructure("RETURN");
//        if (!(returnStructure.getString("TYPE").equals("") || returnStructure.getString("TYPE").equals("S"))) {
//            throw new RuntimeException(returnStructure.getString("MESSAGE"));
//        }
        JCoStructure creditNoteStructure = function.getExportParameterList().getStructure("CREDITNOTE");

        CreditNote creditNote = new CreditNote();
        creditNote.setCustomer(creditNoteStructure.getString("KUNNR"));
        creditNote.setDocumentNo(creditNoteStructure.getString("BELNR"));
        creditNote.setDateIssue(creditNoteStructure.getDate("BUDAT"));
        creditNote.setBlDate(creditNoteStructure.getDate("BLDAT"));
        creditNote.setInvoiceNo(creditNoteStructure.getString("XBLNR"));
        creditNote.setAmount(creditNoteStructure.getDouble("WRBTR"));
        creditNote.setCreditNoteNo(creditNoteStructure.getString("SGTXT"));

        return creditNote;
    }

    @Override
    public List<CreditNote> creditNotesOfCustomer(String customerId) throws JCoException {
        List<CreditNote> creditNotes = new ArrayList<>();

        JCoDestination destination = JCoDestinationManager.getDestination(sapRfcDestination);
        JCoFunction function = destination.getRepository().getFunction("ZCREDITNOTE_GETLIST");
        if (function == null) {
            throw new RuntimeException("ZCREDITNOTE_GETLIST not found in SAP.");
        }

        function.getImportParameterList().setValue("KUNNR", customerId);

        try {
            function.execute(destination);
        } catch (AbapException e) {
            System.out.println(e.toString());
            return null; //TODO:fix
        }

//        JCoStructure returnStructure = function.getExportParameterList().getStructure("RETURN");
//        if (!(returnStructure.getString("TYPE").equals("") || returnStructure.getString("TYPE").equals("S"))) {
//            throw new RuntimeException(returnStructure.getString("MESSAGE"));
//        }
        JCoTable creditNoteTable = function.getTableParameterList().getTable("CREDITNOTES");

        if (creditNoteTable.isEmpty()) {
            logger.log(Level.WARNING, "No data returned from server");
            throw new EmptyPayloadException("No data returned from server");
        }

        for (int i = 0; i < creditNoteTable.getNumRows(); i++, creditNoteTable.nextRow()) {
            logger.log(Level.INFO,
                    "CREDIT NOTE: CUSTOMER={0}, DOCNO={1}, PSTG DATE={2}, BL/DATE={3}, BILLING DOC={4}, AMOUNT={5}, CREDITNOTE NO={6}",
                    new Object[]{
                        creditNoteTable.getString("KUNNR"),
                        creditNoteTable.getString("BELNR"),
                        creditNoteTable.getDate("BUDAT"),
                        creditNoteTable.getDate("BLDAT"),
                        creditNoteTable.getString("XBLNR"),
                        creditNoteTable.getDouble("WRBTR"),
                        creditNoteTable.getString("SGTXT")
                    }
            );

            CreditNote creditNote = new CreditNote();
            creditNote.setCustomer(creditNoteTable.getString("KUNNR"));
            creditNote.setDocumentNo(creditNoteTable.getString("BELNR"));
            creditNote.setDateIssue(creditNoteTable.getDate("BUDAT"));
            creditNote.setBlDate(creditNoteTable.getDate("BLDAT"));
            creditNote.setInvoiceNo(creditNoteTable.getString("XBLNR"));
            creditNote.setAmount(creditNoteTable.getDouble("WRBTR"));
            creditNote.setCreditNoteNo(creditNoteTable.getString("SGTXT"));

            creditNotes.add(creditNote);
        }

        return creditNotes;

    }

    @Override
    public void utilize(String documentNumber) {

    }

    @Override
    public String post(String blDate, String vesselId, String customerId, String invoice, double amount) throws JCoException {
        logger.log(Level.INFO,
                "Service invoked with parameters: blDate={0}, vesselId={1}, customerId={2}, Invoice#={3}, Amount={4}",
                new Object[]{blDate, vesselId, customerId, invoice, amount});

        logger.log(Level.INFO, "-- checking creditnote duplicity ---");

        CreditNote creditNote = creditNoteOfDelivery(blDate, customerId, invoice);
        if (creditNote != null) {
            logger.log(Level.INFO,
                    "--- creditnote already exist for customer: {0}, B/L date: {2} and Invoice#: {3}",
                    new Object[]{customerId, blDate, invoice});

            throw new DupicateCreditnoteException(blDate, customerId, invoice);
        }

        String returnMessage = null;
        JCoDestination destination = JCoDestinationManager.getDestination(sapRfcDestination);
        JCoFunction function = destination.getRepository().getFunction("BAPI_ACC_DOCUMENT_POST");
        JCoFunction bapiTransactionCommit = destination.getRepository().getFunction("BAPI_TRANSACTION_COMMIT");
        JCoFunction bapiTransactionRollback = destination.getRepository().getFunction("BAPI_TRANSACTION_ROLLBACK");

        if (function == null) {
            logger.log(Level.INFO, "BAPI_ACC_DOCUMENT_POST not found in SAP.");
            throw new RuntimeException("BAPI_ACC_DOCUMENT_POST not found in SAP.");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        String creditNoteNo = generator.nextNumber();

        //HEADER
        JCoStructure header = function.getImportParameterList().getStructure("DOCUMENTHEADER");
        header.setValue("USERNAME", jcoUser); //TODO:use login user name
        header.setValue("HEADER_TXT",
                String.format("42CREDITNOTE FOR INVOICE %s",
                        invoice));
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
        accountGl.setValue("ITEM_TEXT", creditNoteNo);
        accountGl.setValue("DOC_TYPE", "DG");
        accountGl.setValue("COMP_CODE", "0140");
        accountGl.setValue("PSTNG_DATE", sdf.format(new Date()));
        accountGl.setValue("VALUE_DATE", blDate);
        accountGl.setValue("CUSTOMER", customerId);
        //accountGl.setValue("COSTCENTER", "C00214A000");
        //accountGl.setValue("PROFIT_CTR", "P002147001");
        accountGl.setValue("ALLOC_NMBR", invoice);
        accountGl.setValue("TAX_CODE", "V0");

        logger.log(Level.INFO, "BAPI_ACC_DOCUMENT_POST Account GL set {0}.", accountGl);

        //ACCOUNT RECEIVABLE
        JCoTable accountReceivable = function.getTableParameterList().getTable("ACCOUNTRECEIVABLE");
        accountReceivable.appendRow();
        accountReceivable.setValue("ITEMNO_ACC", "002");
        accountReceivable.setValue("CUSTOMER", customerId);
        accountReceivable.setValue("ITEM_TEXT", creditNoteNo);
        accountReceivable.setValue("TAX_CODE", "V0");
        accountReceivable.setValue("PMTMTHSUPL", "42");
        accountReceivable.setValue("PYMT_METH", "L");
        //accountReceivable.setValue("PROFIT_CTR", "P002147001");

        logger.log(Level.INFO, "BAPI_ACC_DOCUMENT_POST Account Receivable set {0}.", accountReceivable);

        //CURRENCY AMOUNT
        JCoTable currencyAmount = function.getTableParameterList().getTable("CURRENCYAMOUNT");
        //Debit leg
        currencyAmount.appendRow();
        currencyAmount.setValue("ITEMNO_ACC", "001");
        currencyAmount.setValue("CURRENCY", "USD.");
        currencyAmount.setValue("AMT_DOCCUR", Math.abs(amount));

        logger.log(Level.INFO, "BAPI_ACC_DOCUMENT_POST Debit CurrencyAmount set {0}.", currencyAmount);

        //Credit leg
        currencyAmount.appendRow();
        currencyAmount.setValue("ITEMNO_ACC", "002");
        currencyAmount.setValue("CURRENCY", "USD.");
        currencyAmount.setValue("AMT_DOCCUR", Math.abs(amount) * -1);

        logger.log(Level.INFO, "BAPI_ACC_DOCUMENT_POST Credit CurrencyAmount set {0}.", currencyAmount);

        try {
            JCoContext.begin(destination);
            try {
                function.execute(destination);
                bapiTransactionCommit.getImportParameterList().setValue("WAIT", "10");
                bapiTransactionCommit.execute(destination);
            } catch (AbapException ex) {
                logger.log(Level.SEVERE, "Error executing BAPI_ACC_DOCUMENT_POST.");
                bapiTransactionRollback.execute(destination);
            }
        } catch (JCoException ex) {

        } finally {
            JCoContext.end(destination);
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
