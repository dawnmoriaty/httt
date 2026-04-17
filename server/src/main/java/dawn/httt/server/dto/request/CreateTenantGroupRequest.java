package dawn.httt.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTenantGroupRequest {

    @NotBlank(message = "Ma nhom khong duoc de trong.")
    private String code;

    @NotBlank(message = "Ten nhom khong duoc de trong.")
    private String name;

    @NotNull(message = "Nguoi dai dien khong duoc de trong.")
    private Long representativeUserId;

    private Integer status;

    private String note;
}
