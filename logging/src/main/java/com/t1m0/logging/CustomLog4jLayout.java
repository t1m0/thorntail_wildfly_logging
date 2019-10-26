package com.t1m0.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.MDC;

public class CustomLog4jLayout extends Layout {

    private static final String ERROR_MESSAGE = "{\"level\":\"SEVERE\"\"message\":\"ERROR WHILE LOGGING EVENT! %s}";

    @Override
    public String format(LoggingEvent event) {
        String threadName = event.getThreadName();
        String timeStampString = Instant
                .ofEpochMilli(event.getTimeStamp())
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (JsonGenerator jGenerator = new JsonFactory().createGenerator(out, JsonEncoding.UTF8)) {
            jGenerator.writeStartObject();
            writeField(jGenerator, "@timestamp", timeStampString);
            writeField(jGenerator, "message", event.getRenderedMessage());
            writeField(jGenerator, "level", event.getLevel().toString());
            writeField(jGenerator, "logger_name", event.getLoggerName());
            writeField(jGenerator, "thread_name", threadName);
            writeMDC(jGenerator);
            if (event.getThrowableInformation() != null && event.getThrowableInformation().getThrowable() != null) {
                writeException(jGenerator, event.getThrowableInformation().getThrowable());
            }
            jGenerator.writeEndObject();
            jGenerator.flush();
            return out.toString(StandardCharsets.UTF_8.name()) + "\n";
        } catch (IOException e) {
            return String.format(ERROR_MESSAGE, e.getMessage());
        }
    }

    @Override
    public boolean ignoresThrowable() {
        return false;
    }

    @Override
    public void activateOptions() {

    }


    private void writeException(JsonGenerator jGenerator, Throwable throwable) throws IOException {
        jGenerator.writeObjectFieldStart("exception");
        writeField(jGenerator, "class", throwable.getClass().getCanonicalName());
        writeField(jGenerator, "message", throwable.getMessage());
        writeField(jGenerator, "stacktrace", extractStackTrace(throwable));
        jGenerator.writeEndObject();
    }

    private void writeMDC(JsonGenerator jGenerator) throws IOException {
        jGenerator.writeObjectFieldStart("mdc");
        if (MDC.getCopyOfContextMap() != null) {
            for (Map.Entry<String, String> entry : MDC.getCopyOfContextMap().entrySet()) {
                writeField(jGenerator, entry.getKey(), entry.getValue());
            }
        }
        jGenerator.writeEndObject();
    }

    private void writeField(JsonGenerator jGenerator, String fieldName, String value) throws IOException {
        if (value != null && value.trim().length() > 0) {
            jGenerator.writeStringField(fieldName, value);
        }
    }

    private String extractStackTrace(Throwable throwable) {
        StringWriter errors = new StringWriter();
        throwable.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }
}