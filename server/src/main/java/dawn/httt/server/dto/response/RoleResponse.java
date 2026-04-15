package dawn.httt.server.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoleResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer status;
    private Boolean systemRole;
    private List<Long> permissionIds;
    private List<String> permissionKeys;
}
