package com.company.security;

import com.company.model.ServiceResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${API_KEYS}")
    private String apiKeys; // Load the keys as a comma-separated string

    @Value("${API_KEYS_ADMIN}")
    private String adminApiKeys; // Comma-separated admin API keys

    private List<String> validApiKeys;
    private List<String> validAdminApiKeys;

    @Override
    protected void initFilterBean() throws ServletException {
        // Split the keys into a list once at initialization
        validApiKeys = Arrays.asList(apiKeys.split(","));
        validAdminApiKeys = Arrays.asList(adminApiKeys.split(","));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestApiKey = request.getHeader("x-api-key");
        String path = request.getRequestURI();

        // Allow admin API keys for ALL endpoints
        if (validAdminApiKeys.contains(requestApiKey)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Restrict certain paths to admin keys only
        if (isAdminOnlyEndpoint(path)) {
            ServiceResult serviceResult = ServiceResult.builder()
                    .returnCode("401")
                    .returnMessage("Unauthorized: Admin API Key required")
                    .build();
            response.setContentType("application/json");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(new ObjectMapper().writeValueAsString(serviceResult));
            return;
        }

        // Allow regular API keys for all other endpoints
        if (validApiKeys.contains(requestApiKey)) {
            filterChain.doFilter(request, response);
            return;
        }

        // If no valid key is provided, deny access
        ServiceResult serviceResult = ServiceResult.builder()
                .returnCode("401")
                .returnMessage("Unauthorized: Invalid API Key")
                .build();
        response.setContentType("application/json");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(new ObjectMapper().writeValueAsString(serviceResult));
    }

    private boolean isAdminOnlyEndpoint(String path) {
        return path.equals("/api/firebase/getAllData");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Exclude Swagger endpoints from API key filtering
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/webjars");
    }
}
