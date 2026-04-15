package dawn.httt.server.security;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long selectedRoleId;
    private Long sessionVersion;
}
