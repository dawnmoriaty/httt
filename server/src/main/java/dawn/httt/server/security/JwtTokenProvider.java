package dawn.httt.server.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;

public interface JwtTokenProvider {

    String generateAccessToken(AuthenticatedUser authenticatedUser, String tokenId, Instant expiresAt);

    DecodedJWT verifyAccessToken(String token);
}
