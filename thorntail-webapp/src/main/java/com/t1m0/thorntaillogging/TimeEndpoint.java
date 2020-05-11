package com.t1m0.thorntaillogging;

import java.util.Date;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Path("/time")
@Produces(MediaType.APPLICATION_JSON)
public class TimeEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeEndpoint.class);

    @GET
    @Path("/now")
    @Produces(MediaType.APPLICATION_JSON)
    public String get() {
        MDC.put("traceId", UUID.randomUUID().toString());
        try {
            LOGGER.info("Test log");
            return String.format("{\"value\" : \"The time is %s\"}", new Date());
        } finally {
            MDC.remove("traceId");
        }
    }
}
