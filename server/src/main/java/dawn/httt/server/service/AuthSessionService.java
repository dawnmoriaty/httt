package dawn.httt.server.service;

import dawn.httt.server.constant.CacheKeyConstant;
import dawn.httt.server.integration.redis.RedisService;
import dawn.httt.server.security.AuthenticatedUser;
import dawn.httt.server.security.RefreshSession;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class AuthSessionService {

    private final RedisService redisService;

    public AuthSessionService(RedisService redisService) {
        this.redisService = redisService;
    }

    public boolean isRedisEnabled() {
        return redisService.isConnected();
    }

    public void saveAccessSession(String tokenId, AuthenticatedUser authenticatedUser, Duration ttl) {
        if (!isRedisEnabled()) {
            return;
        }

        redisService.set(accessKey(tokenId), authenticatedUser, ttl);
        redisService.set(userSessionIndexKey(authenticatedUser.getUserId(), tokenId), tokenId, ttl);
    }

    public void saveRefreshSession(String refreshToken, RefreshSession refreshSession, Duration ttl) {
        if (!isRedisEnabled()) {
            return;
        }

        redisService.set(refreshKey(refreshToken), refreshSession, ttl);
        redisService.set(userRefreshIndexKey(refreshSession.getUserId(), refreshToken), refreshToken, ttl);
    }

    public Optional<AuthenticatedUser> getAccessSession(String tokenId) {
        if (!isRedisEnabled()) {
            return Optional.empty();
        }

        return redisService.get(accessKey(tokenId), AuthenticatedUser.class);
    }

    public Optional<RefreshSession> getRefreshSession(String refreshToken) {
        if (!isRedisEnabled()) {
            return Optional.empty();
        }

        return redisService.get(refreshKey(refreshToken), RefreshSession.class);
    }

    public void removeAccessSession(Long userId, String tokenId) {
        if (!isRedisEnabled()) {
            return;
        }

        redisService.delete(accessKey(tokenId), userSessionIndexKey(userId, tokenId));
    }

    public void removeRefreshSession(Long userId, String refreshToken) {
        if (!isRedisEnabled()) {
            return;
        }

        redisService.delete(refreshKey(refreshToken), userRefreshIndexKey(userId, refreshToken));
    }

    public boolean isBlacklisted(String tokenId) {
        return isRedisEnabled() && redisService.get(blacklistKey(tokenId), String.class).isPresent();
    }

    public void blacklist(String tokenId, Duration ttl) {
        if (!isRedisEnabled() || ttl.isNegative() || ttl.isZero()) {
            return;
        }

        redisService.set(blacklistKey(tokenId), "revoked", ttl);
    }

    public void invalidateUserSessions(Long userId) {
        if (!isRedisEnabled()) {
            return;
        }

        removeIndexedSessions(redisService.keys(userSessionIndexPattern(userId)), true);
        removeIndexedSessions(redisService.keys(userRefreshIndexPattern(userId)), false);
    }

    private void removeIndexedSessions(Set<String> indexKeys, boolean accessSession) {
        if (indexKeys.isEmpty()) {
            return;
        }

        List<String> keysToDelete = new ArrayList<>();
        for (String indexKey : indexKeys) {
            keysToDelete.add(indexKey);
            String token = indexKey.substring(indexKey.lastIndexOf(':') + 1);
            keysToDelete.add(accessSession ? accessKey(token) : refreshKey(token));
        }

        redisService.delete(keysToDelete.toArray(String[]::new));
    }

    private String accessKey(String tokenId) {
        return String.format(CacheKeyConstant.AUTH_SESSION, tokenId);
    }

    private String refreshKey(String refreshToken) {
        return String.format(CacheKeyConstant.AUTH_REFRESH, refreshToken);
    }

    private String blacklistKey(String tokenId) {
        return String.format(CacheKeyConstant.AUTH_BLACKLIST, tokenId);
    }

    private String userSessionIndexKey(Long userId, String tokenId) {
        return String.format(CacheKeyConstant.AUTH_USER_SESSION_INDEX, userId, tokenId);
    }

    private String userRefreshIndexKey(Long userId, String refreshToken) {
        return String.format(CacheKeyConstant.AUTH_USER_REFRESH_INDEX, userId, refreshToken);
    }

    private String userSessionIndexPattern(Long userId) {
        return String.format(CacheKeyConstant.AUTH_USER_SESSION_INDEX, userId, "*");
    }

    private String userRefreshIndexPattern(Long userId) {
        return String.format(CacheKeyConstant.AUTH_USER_REFRESH_INDEX, userId, "*");
    }
}
