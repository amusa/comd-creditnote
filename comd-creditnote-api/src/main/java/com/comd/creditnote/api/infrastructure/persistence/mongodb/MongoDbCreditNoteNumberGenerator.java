/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comd.creditnote.api.infrastructure.persistence.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import java.time.Year;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.bson.Document;
import com.comd.creditnote.api.infrastructure.jco.CreditNoteNumberGenerator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maliska
 */
@ApplicationScoped
public class MongoDbCreditNoteNumberGenerator implements CreditNoteNumberGenerator {

    private static final Logger logger = Logger.getLogger(MongoDbCreditNoteNumberGenerator.class.getName());

    @Inject
    MongoDatabase mongoDb;

    @Override
    public String nextNumber() {
        Document doc = seriesCollection()
                .find()
                .first();

        logger.log(Level.INFO, "Find serial counter finished: {0}", doc);

        Integer fiscalYear;
        Integer serialNumber;

        if (null == doc) {
            fiscalYear = getCurrentYear();
            serialNumber = 1;
        } else {
            fiscalYear = doc.getInteger("fiscalYear");
            serialNumber = doc.getInteger("serialNumber");
            Integer currentYear = getCurrentYear();

            logger.log(Level.INFO, "FiscalYear={0}, CurrentYear={1}", new Object[]{fiscalYear, currentYear});

            if (fiscalYear.equals(currentYear)) {
                serialNumber++;
            } else if (fiscalYear < currentYear) {
                serialNumber = 1;
                fiscalYear++;
            } else {
                throw new RuntimeException("Unexpected exception!");
            }
        }

        logger.log(Level.INFO, "Updating serial counter: fiscalYear={0}, serialNumber={1}", new Object[]{fiscalYear, serialNumber});

        seriesCollection().updateOne(
                new Document(),
                new Document("$set",
                        new Document("fiscalYear", fiscalYear)
                                .append("serialNumber", serialNumber)
                ),
                new UpdateOptions().upsert(true));

        String cNote = formatCreditNote(fiscalYear, serialNumber);

        logger.log(Level.INFO, "Returning Credit Note number: {0}", cNote);

        return cNote;
    }

    private MongoCollection<Document> seriesCollection() {
        return mongoDb.getCollection("creditnote");
    }

    private Integer getCurrentYear() {
        return Year.now().getValue();
    }

    private String formatCreditNote(Integer fiscalYear, Integer serialNumber) {
        return String.format("S-%03d/%d", new Object[]{serialNumber, fiscalYear});
    }
}
