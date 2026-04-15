package dawn.httt.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Username khong duoc de trong.")
    private String username;

    @NotBlank(message = "Password khong duoc de trong.")
    private String password;

    private String selectedRoleCode;
}
