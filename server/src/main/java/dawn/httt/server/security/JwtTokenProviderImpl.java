package dawn.httt.server.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import dawn.httt.server.config.AppSecurityProperties;
import java.time.Instant;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProviderImpl implements JwtTokenProvider {

    private final Algorithm algorithm;
    private final JWTVerifier jwtVerifier;

    public JwtTokenProviderImpl(AppSecurityProperties appSecurityProperties) {
        this.algorithm = Algorithm.HMAC512(appSecurityProperties.getJwtSecret());
        this.jwtVerifier = JWT.require(algorithm).build();
    }

    @Override
    public String generateAccessToken(AuthenticatedUser authenticatedUser, String tokenId, Instant expiresAt) {
        return JWT.create()
                .withJWTId(tokenId)
                .withSubject(String.valueOf(authenticatedUser.getUserId()))
                .withClaim("userId", authenticatedUser.getUserId())
                .withClaim("username", authenticatedUser.getUsername())
                .withClaim("fullName", authenticatedUser.getFullName())
                .withClaim("email", authenticatedUser.getEmail())
                .withClaim("selectedRoleId", authenticatedUser.getSelectedRoleId())
                .withClaim("selectedRoleCode", authenticatedUser.getSelectedRoleCode())
                .withClaim("sessionVersion", authenticatedUser.getSessionVersion())
                .withClaim("roleCodes", authenticatedUser.getRoleCodes())
                .withClaim("permissions", authenticatedUser.getPermissions())
                .withExpiresAt(Date.from(expiresAt))
                .sign(algorithm);
    }

    @Override
    public DecodedJWT verifyAccessToken(String token) {
        return jwtVerifier.verify(token);
    }
}
