package com.t1m0.thorntaillogging;

import java.util.Date;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.t1m0.logging.MDCWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/time")
@Produces(MediaType.APPLICATION_JSON)
public class TimeEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeEndpoint.class);

    @GET
    @Path("/now")
    @Produces(MediaType.APPLICATION_JSON)
    public String get() {
        MDCWrapper.put("traceId", UUID.randomUUID().toString());
        LOGGER.info("Test log");
        return String.format("{\"value\" : \"The time is %s\"}", new Date());
    }
}
