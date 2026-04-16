package dawn.httt.server.service;

import dawn.httt.server.constant.RoleCodeConstant;
import dawn.httt.server.security.AuthenticatedUser;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class PermissionGuard {

    public boolean hasPermission(AuthenticatedUser authenticatedUser, String resource, String action) {
        if (authenticatedUser == null) {
            return false;
        }

        if (isSuperAdmin(authenticatedUser)) {
            return true;
        }

        return authenticatedUser.hasPermission(normalizeResource(resource), normalizeAction(action));
    }

    public String toPermissionKey(String resource, String action) {
        return normalizeResource(resource) + ":" + normalizeAction(action);
    }

    public boolean isSuperAdmin(AuthenticatedUser authenticatedUser) {
        return authenticatedUser != null
                && authenticatedUser.getRoleCodes() != null
                && authenticatedUser.getRoleCodes().contains(RoleCodeConstant.SUPER_ADMIN);
    }

    private String normalizeResource(String resource) {
        return resource == null ? "" : resource.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeAction(String action) {
        return action == null ? "" : action.trim().toUpperCase(Locale.ROOT);
    }
}
