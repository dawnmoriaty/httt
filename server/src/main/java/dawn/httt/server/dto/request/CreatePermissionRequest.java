package dawn.httt.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePermissionRequest {

    @NotBlank(message = "Module code khong duoc de trong.")
    private String moduleCode;

    @NotBlank(message = "Module name khong duoc de trong.")
    private String moduleName;

    @NotBlank(message = "Resource code khong duoc de trong.")
    private String resourceCode;

    @NotBlank(message = "Resource name khong duoc de trong.")
    private String resourceName;

    @NotBlank(message = "Action code khong duoc de trong.")
    private String actionCode;

    @NotBlank(message = "Action name khong duoc de trong.")
    private String actionName;
}
