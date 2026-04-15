package dawn.httt.server.security;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatedUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private Long selectedRoleId;
    private String selectedRoleCode;
    private List<String> roleCodes;
    private List<String> permissions;
    private Long sessionVersion;

    public boolean hasPermission(String resource, String action) {
        String expectedPermission = normalizeResource(resource) + ":" + normalizeAction(action);
        return permissions != null && permissions.contains(expectedPermission);
    }

    private String normalizeResource(String resource) {
        return resource == null ? "" : resource.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeAction(String action) {
        return action == null ? "" : action.trim().toUpperCase(Locale.ROOT);
    }
}
