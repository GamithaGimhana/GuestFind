package com.gdse.aad.backend.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdse.aad.backend.dto.ApiResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        ApiResponseDTO errorResponse = new ApiResponseDTO(401, "Unauthorized - Invalid or missing token", null);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
    }
}
