package dawn.httt.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateModulePermissionsRequest {

    @NotBlank(message = "Module code khong duoc de trong.")
    private String moduleCode;

    @NotBlank(message = "Module name khong duoc de trong.")
    private String moduleName;

    @NotBlank(message = "Resource code khong duoc de trong.")
    private String resourceCode;

    @NotBlank(message = "Resource name khong duoc de trong.")
    private String resourceName;

    @NotEmpty(message = "Danh sach action khong duoc de trong.")
    private List<ActionItem> actions;

    @Getter
    @Setter
    public static class ActionItem {

        @NotBlank(message = "Action code khong duoc de trong.")
        private String actionCode;

        @NotBlank(message = "Action name khong duoc de trong.")
        private String actionName;
    }
}
