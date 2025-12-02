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
    INVALID_EXAM_ATTEMPT_STATE(1034,"The exam is summited or time out" , HttpStatus.BAD_REQUEST ),

    QUESTION_V2_NOT_FOUND(1035, "Question not found", HttpStatus.BAD_REQUEST),
    QUESTION_V2_ALREADY_EXISTS(1036, "Question already exists", HttpStatus.BAD_REQUEST),
    ANSWER_V2_NOT_FOUND(1037, "Answer not found", HttpStatus.BAD_REQUEST),
    QUESTION_TOPIC_V2_NOT_FOUND(1038, "Question topic not found", HttpStatus.BAD_REQUEST),
    QUESTION_DIFFICULTY_V2_NOT_FOUND(1039, "Question difficulty not found", HttpStatus.BAD_REQUEST),
    INVALID_QUESTION_V2_TYPE(1040, "Invalid question type", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_ANSWERS_V2(1041, "Question must have at least one answer", HttpStatus.BAD_REQUEST),
    NO_CORRECT_ANSWER_V2(1042, "Question must have at least one correct answer", HttpStatus.BAD_REQUEST),
    MULTIPLE_CORRECT_ANSWERS_V2(1043, "Single choice question can only have one correct answer", HttpStatus.BAD_REQUEST),
    EXAM_RULE_NOT_FOUND(1044,"Exam rule not found" , HttpStatus.BAD_REQUEST ),

    EXISTED_QUESTION_TOPIC(1045, "Question topic name is existed", HttpStatus.BAD_REQUEST ),

    QUESTION_DIFFICULTY_EXISTED(1046, "Question difficulty name is existed", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_QUESTIONS_IN_BANK(1047, "Insufficient questions in bank for the selected rule", HttpStatus.BAD_REQUEST),
    ALREADY_RATE(1048, "You have already rated this test", HttpStatus.BAD_REQUEST ),
    ALREADY_EXISTS(1049, "Rating already exists", HttpStatus.BAD_REQUEST),
    LEARNING_MATERIAL_RATING_ALREADY_EXISTS(1050, "You have already rated this learning material", HttpStatus.BAD_REQUEST),
    LESSON_NOT_FOUND(1032, "Lesson not found", HttpStatus.BAD_REQUEST),
    ALREADY_REGISTERED(1033, "You have already registered for this learning material", HttpStatus.BAD_REQUEST),
    NOT_FOUND(1034,"Teacher profile not found" ,HttpStatus.BAD_REQUEST ),
    INVALID_INPUT(1035,"Teacher must be at least 23 years old." ,HttpStatus.BAD_REQUEST ),

    INVALID_SCORE(1036,"The score is exceed or negative" , HttpStatus.BAD_REQUEST),

    PARENT_PROFILE_NOT_FOUND(1037,"Parent profile not found." , HttpStatus.BAD_REQUEST),
    STUDENT_PROFILE_NOT_FOUND(1038,"Student profile not found." , HttpStatus.BAD_REQUEST),

    ALREADY_LINKED_STUDENT(1039,"You have already linked this student" , HttpStatus.BAD_REQUEST),
    PAYMENT_AMOUNT_TOO_LOW(1040,"Payment amount is less than the price of the learning material.",HttpStatus.BAD_REQUEST ),
    INVALID_AMOUNT(1041, "Withdrawal amount must be greater than 0." ,HttpStatus.BAD_REQUEST ),
    INSUFFICIENT_BALANCE(1042,"Insufficient balance.",HttpStatus.BAD_REQUEST ), TRANSACTION_TYPE_NOT_FOUND(1043,"Transaction type not found" , HttpStatus.BAD_REQUEST ),
    TRANSACTION_NOT_FOUND(1044,"Transaction not found" , HttpStatus.BAD_REQUEST ),
    INVALID_STATUS(1045,"Status is not pending" ,HttpStatus.BAD_REQUEST ),
    PAYMENT_NOT_FOUND(1046,"PAYMENT NOT FOUND" , HttpStatus.BAD_REQUEST ),
    TRANSACTION_IS_VALID(1047, "You have a pending withdrawal order." , HttpStatus.BAD_REQUEST ),;


    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
