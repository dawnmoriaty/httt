package dawn.httt.server.security;

import dawn.httt.server.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentAuthenticatedUserProvider {

    public AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new UnauthorizedException("UNAUTHORIZED", "Ban chua dang nhap.");
        }

        return authenticatedUser;
    }
}
