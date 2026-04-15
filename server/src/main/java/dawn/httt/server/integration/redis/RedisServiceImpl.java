package dawn.httt.server.integration.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dawn.httt.server.exception.BadRequestException;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisServiceImpl implements RedisService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public RedisServiceImpl(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean isConnected() {
        try {
            RedisConnection connection = stringRedisTemplate.getConnectionFactory() == null
                    ? null
                    : stringRedisTemplate.getConnectionFactory().getConnection();
            return connection != null && connection.ping() != null;
        } catch (Exception exception) {
            return false;
        }
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        String rawValue = stringRedisTemplate.opsForValue().get(key);
        if (rawValue == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(rawValue, type));
        } catch (JsonProcessingException exception) {
            throw new BadRequestException("REDIS_DESERIALIZE_ERROR", "Khong the doc du lieu Redis.");
        }
    }

    @Override
    public void set(String key, Object value) {
        stringRedisTemplate.opsForValue().set(key, writeValue(value));
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
        stringRedisTemplate.opsForValue().set(key, writeValue(value), ttl);
    }

    @Override
    public boolean setIfAbsent(String key, Object value, Duration ttl) {
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, writeValue(value), ttl);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public void delete(String... keys) {
        stringRedisTemplate.delete(Set.of(keys));
    }

    @Override
    public Set<String> keys(String pattern) {
        Set<String> redisKeys = stringRedisTemplate.keys(pattern);
        return redisKeys == null ? Set.of() : new LinkedHashSet<>(redisKeys);
    }

    @Override
    public void deleteByPattern(String pattern) {
        Set<String> redisKeys = keys(pattern);
        if (!redisKeys.isEmpty()) {
            stringRedisTemplate.delete(redisKeys);
        }
    }

    private String writeValue(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BadRequestException("REDIS_SERIALIZE_ERROR", "Khong the ghi du lieu Redis.");
        }
    }
}
