package com.gdse.aad.backend.exception;

import com.gdse.aad.backend.dto.ApiResponseDTO;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponseDTO handleUserNameNotFoundException(UsernameNotFoundException ex) {
        return new ApiResponseDTO(404, "User Not Found", null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseDTO handleBadCredentials(BadCredentialsException ex) {
        return new ApiResponseDTO(400, "Bad Credentials", null);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponseDTO handleJWTTokenExpiredException(ExpiredJwtException ex) {
        return new ApiResponseDTO(401, "JWT Token Expired", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponseDTO handleAccessDenied(AccessDeniedException ex) {
        return new ApiResponseDTO(403, "Forbidden - You don’t have permission", null);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponseDTO handleAllExceptions(RuntimeException ex) {
        return new ApiResponseDTO(500, "Internal Server Error", null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseDTO handleIllegalArgument(IllegalArgumentException ex) {
        return new ApiResponseDTO(400, ex.getMessage(), null);
    }
}
