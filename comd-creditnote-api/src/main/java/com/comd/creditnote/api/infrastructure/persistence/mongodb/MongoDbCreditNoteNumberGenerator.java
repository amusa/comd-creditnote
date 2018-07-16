/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comd.creditnote.api.infrastructure.persistence.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import java.time.Year;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.bson.Document;
import com.comd.creditnote.api.infrastructure.jco.CreditNoteNumberGenerator;

/**
 *
 * @author maliska
 */
@ApplicationScoped
public class MongoDbCreditNoteNumberGenerator implements CreditNoteNumberGenerator {

    @Inject
    MongoDatabase mongoDb;

    @Override
    public String nextNumber() {
        Document doc = seriesCollection()
                .find()
                .first();

        Integer fiscalYear;
        Integer serialNumber;

        if (null == doc) {
            fiscalYear = getCurrentYear();
            serialNumber = 1;
        } else {
            fiscalYear = doc.getInteger("fiscalYear");
            serialNumber = doc.getInteger("serialNumber");
            Integer currentYear = getCurrentYear();

            if (fiscalYear == currentYear) {
                serialNumber++;
            } else if (fiscalYear < currentYear) {
                serialNumber = 1;
                fiscalYear++;
            } else {
                throw new RuntimeException("Unexpected exception!");
            }
        }

        //seriesCollection().updateOne(doc, Updates.set("likes", 150));
        seriesCollection().updateOne(
                new Document(),
                new Document("$set",
                        new Document("fiscalYear", fiscalYear)
                                .append("serialNumber", serialNumber)
                ),
                new UpdateOptions().upsert(true));

        return formatCreditNote(fiscalYear, serialNumber);
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
