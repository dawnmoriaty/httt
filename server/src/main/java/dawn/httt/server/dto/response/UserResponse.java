package dawn.httt.server.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private Integer status;
    private Long sessionVersion;
    private List<RoleSummaryResponse> roles;
}
