package com.examplatform.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public final class ApiErrors {

    public static final String GENERIC = "কিছু সমস্যা হয়েছে, আবার চেষ্টা করুন।";

    private ApiErrors() {}

    // ইচ্ছাকৃতভাবে ছোঁড়া business error কিনা (message user কে দেখানো safe)
    public static boolean isBusiness(Throwable ex) {
        if (ex == null || ex.getCause() != null) return false;
        Class<?> c = ex.getClass();
        return c == RuntimeException.class
                || c == IllegalStateException.class
                || c == IllegalArgumentException.class
                || ex instanceof ValidationException
                || ex instanceof ResourceNotFoundException
                || ex instanceof DuplicateResourceException
                || ex instanceof ResponseStatusException;
    }

    public static String safeMessage(Throwable ex) {
        if (!isBusiness(ex)) return GENERIC;
        if (ex instanceof ResponseStatusException rse) {
            String r = rse.getReason();
            return (r == null || r.isBlank()) ? GENERIC : r;
        }
        if (ex instanceof ValidationException ve) {
            return String.join(", ", ve.getErrors());
        }
        String m = ex.getMessage();
        return (m == null || m.isBlank()) ? GENERIC : m;
    }

    public static HttpStatus status(Throwable ex) {
        if (!isBusiness(ex)) return HttpStatus.INTERNAL_SERVER_ERROR;
        if (ex instanceof ResponseStatusException rse) {
            HttpStatus s = HttpStatus.resolve(rse.getStatusCode().value());
            return s != null ? s : HttpStatus.BAD_REQUEST;
        }
        if (ex instanceof ResourceNotFoundException) return HttpStatus.NOT_FOUND;
        if (ex instanceof DuplicateResourceException) return HttpStatus.CONFLICT;
        return HttpStatus.BAD_REQUEST;
    }

    // Controller এর catch ব্লকে: return ApiErrors.respond(ex);
    public static ResponseEntity<Map<String, Object>> respond(Exception ex) {
        if (!isBusiness(ex)) {
            log.error("Unexpected error", ex);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", safeMessage(ex));
        return ResponseEntity.status(status(ex)).body(body);
    }
}
