package com.capstone.web.common.exception;

import com.capstone.web.common.response.ErrorResponse;
import com.capstone.web.member.exception.DuplicateEmailException;
import com.capstone.web.member.exception.DuplicateNicknameException;
import com.capstone.web.member.exception.MemberErrorCode;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        List<ErrorResponse.FieldError> errors = bindingResult.getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .collect(Collectors.toList());
        ErrorResponse response = ErrorResponse.of(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "입력값을 다시 확인해주세요.", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException ex) {
        return buildMemberErrorResponse(ex.getErrorCode());
    }

    @ExceptionHandler(DuplicateNicknameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateNickname(DuplicateNicknameException ex) {
        return buildMemberErrorResponse(ex.getErrorCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        ErrorResponse response = ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ErrorResponse.FieldError toFieldError(FieldError error) {
        return new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage());
    }

    private ResponseEntity<ErrorResponse> buildMemberErrorResponse(MemberErrorCode errorCode) {
        ErrorResponse.FieldError fieldError = new ErrorResponse.FieldError(errorCode.field(), errorCode.message());
        ErrorResponse response = ErrorResponse.of(errorCode.status(), errorCode.code(), errorCode.message(), List.of(fieldError));
        return ResponseEntity.status(errorCode.status()).body(response);
    }
}
