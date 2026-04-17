package dawn.httt.server.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TenantGroupMemberResponse {

    private Long id;
    private Long tenantGroupId;
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private Integer memberRole;
    private String joinedAt;
    private String leftAt;
    private String idCardNumber;
    private String idCardFront;
    private String idCardBack;
}
