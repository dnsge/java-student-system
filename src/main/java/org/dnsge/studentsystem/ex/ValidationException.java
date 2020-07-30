package org.dnsge.studentsystem.ex;

public class ValidationException extends BadRequestException {

    public ValidationException(String message) {
        super(message);
    }

}
