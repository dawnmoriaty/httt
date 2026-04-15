package dawn.httt.server.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CurrentUserResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private Long selectedRoleId;
    private String selectedRoleCode;
    private List<String> roleCodes;
    private List<String> permissions;
}
