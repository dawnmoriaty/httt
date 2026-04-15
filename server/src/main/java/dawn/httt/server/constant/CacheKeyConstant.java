package dawn.httt.server.constant;

public final class CacheKeyConstant {

    public static final String AUTH_SESSION = "auth:session:%s";
    public static final String AUTH_REFRESH = "auth:refresh:%s";
    public static final String AUTH_BLACKLIST = "auth:blacklist:%s";
    public static final String AUTH_USER_SESSION_INDEX = "auth:user-session:%s:%s";
    public static final String AUTH_USER_REFRESH_INDEX = "auth:user-refresh:%s:%s";

    private CacheKeyConstant() {
    }
}
