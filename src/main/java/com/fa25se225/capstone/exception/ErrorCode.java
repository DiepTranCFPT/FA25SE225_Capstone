package com.fa25se225.capstone.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(1001, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR(1002, "Invalid input data", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1003, "You do not have permission", HttpStatus.FORBIDDEN),
    CANNOT_SEND_EMAIL(1004, "Cannot send email", HttpStatus.BAD_REQUEST),
    TEMPLATE_NOT_FOUND(1005, "Brevo template not found", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1006,"User not found", HttpStatus.BAD_REQUEST ),
    EXISTED_EMAIL(1007,"Email is existed" , HttpStatus.BAD_REQUEST),
    LOCKED_ACCOUNT(1008,"Your account is locked, try again in 24 hours" , HttpStatus.UNAUTHORIZED),
    UNAUTHENTICATED(1009, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNVERIFIED_EMAIL(1010,"Your email is unverify" , HttpStatus.UNAUTHORIZED),
    MORE_THAN_5_FAILED_PASSWORD(1011, "Your account is locked for 24 hours due to entering the wrong password more than 5 times", HttpStatus.BAD_REQUEST);

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
