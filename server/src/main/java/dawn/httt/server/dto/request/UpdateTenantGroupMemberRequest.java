package dawn.httt.server.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTenantGroupMemberRequest {

    @NotNull(message = "Vai tro thanh vien khong duoc de trong.")
    private Integer memberRole;

    private String joinedAt;

    private String leftAt;

    private String idCardNumber;

    private String idCardFront;

    private String idCardBack;
}
