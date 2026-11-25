package com.lukmn.fundamental.adapter.in;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filter-level security errors (missing/invalid token, forbidden) are routed to GlobalExceptionHandler,
 * so they share the same ProblemDetail format as every other API error.
 */
@Component 
public class SecurityProblemHandler implements AuthenticationEntryPoint, AccessDeniedHandler{
    
    private final AuthenticationEntryPoint bearerEntryPoint = new BearerTokenAuthenticationEntryPoint();
    private final AccessDeniedHandler bearerAccessDeniedHandler = new BearerTokenAccessDeniedHandler();
    private final HandlerExceptionResolver resolver;
    
    public SecurityProblemHandler(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        bearerAccessDeniedHandler.handle(request, response, accessDeniedException);
        resolver.resolveException(request, response, null, accessDeniedException);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        bearerEntryPoint.commence(request, response, authException);
        resolver.resolveException(request, response, null, authException);
    }

}
