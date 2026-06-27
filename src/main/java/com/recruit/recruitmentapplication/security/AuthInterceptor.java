package com.recruit.recruitmentapplication.security;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.util.SessionConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        SessionUser loggedInUser = session == null
                ? null
                : (SessionUser) session.getAttribute(SessionConstants.LOGGED_IN_USER);

        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return false;
        }

        if (request.getRequestURI().startsWith(request.getContextPath() + "/admin")
                && !Role.ADMIN.equals(loggedInUser.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/error/403");
            return false;
        }

        if (isCompanyWriteRequest(request) && !canManageCompanies(loggedInUser)) {
            response.sendRedirect(request.getContextPath() + "/error/403");
            return false;
        }
        if (isJobWriteRequest(request) && !canManageCompanies(loggedInUser)) {
            response.sendRedirect(request.getContextPath() + "/error/403");
            return false;
        }
        if (isCandidateAccessDenied(request, loggedInUser)) {
            response.sendRedirect(request.getContextPath() + "/error/403");
            return false;
        }
        return true;
    }

    private boolean isCompanyWriteRequest(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (!path.startsWith("/companies")) {
            return false;
        }
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return "/companies/new".equals(path)
                || (path.startsWith("/companies/") && path.endsWith("/edit"));
    }

    private boolean canManageCompanies(SessionUser user) {
        return Role.ADMIN.equals(user.getRoleName()) || Role.RECRUITER.equals(user.getRoleName());
    }

    private boolean isJobWriteRequest(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (!path.startsWith("/jobs")) return false;
        if (!"GET".equalsIgnoreCase(request.getMethod())) return true;
        return "/jobs/new".equals(path) || (path.startsWith("/jobs/") && path.endsWith("/edit"));
    }

    private boolean isCandidateAccessDenied(HttpServletRequest request, SessionUser user) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (!path.startsWith("/candidates")) return false;
        if (path.equals("/candidates/me")) return !Role.CANDIDATE.equals(user.getRoleName());
        return !(Role.ADMIN.equals(user.getRoleName()) || Role.RECRUITER.equals(user.getRoleName()));
    }
}
