package dawn.httt.server.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TenantGroupResponse {

    private Long id;
    private String code;
    private String name;
    private Long representativeUserId;
    private String representativeFullName;
    private Integer status;
    private String note;
    private Long memberCount;
    private Boolean hasActiveContract;
}
