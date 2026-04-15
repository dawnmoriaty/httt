package dawn.httt.server.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRolesRequest {

    @NotEmpty(message = "Can chon it nhat mot role.")
    private List<Long> roleIds;
}
