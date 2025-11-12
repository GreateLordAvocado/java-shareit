package ru.practicum.shareit.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {
    @JsonProperty("timestamp")
    private final String timestamp;

    @JsonProperty("status")
    private final int status;

    @JsonProperty("error")
    private final String error;

    @JsonProperty("path")
    private final String path;

    public ErrorResponse(String timestamp, int status, String error, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.path = path;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getPath() {
        return path;
    }
}
