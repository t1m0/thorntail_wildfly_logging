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
import org.jboss.logmanager.ExtFormatter;
import org.jboss.logmanager.ExtLogRecord;

public class CustomLogFormatter extends ExtFormatter {

    private static final String ERROR_MESSAGE = "{\"level\":\"SEVERE\"\"message\":\"ERROR WHILE LOGGING EVENT! %s %s}\n";

    @Override
    public String format(ExtLogRecord record) {
        int threadID = record.getThreadID();
        String timeStampString = Instant
                .ofEpochMilli(record.getMillis())
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            JsonGenerator jGenerator = new JsonFactory().createGenerator(out, JsonEncoding.UTF8);
            jGenerator.writeStartObject();
            writeField(jGenerator, "@timestamp", timeStampString);
            writeField(jGenerator, "message", record.getMessage());
            writeField(jGenerator, "level", record.getLevel().toString());
            writeField(jGenerator, "logger_name", record.getLoggerName());
            jGenerator.writeNumberField("thread_id", threadID);
            writeMDC(jGenerator, record.getMdcCopy());
            if (record.getThrown() != null) {
                writeException(jGenerator, record.getThrown());
            }
            jGenerator.writeEndObject();
            jGenerator.flush();
            return out.toString(StandardCharsets.UTF_8.name()) + "\n";
        } catch (Throwable e) {
            e.printStackTrace();
            return String.format(ERROR_MESSAGE, e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private void writeException(JsonGenerator jGenerator, Throwable throwable) throws IOException {
        jGenerator.writeObjectFieldStart("exception");
        writeField(jGenerator, "class", throwable.getClass().getCanonicalName());
        writeField(jGenerator, "message", throwable.getMessage());
        writeField(jGenerator, "stacktrace", extractStackTrace(throwable));
        jGenerator.writeEndObject();
    }

    private void writeMDC(JsonGenerator jGenerator, Map<String, String> mdcCopy) throws IOException {
        jGenerator.writeObjectFieldStart("mdc");
        for (Map.Entry<String, String> entry : mdcCopy.entrySet()) {
            writeField(jGenerator, entry.getKey(), entry.getValue());
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