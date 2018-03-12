package com.orwellg.yggdrasil.bacs.log.hive.topology.models;

public class Log {
    private String instance;
    private String logLevel;
    private String component;
    private long timestamp;
    private String message;
    private String errorMessage;
    private String trace;

    public Log(String instance, String logLevel, String component, long timestamp, String message, String errorMessage, String trace) {
        this.instance = instance;
        this.logLevel = logLevel;
        this.component = component;
        this.timestamp = timestamp;
        this.message = message;
        this.errorMessage = errorMessage;
        this.trace = trace;
    }

    public Log(String instance, String logLevel, String component, long timestamp, String message) {
        this.instance = instance;
        this.logLevel = logLevel;
        this.component = component;
        this.timestamp = timestamp;
        this.message = message;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getTrace() {
        return trace;
    }

    public void setTrace(String trace) {
        this.trace = trace;
    }

    @Override
    public String toString() {
        return "Log{" +
                "instance='" + instance + '\'' +
                ", logLevel='" + logLevel + '\'' +
                ", component='" + component + '\'' +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", trace='" + trace + '\'' +
                '}';
    }
}
