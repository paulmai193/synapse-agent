package com.synapse.core.aspect;

import com.synapse.core.service.AuditService;
import com.synapse.data.entity.AuditLog;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Aspect for automatic audit logging using Spring AOP.
 */
@Aspect
@Component
public class AuditAspect {

    @Autowired
    private AuditService auditService;

    @AfterReturning("execution(* com.synapse.web.controller.AuthController.login(..))")
    public void logLogin(JoinPoint joinPoint) {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                InetAddress ipAddress = getClientIpAddress(request);
                String userAgent = request.getHeader("User-Agent");
                
                // Note: In real implementation, extract userId from login response
                auditService.logAction(AuditLog.USER_LOGIN, "AUTH", "login", 
                    Map.of("method", "login"), ipAddress, userAgent);
            }
        } catch (Exception e) {
            // Log error but don't fail the main operation
        }
    }

    @AfterReturning("execution(* com.synapse.web.controller.UserController.registerUser(..))")
    public void logUserRegistration(JoinPoint joinPoint) {
        try {
            auditService.logAction(AuditLog.USER_REGISTER, "USER", "registration", 
                Map.of("method", "registerUser"));
        } catch (Exception e) {
            // Log error but don't fail the main operation
        }
    }

    @AfterReturning("execution(* com.synapse.web.controller.UserController.assignRole(..))")
    public void logRoleAssignment(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0) {
                String userId = args[0].toString();
                auditService.logAction(AuditLog.ROLE_ASSIGN, "USER", userId, 
                    Map.of("method", "assignRole"));
            }
        } catch (Exception e) {
            // Log error but don't fail the main operation
        }
    }

    @AfterReturning("execution(* com.synapse.web.controller.UserController.assignProject(..))")
    public void logProjectAssignment(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0) {
                String userId = args[0].toString();
                auditService.logAction(AuditLog.PROJECT_ASSIGN, "USER", userId, 
                    Map.of("method", "assignProject"));
            }
        } catch (Exception e) {
            // Log error but don't fail the main operation
        }
    }

    @AfterReturning("execution(* com.synapse.web.controller.UserController.softDeleteUser(..))")
    public void logUserDeletion(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0) {
                String userId = args[0].toString();
                auditService.logAction(AuditLog.USER_DELETE, "USER", userId, 
                    Map.of("method", "softDeleteUser", "type", "soft_delete"));
            }
        } catch (Exception e) {
            // Log error but don't fail the main operation
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private InetAddress getClientIpAddress(HttpServletRequest request) {
        try {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return InetAddress.getByName(xForwardedFor.split(",")[0].trim());
            }
            
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return InetAddress.getByName(xRealIp);
            }
            
            return InetAddress.getByName(request.getRemoteAddr());
        } catch (UnknownHostException e) {
            return null;
        }
    }
}