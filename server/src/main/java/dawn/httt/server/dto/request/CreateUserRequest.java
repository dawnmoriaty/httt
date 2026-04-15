package dawn.httt.server.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

    @NotBlank(message = "Username khong duoc de trong.")
    private String username;

    @NotBlank(message = "Ho ten khong duoc de trong.")
    private String fullName;

    @Email(message = "Email khong hop le.")
    @NotBlank(message = "Email khong duoc de trong.")
    private String email;

    @NotBlank(message = "Password khong duoc de trong.")
    @Size(min = 6, message = "Password phai co it nhat 6 ky tu.")
    private String password;

    @NotEmpty(message = "Can chon it nhat mot role.")
    private List<Long> roleIds;
}
