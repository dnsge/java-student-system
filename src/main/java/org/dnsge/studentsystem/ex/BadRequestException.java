package org.dnsge.studentsystem.ex;

public class BadRequestException extends RuntimeException {
    public BadRequestException() {
        super();
    }

    public BadRequestException(String message) {
        super(message);
    }
}
