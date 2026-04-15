package dawn.httt.server.constant;

public final class SecurityPathConstant {

    public static final String PUBLIC_HEALTH = "/public/health";
    public static final String AUTH_LOGIN = "/auth/login";
    public static final String AUTH_REGISTER = "/auth/register";
    public static final String AUTH_REFRESH = "/auth/refresh";
    public static final String[] PUBLIC_ENDPOINTS = {
            PUBLIC_HEALTH,
            AUTH_LOGIN,
            AUTH_REGISTER,
            AUTH_REFRESH
    };

    private SecurityPathConstant() {
    }
}
