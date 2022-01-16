package hu.numichi.reactive.logger;

import org.slf4j.MDC;
import reactor.util.annotation.Nullable;

import java.util.Map;

public class MDCSnapshot implements AutoCloseable {
    private MDCSnapshot(@Nullable final Map<String, String> context) {
        if (context == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(context);
        }
    }
    
    public static MDCSnapshot of(@Nullable final Map<String, String> context) {
        return new MDCSnapshot(context);
    }
    
    public static MDCSnapshot empty() {
        return new MDCSnapshot(null);
    }
    
    @Override
    public void close() {
        MDC.clear();
    }
}
