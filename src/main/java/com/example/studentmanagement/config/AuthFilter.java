package com.example.studentmanagement.config;

import com.example.studentmanagement.controller.AuthController;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest  request  = (HttpServletRequest)  servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Allow internal forwards, error handling, etc.
        if (request.getDispatcherType() != DispatcherType.REQUEST) {
            filterChain.doFilter(request, response);
            return;
        }

        String uri = request.getRequestURI();
        HttpSession session = request.getSession(false);

        boolean isAdminAuth   = session != null && session.getAttribute(AuthController.AUTH_USER_KEY)    != null;
        boolean isStudentAuth = session != null && session.getAttribute(AuthController.STUDENT_AUTH_KEY) != null;

        // Route classification
        boolean isAdminLoginRoute   = uri.equals("/login")  || uri.equals("/login.html");
        boolean isStudentLoginRoute = uri.equals("/student-login") || uri.equals("/student-login.html");

        // Student-zone: login page + dashboard page + student API
        boolean isStudentZone = isStudentLoginRoute
                || uri.equals("/student-dashboard.html")
                || uri.startsWith("/api/student/");

        // Completely public resources
        boolean isPublicResource = isAdminLoginRoute || isStudentLoginRoute
                || uri.startsWith("/api/auth/")
                || uri.equals("/style.css")
                || uri.equals("/favicon.ico")
                || uri.startsWith("/error")
                || uri.startsWith("/h2-console");

        // Public resources
        if (isPublicResource) {
            if (isAdminLoginRoute && isAdminAuth) {
                response.sendRedirect("/");
                return;
            }
            if (isStudentLoginRoute && isStudentAuth) {
                response.sendRedirect("/student-dashboard.html");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        // Student-zone (non-login pages)
        if (isStudentZone) {
            if (isStudentAuth) {
                filterChain.doFilter(request, response);
                return;
            }
            if (uri.startsWith("/api/")) {
                sendUnauthorizedJson(response);
            } else {
                response.sendRedirect("/student-login.html");
            }
            return;
        }

        // Admin zone (everything else)
        if (isAdminAuth) {
            filterChain.doFilter(request, response);
            return;
        }
        if (uri.startsWith("/api/")) {
            sendUnauthorizedJson(response);
        } else {
            response.sendRedirect("/login");
        }
    }

    private void sendUnauthorizedJson(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
    }
}
