package dawn.httt.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoleRequest {

    @NotBlank(message = "Role name khong duoc de trong.")
    private String name;

    private String description;

    @NotNull(message = "Status khong duoc de trong.")
    private Integer status;
}
