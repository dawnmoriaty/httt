package dawn.httt.server.integration.redis;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

public interface RedisService {

    boolean isConnected();

    <T> Optional<T> get(String key, Class<T> type);

    void set(String key, Object value);

    void set(String key, Object value, Duration ttl);

    boolean setIfAbsent(String key, Object value, Duration ttl);

    void delete(String... keys);

    Set<String> keys(String pattern);

    void deleteByPattern(String pattern);
}
