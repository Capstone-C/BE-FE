package com.capstone.web.common.exception;

import com.capstone.web.auth.exception.AuthErrorCode;
import com.capstone.web.auth.exception.InvalidCredentialsException;
import com.capstone.web.auth.exception.WithdrawnMemberException;
import com.capstone.web.comment.exception.CommentNotFoundException;
import com.capstone.web.comment.exception.CommentPermissionException;
import com.capstone.web.common.response.ErrorResponse;
import com.capstone.web.diary.exception.DiaryErrorCode;
import com.capstone.web.diary.exception.DiaryNotFoundException;
import com.capstone.web.diary.exception.DuplicateDiaryEntryException;
import com.capstone.web.diary.exception.UnauthorizedDiaryAccessException;
import com.capstone.web.member.exception.DuplicateEmailException;
import com.capstone.web.member.exception.DuplicateNicknameException;
import com.capstone.web.member.exception.InvalidNicknameException;
import com.capstone.web.member.exception.InvalidProfileImageSizeException;
import com.capstone.web.member.exception.InvalidProfileImageTypeException;
import com.capstone.web.member.exception.MemberErrorCode;
import com.capstone.web.member.exception.InvalidOldPasswordException;
import com.capstone.web.member.exception.SameAsOldPasswordException;
import com.capstone.web.member.exception.PasswordChangeErrorCode;
import com.capstone.web.member.exception.RecentPasswordReuseException;
import com.capstone.web.member.exception.PasswordResetException;
import com.capstone.web.member.exception.PasswordResetErrorCode;
import com.capstone.web.member.exception.MemberBlockException;
import com.capstone.web.member.exception.MemberBlockErrorCode;
import com.capstone.web.member.exception.InvalidWithdrawPasswordException;
import com.capstone.web.member.exception.MemberWithdrawErrorCode;
import java.util.List;
import java.util.stream.Collectors;

import com.capstone.web.posts.exception.PostNotFoundException;
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
    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePostNotFoundException(PostNotFoundException ex) {
        // new ErrorResponse(...) 대신 ErrorResponse.of(...) 사용
        ErrorResponse response = ErrorResponse.of(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler({InvalidOldPasswordException.class, SameAsOldPasswordException.class, RecentPasswordReuseException.class})
    public ResponseEntity<ErrorResponse> handlePasswordChange(RuntimeException ex) {
        log.info("비밀번호 예외 핸들러 진입: {}", ex.getClass().getSimpleName());
        PasswordChangeErrorCode errorCode = null;
        if (ex instanceof InvalidOldPasswordException) {
            errorCode = ((InvalidOldPasswordException) ex).getErrorCode();
        } else if (ex instanceof SameAsOldPasswordException) {
            errorCode = ((SameAsOldPasswordException) ex).getErrorCode();
        } else if (ex instanceof RecentPasswordReuseException) {
            errorCode = ((RecentPasswordReuseException) ex).getErrorCode();
        }
        if (errorCode != null) {
            ErrorResponse.FieldError fieldError = new ErrorResponse.FieldError(errorCode.field(), errorCode.message());
            ErrorResponse response = ErrorResponse.of(errorCode.status(), errorCode.code(), errorCode.message(), List.of(fieldError));
            return ResponseEntity.status(errorCode.status()).body(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(HttpStatus.BAD_REQUEST, "MEMBER_PASSWORD_CHANGE_ERROR", ex.getMessage()));
    }

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

    @ExceptionHandler(InvalidNicknameException.class)
    public ResponseEntity<ErrorResponse> handleInvalidNickname(InvalidNicknameException ex) {
        return buildMemberErrorResponse(ex.getErrorCode());
    }

    @ExceptionHandler({InvalidProfileImageTypeException.class, InvalidProfileImageSizeException.class})
    public ResponseEntity<ErrorResponse> handleInvalidProfileImage(RuntimeException ex) {
        if (ex instanceof InvalidProfileImageTypeException e) {
            return buildMemberErrorResponse(e.getErrorCode());
        } else if (ex instanceof InvalidProfileImageSizeException e) {
            return buildMemberErrorResponse(e.getErrorCode());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, "MEMBER_PROFILE_INVALID", ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildAuthErrorResponse(ex.getErrorCode());
    }

    @ExceptionHandler(WithdrawnMemberException.class)
    public ResponseEntity<ErrorResponse> handleWithdrawnMember(WithdrawnMemberException ex) {
        return buildAuthErrorResponse(ex.getErrorCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        ErrorResponse response = ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(PasswordResetException.class)
    public ResponseEntity<ErrorResponse> handlePasswordReset(PasswordResetException ex) {
        PasswordResetErrorCode code = ex.getErrorCode();
        ErrorResponse.FieldError fieldError = new ErrorResponse.FieldError(code.field(), code.message());
        ErrorResponse response = ErrorResponse.of(code.status(), code.code(), code.message(), List.of(fieldError));
        return ResponseEntity.status(code.status()).body(response);
    }

    @ExceptionHandler(MemberBlockException.class)
    public ResponseEntity<ErrorResponse> handleMemberBlock(MemberBlockException ex) {
        MemberBlockErrorCode code = ex.getErrorCode();
        // 매핑 규칙: SELF_BLOCK/DUPLICATE_BLOCK/NOT_BLOCKED -> 400, MEMBER_NOT_FOUND -> 404
        HttpStatus status;
        switch (code) {
            case MEMBER_NOT_FOUND -> status = HttpStatus.NOT_FOUND;
            default -> status = HttpStatus.BAD_REQUEST;
        }
        ErrorResponse.FieldError fieldError = new ErrorResponse.FieldError("memberBlock", code.name());
        ErrorResponse response = ErrorResponse.of(status, "MEMBER_BLOCK_" + code.name(), code.name(), List.of(fieldError));
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(DiaryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDiaryNotFound(DiaryNotFoundException ex) {
        return buildDiaryErrorResponse(DiaryErrorCode.DIARY_NOT_FOUND);
    }

    @ExceptionHandler(DuplicateDiaryEntryException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateDiaryEntry(DuplicateDiaryEntryException ex) {
        return buildDiaryErrorResponse(DiaryErrorCode.DUPLICATE_DIARY_ENTRY);
    }

    @ExceptionHandler(UnauthorizedDiaryAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedDiaryAccess(UnauthorizedDiaryAccessException ex) {
        return buildDiaryErrorResponse(DiaryErrorCode.UNAUTHORIZED_DIARY_ACCESS);
    }

    @ExceptionHandler(InvalidWithdrawPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidWithdrawPassword(InvalidWithdrawPasswordException ex) {
        MemberWithdrawErrorCode errorCode = ex.getErrorCode();
        ErrorResponse.FieldError fieldError = new ErrorResponse.FieldError(errorCode.field(), errorCode.message());
        ErrorResponse response = ErrorResponse.of(errorCode.status(), errorCode.code(), errorCode.message(), List.of(fieldError));
        return ResponseEntity.status(errorCode.status()).body(response);
    }

    private ErrorResponse.FieldError toFieldError(FieldError error) {
        return new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage());
    }

    private ResponseEntity<ErrorResponse> buildMemberErrorResponse(MemberErrorCode errorCode) {
        ErrorResponse.FieldError fieldError = new ErrorResponse.FieldError(errorCode.field(), errorCode.message());
        ErrorResponse response = ErrorResponse.of(errorCode.status(), errorCode.code(), errorCode.message(), List.of(fieldError));
        return ResponseEntity.status(errorCode.status()).body(response);
    }

    private ResponseEntity<ErrorResponse> buildAuthErrorResponse(AuthErrorCode errorCode) {
        ErrorResponse.FieldError fieldError = new ErrorResponse.FieldError(errorCode.field(), errorCode.message());
        ErrorResponse response = ErrorResponse.of(errorCode.status(), errorCode.code(), errorCode.message(), List.of(fieldError));
        return ResponseEntity.status(errorCode.status()).body(response);
    }
    // 👇 [추가] CommentNotFoundException 핸들러
    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCommentNotFoundException(CommentNotFoundException ex) {
        log.warn("Comment Not Found: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // 👇 [추가] CommentPermissionException 핸들러
    @ExceptionHandler(CommentPermissionException.class)
    public ResponseEntity<ErrorResponse> handleCommentPermissionException(CommentPermissionException ex) {
        log.warn("Comment Permission Denied: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(HttpStatus.FORBIDDEN, "COMMENT_PERMISSION_DENIED", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);

    private ResponseEntity<ErrorResponse> buildDiaryErrorResponse(DiaryErrorCode errorCode) {
        ErrorResponse.FieldError fieldError = new ErrorResponse.FieldError(errorCode.getFieldName(), errorCode.getMessage());
        ErrorResponse response = ErrorResponse.of(errorCode.getHttpStatus(), errorCode.getCode(), errorCode.getMessage(), List.of(fieldError));
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }
}
