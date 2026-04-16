package dawn.httt.server.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import dawn.httt.server.exception.UnauthorizedException;
import dawn.httt.server.service.AuthSnapshotService;
import dawn.httt.server.service.AuthSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthSessionService authSessionService;
    private final AuthSnapshotService authSnapshotService;
    private final SecurityAuthenticationEntryPoint securityAuthenticationEntryPoint;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            AuthSessionService authSessionService,
            AuthSnapshotService authSnapshotService,
            SecurityAuthenticationEntryPoint securityAuthenticationEntryPoint
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authSessionService = authSessionService;
        this.authSnapshotService = authSnapshotService;
        this.securityAuthenticationEntryPoint = securityAuthenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {
            DecodedJWT decodedJWT = jwtTokenProvider.verifyAccessToken(token);
            String tokenId = decodedJWT.getId();

            if (authSessionService.isBlacklisted(tokenId)) {
                throw new UnauthorizedException("TOKEN_REVOKED", "Token da bi thu hoi.");
            }

            AuthenticatedUser authenticatedUser = resolveAuthenticatedUser(decodedJWT);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    authenticatedUser,
                    null,
                    buildAuthorities(authenticatedUser)
            );

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        } catch (Exception exception) {
            SecurityContextHolder.clearContext();
            securityAuthenticationEntryPoint.commence(
                    request,
                    response,
                    new org.springframework.security.authentication.BadCredentialsException(
                            "Token khong hop le hoac da het han.",
                            exception
                    )
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private AuthenticatedUser resolveAuthenticatedUser(DecodedJWT decodedJWT) {
        String tokenId = decodedJWT.getId();
        Optional<AuthenticatedUser> cachedUser = authSessionService.getAccessSession(tokenId);
        if (cachedUser.isPresent()) {
            return cachedUser.get();
        }

        if (authSessionService.isRedisEnabled()) {
            Long userId = decodedJWT.getClaim("userId").asLong();
            Long selectedRoleId = decodedJWT.getClaim("selectedRoleId").asLong();
            AuthenticatedUser freshUser = authSnapshotService.resolveFreshAuthenticatedUser(userId, selectedRoleId);

            Duration remainingTtl = Duration.between(Instant.now(), decodedJWT.getExpiresAtAsInstant());
            if (!remainingTtl.isNegative() && !remainingTtl.isZero()) {
                authSessionService.saveAccessSession(tokenId, freshUser, remainingTtl);
            }

            return freshUser;
        }

        return AuthenticatedUser.builder()
                .userId(decodedJWT.getClaim("userId").asLong())
                .username(decodedJWT.getClaim("username").asString())
                .fullName(decodedJWT.getClaim("fullName").asString())
                .email(decodedJWT.getClaim("email").asString())
                .selectedRoleId(decodedJWT.getClaim("selectedRoleId").asLong())
                .selectedRoleCode(decodedJWT.getClaim("selectedRoleCode").asString())
                .sessionVersion(decodedJWT.getClaim("sessionVersion").asLong())
                .roleCodes(readClaimList(decodedJWT, "roleCodes"))
                .permissions(readClaimList(decodedJWT, "permissions"))
                .build();
    }

    private List<String> readClaimList(DecodedJWT decodedJWT, String claimName) {
        List<String> claimValues = decodedJWT.getClaim(claimName).asList(String.class);
        return claimValues == null ? List.of() : claimValues;
    }

    private List<GrantedAuthority> buildAuthorities(AuthenticatedUser authenticatedUser) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (authenticatedUser.getRoleCodes() != null) {
            authenticatedUser.getRoleCodes().forEach(roleCode -> authorities.add(new SimpleGrantedAuthority("ROLE_" + roleCode)));
        }
        if (authenticatedUser.getPermissions() != null) {
            authenticatedUser.getPermissions().forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
        }
        return authorities;
    }
}
