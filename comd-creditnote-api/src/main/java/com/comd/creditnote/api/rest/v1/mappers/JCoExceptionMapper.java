package com.comd.creditnote.api.rest.v1.mappers;

import com.comd.creditnote.lib.v1.common.ApiError;
import com.sap.conn.jco.JCoException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.UUID;

public class JCoExceptionMapper implements ExceptionMapper<JCoException> {

    @Override
    public Response toResponse(JCoException exception) {

        ApiError error = new ApiError();
        error.setRef(UUID.randomUUID());
        error.setStatus(500);
        error.setCode("sap.server.error");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
    }
}
