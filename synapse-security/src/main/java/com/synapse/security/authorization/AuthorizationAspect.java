package com.synapse.security.authorization;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.UUID;

/**
 * Aspect for handling custom authorization annotations.
 */
@Aspect
@Component
public class AuthorizationAspect {

    @Autowired
    private AuthorizationService authorizationService;

    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        String[] roles = requireRole.value();
        boolean requireAll = requireRole.requireAll();

        boolean hasAccess = requireAll ? 
            authorizationService.hasAllRoles(roles) : 
            authorizationService.hasAnyRole(roles);

        if (!hasAccess) {
            throw new AccessDeniedException("Insufficient role permissions");
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(requireProjectAccess)")
    public Object checkProjectAccess(ProceedingJoinPoint joinPoint, RequireProjectAccess requireProjectAccess) throws Throwable {
        UUID projectId = extractProjectId(joinPoint, requireProjectAccess.projectIdParam());
        
        if (projectId == null || !authorizationService.hasProjectAccess(projectId)) {
            throw new AccessDeniedException("Insufficient project access permissions");
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(requireDepartmentAccess)")
    public Object checkDepartmentAccess(ProceedingJoinPoint joinPoint, RequireDepartmentAccess requireDepartmentAccess) throws Throwable {
        UUID departmentId = extractDepartmentId(joinPoint, requireDepartmentAccess.departmentIdParam());
        
        if (departmentId == null || !authorizationService.hasDepartmentAccess(departmentId)) {
            throw new AccessDeniedException("Insufficient department access permissions");
        }

        return joinPoint.proceed();
    }

    private UUID extractProjectId(ProceedingJoinPoint joinPoint, String paramName) {
        return extractUUIDParameter(joinPoint, paramName);
    }

    private UUID extractDepartmentId(ProceedingJoinPoint joinPoint, String paramName) {
        return extractUUIDParameter(joinPoint, paramName);
    }

    private UUID extractUUIDParameter(ProceedingJoinPoint joinPoint, String paramName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals(paramName) && args[i] instanceof UUID) {
                return (UUID) args[i];
            }
        }

        return null;
    }
}