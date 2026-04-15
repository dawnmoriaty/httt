package dawn.httt.server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoleSummaryResponse {

    private Long id;
    private String code;
    private String name;
}
