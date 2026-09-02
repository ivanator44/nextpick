package com.nextpick.backend.exception;

public class UpstreamServiceException extends RuntimeException {
    private final String service;
    private final int upstreamStatus;

    public UpstreamServiceException(String service, int upstreamStatus, String message) {
        super(message);
        this.service = service;
        this.upstreamStatus = upstreamStatus;
    }

    public String getService() {
        return service;
    }

    public int getUpstreamStatus() {
        return upstreamStatus;
    }
}
