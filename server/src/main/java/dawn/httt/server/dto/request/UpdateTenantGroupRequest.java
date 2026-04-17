package dawn.httt.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTenantGroupRequest {

    @NotBlank(message = "Ten nhom khong duoc de trong.")
    private String name;

    @NotNull(message = "Nguoi dai dien khong duoc de trong.")
    private Long representativeUserId;

    @NotNull(message = "Status khong duoc de trong.")
    private Integer status;

    private String note;
}
