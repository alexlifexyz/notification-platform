package com.enterprise.notification.common.exception;

/**
 * 通知客户端异常
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
public class NotificationClientException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    public NotificationClientException(String message) {
        super(message);
        this.errorCode = "UNKNOWN_ERROR";
        this.httpStatus = 500;
    }

    public NotificationClientException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "UNKNOWN_ERROR";
        this.httpStatus = 500;
    }

    public NotificationClientException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public NotificationClientException(String errorCode, String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String toString() {
        return String.format("NotificationClientException{errorCode='%s', httpStatus=%d, message='%s'}", 
                errorCode, httpStatus, getMessage());
    }
}
