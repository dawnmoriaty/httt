package dawn.httt.server.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PermissionResponse {

    private Long id;
    private String moduleCode;
    private String moduleName;
    private String resourceCode;
    private String resourceName;
    private String actionCode;
    private String actionName;
    private Integer status;
}
