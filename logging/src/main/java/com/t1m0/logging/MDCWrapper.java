package com.t1m0.logging;

import org.jboss.logmanager.MDC;

public class MDCWrapper {

    public static void put(String key, String value) {
        MDC.put(key, value);
    }
}
