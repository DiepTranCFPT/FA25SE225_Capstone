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
    LEARNING_MATERIAL_RATING_NOT_FOUND(1051, "Learning material rating not found", HttpStatus.BAD_REQUEST),
    NOTE_ALREADY_EXISTS(1052, "You have already created a note for this lesson", HttpStatus.BAD_REQUEST),
    NOTE_NOT_FOUND(1053, "Note not found", HttpStatus.NOT_FOUND),
    LESSON_NOT_FOUND(1054, "Lesson not found", HttpStatus.BAD_REQUEST),
    ALREADY_REGISTERED(1055, "You have already registered for this learning material", HttpStatus.BAD_REQUEST),
    TEACHER_PROFILE_NOT_FOUND(1056,"Teacher profile not found" ,HttpStatus.BAD_REQUEST ),
    TEACHER_INVALID_AGE(1057,"Teacher must be at least 23 years old." ,HttpStatus.BAD_REQUEST ),
    TEACHER_RATING_NOT_FOUND(1058,"Teacher rating not found" ,HttpStatus.BAD_REQUEST ),
    TEACHER_RATING_ALREADY_EXISTS(1059,"You have already rated this teacher" ,HttpStatus.BAD_REQUEST ),
    INVALID_SCORE(1060,"The score is exceed or negative" , HttpStatus.BAD_REQUEST),
    PARENT_PROFILE_NOT_FOUND(1061,"Parent profile not found." , HttpStatus.BAD_REQUEST),
    STUDENT_PROFILE_NOT_FOUND(1062,"Student profile not found." , HttpStatus.BAD_REQUEST),
    ALREADY_LINKED_STUDENT(1063,"You have already linked this student" , HttpStatus.BAD_REQUEST),
    PAYMENT_AMOUNT_TOO_LOW(1064,"Payment amount is less than the price of the learning material.",HttpStatus.BAD_REQUEST ),
    INVALID_AMOUNT(1065, "Withdrawal amount must be greater than 0." ,HttpStatus.BAD_REQUEST ),
    INSUFFICIENT_BALANCE(1066,"Insufficient balance.",HttpStatus.BAD_REQUEST ),
    TRANSACTION_TYPE_NOT_FOUND(1067,"Transaction type not found" , HttpStatus.BAD_REQUEST ),
    TRANSACTION_NOT_FOUND(1068,"Transaction not found" , HttpStatus.BAD_REQUEST ),
    INVALID_STATUS(1069,"Status is not pending" ,HttpStatus.BAD_REQUEST ),
    PAYMENT_NOT_FOUND(1070,"PAYMENT NOT FOUND" , HttpStatus.BAD_REQUEST ),
    TRANSACTION_IS_VALID(1071, "You have a pending withdrawal order." , HttpStatus.BAD_REQUEST ),
    PAYMENT_METHOD_NOT_FOUND(1072, "You dont have payment method" , HttpStatus.BAD_REQUEST ),
    CANNOT_DELETE_QUESTION(1073,"Cannot delete question because it is referenced by other entities (e.g., exams)." , HttpStatus.BAD_REQUEST),
    SPEL(1074,"Please cast varargs... to array" ,HttpStatus.INTERNAL_SERVER_ERROR ),
    CONCURRENT_LOGIN_DETECTED(1075, "Invalid exam session, your account is currently taking this test on a different device.", HttpStatus.CONFLICT),
    COMMUNITY_NOT_FOUND(1076, "Community not found", HttpStatus.BAD_REQUEST),
    POST_NOT_FOUND(1077,"Post not found", HttpStatus.BAD_REQUEST),
    COMMENT_NOT_FOUND(1078, "Comment not found", HttpStatus.BAD_REQUEST),
    COMMENT_NOT_SAME_THE_POST(1079,"Comment not in the same post" , HttpStatus.BAD_REQUEST),
    INVALID_VOTE_VALUE(1080,"Vote value must be 1 (Like), -1(DisLike) or 0(Nothing)" , HttpStatus.BAD_REQUEST ),
    FLASHCARD_SET_NOT_FOUND(1081,"Flash card set not found" , HttpStatus.BAD_REQUEST ),
    INVALID_COMMENT(1082, "There are no content or image in comment", HttpStatus.BAD_REQUEST),
    EXCEED_REQUEST(1083, "You have used up all 3 requests for today." , HttpStatus.BAD_REQUEST),
    INVALID_SCORE_RANGE(1084,"Invalid score range" , HttpStatus.BAD_REQUEST),
    QUESTION_CONTEXT_NOT_FOUND(1085,"Question Context not found" ,HttpStatus.BAD_REQUEST );



    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
