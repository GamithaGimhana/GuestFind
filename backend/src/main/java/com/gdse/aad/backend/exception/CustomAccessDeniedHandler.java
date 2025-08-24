package com.gdse.aad.backend.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdse.aad.backend.dto.ApiResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ApiResponseDTO errorResponse = new ApiResponseDTO(403, "Forbidden - You don’t have permission", null);

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
    }
}
