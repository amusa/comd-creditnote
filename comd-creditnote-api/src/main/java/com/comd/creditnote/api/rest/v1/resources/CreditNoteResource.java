package com.comd.creditnote.api.rest.v1.resources;

import com.comd.creditnote.api.infrastructure.jco.TestCreditNoteService;
import com.sap.conn.jco.JCoException;
import com.comd.creditnote.api.services.CreditNoteService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import org.eclipse.microprofile.faulttolerance.Fallback;

@Path("/creditnote")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CreditNoteResource {

    @Inject
    private CreditNoteService creditNoteService;

    @Inject
    TestCreditNoteService service;

    @Fallback(fallbackMethod = "fallbackDelivery")
    @GET
    public Response delivery(@QueryParam("bldate") String blDate,
            @QueryParam("vessel") String vesselId, @QueryParam("customer") String customerId) throws JCoException, ParseException {

//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        Date dateBl;
//
//        dateBl = sdf.parse(blDate);
//        return Response.ok(creditNoteService.creditNotesOfDelivery(blDate, customerId))
//                .header("X-Total-Count", 0).build();
        return Response.ok(service.creditNotesOfDelivery(blDate, customerId))
                .header("X-Total-Count", 0).build();

    }

    public Response fallbackDelivery(@QueryParam("bldate") String blDate,
            @QueryParam("vessel") String vesselId, @QueryParam("customer") String customerId) {
        return Response.ok()
                .header("X-Total-Count", 0).build();
    }
}
