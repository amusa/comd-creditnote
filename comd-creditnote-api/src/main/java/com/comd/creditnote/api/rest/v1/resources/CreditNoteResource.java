package com.comd.creditnote.api.rest.v1.resources;

import com.comd.creditnote.lib.v1.request.PostCreditNoteRequest;
import com.sap.conn.jco.JCoException;
import com.comd.creditnote.api.services.CreditNoteService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/creditnote")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CreditNoteResource {

    private static final Logger logger = Logger.getLogger(CreditNoteResource.class.getName());

    @Inject
    private CreditNoteService creditNoteService;

//    @Path("/post") 
    @POST
    public Response post(PostCreditNoteRequest creditNoteRequest) throws JCoException, ParseException {

        logger.log(Level.INFO,
                "CreditNote Request received.  blDate={0}, vesselId={1}, customerId={2}, invoice={3}, amount={0}",
                new Object[]{creditNoteRequest.getBlDate(),
                    creditNoteRequest.getVesselId(),
                    creditNoteRequest.getCustomerId(),
                    creditNoteRequest.getInvoice(),
                    creditNoteRequest.getAmount()});
        
        return Response.ok(
                creditNoteService.post(
                        creditNoteRequest.getBlDate(),
                        creditNoteRequest.getVesselId(),
                        creditNoteRequest.getCustomerId(),
                        creditNoteRequest.getInvoice(),
                        creditNoteRequest.getAmount())
        )
                .header("X-Total-Count", 0)
                .build();

    }

}
