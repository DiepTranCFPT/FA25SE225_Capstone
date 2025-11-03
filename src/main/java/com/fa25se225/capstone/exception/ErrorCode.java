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
    MORE_THAN_5_FAILED_PASSWORD(1011, "Your account is locked for 24 hours due to entering the wrong password more than 5 times", HttpStatus.BAD_REQUEST),
    ACCESS_TOKEN_EXPIRED(1012, "Access token expired", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED(1013, "Refresh token expired", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(1014, "Invalid token", HttpStatus.UNAUTHORIZED),
    INVALID_OTP(1015,"Invalid OTP" , HttpStatus.BAD_REQUEST ),
    EXPIRED_OTP(1016,"OTP has expired" , HttpStatus.BAD_REQUEST ),
    FAIL_TO_RESET_PASSWORD(1017,"Fail to reset password" , HttpStatus.BAD_REQUEST ),
    INVALID_VERIFICATION_TOKEN(1018,"Invalid verification token" ,HttpStatus.BAD_REQUEST ),
    INVALID_PASSWORD(1019,"Current password is incorrect" ,HttpStatus.BAD_REQUEST ),
    INVALID_NEW_PASSWORD(1020, "New password must be different from current password" , HttpStatus.BAD_REQUEST ),
    INVALID_ROLE_NAME(1021, "Invalid role name", HttpStatus.BAD_REQUEST ),
    EXISTED_ROLE(1022,"Role is existed" , HttpStatus.BAD_REQUEST ),
    PERMISSION_NOT_FOUND(1023,"Permission not found" ,HttpStatus.BAD_REQUEST ),
    INVALID_IO(1024,"There are something wrong with file IO " , HttpStatus.INTERNAL_SERVER_ERROR),
    LEARNING_MATERIAL_NOT_FOUND(1025, "Learning material not found", HttpStatus.BAD_REQUEST),
    MATERIAL_TYPE_NOT_FOUND(1026, "Material type not found", HttpStatus.BAD_REQUEST),
    SUBJECT_NOT_FOUND(1027, "Subject not found", HttpStatus.BAD_REQUEST),
    EXAM_NOT_FOUND(1028, "Exam not found", HttpStatus.BAD_REQUEST),
    EXAM_QUESTION_NOT_FOUND(1029, "Exam question not found", HttpStatus.BAD_REQUEST),
    QUESTION_NOT_FOUND(1030, "Question not found", HttpStatus.BAD_REQUEST),
    QUESTION_DIFFICULTY_NOT_FOUND(1031, "Question difficulty not found", HttpStatus.BAD_REQUEST),

    EXAM_TEMPLATE_NOT_FOUND(1032,"Exam template not found" , HttpStatus.BAD_REQUEST ),
    EXAM_ATTEMPT_NOT_FOUND(1033,"Exam attempt not found" , HttpStatus.BAD_REQUEST ),
    INVALID_EXAM_ATTEMPT_STATE(1034,"The exam is summited or time out" , HttpStatus.BAD_REQUEST );

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
