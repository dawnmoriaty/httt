package dawn.httt.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoleRequest {

    @NotBlank(message = "Role code khong duoc de trong.")
    private String code;

    @NotBlank(message = "Role name khong duoc de trong.")
    private String name;

    private String description;
}
