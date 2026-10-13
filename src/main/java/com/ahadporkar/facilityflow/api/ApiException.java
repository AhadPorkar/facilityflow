package com.ahadporkar.facilityflow.api;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    public ApiException(HttpStatus status, String code, String message) {
        super(message); this.status = status; this.code = code;
    }
    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
    public static ApiException notFound(String entity) {
        return new ApiException(HttpStatus.NOT_FOUND,"NOT_FOUND",entity+" not found");
    }
    public static ApiException invalid(String message) {
        return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,"INVALID_WORKFLOW",message);
    }
    public static ApiException forbidden() {
        return new ApiException(HttpStatus.FORBIDDEN,"ACCESS_DENIED","Operation not permitted");
    }
}
