package dawn.httt.server.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignRolePermissionsRequest {

    @NotNull(message = "Danh sach permission khong duoc null.")
    private List<Long> permissionIds;
}
