package dawn.httt.server.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionExportMailRequest {

    @Email(message = "Email khong hop le.")
    @NotBlank(message = "Email nguoi nhan khong duoc de trong.")
    private String email;
}
