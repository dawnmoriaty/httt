package dawn.httt.server.security;

import dawn.httt.server.exception.ForbiddenException;
import dawn.httt.server.exception.UnauthorizedException;
import dawn.httt.server.service.PermissionGuard;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RequirePermissionAspect {

    private final PermissionGuard permissionGuard;

    public RequirePermissionAspect(PermissionGuard permissionGuard) {
        this.permissionGuard = permissionGuard;
    }

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new UnauthorizedException("UNAUTHORIZED", "Ban chua dang nhap.");
        }

        if (!permissionGuard.hasPermission(authenticatedUser, requirePermission.resource(), requirePermission.action())) {
            throw new ForbiddenException("FORBIDDEN", "Ban khong co quyen thuc hien thao tac nay.");
        }

        return joinPoint.proceed();
    }
}
