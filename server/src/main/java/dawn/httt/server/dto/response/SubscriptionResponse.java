package dawn.httt.server.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionResponse {

    private Long id;
    private String title;
    private String description;
    private Integer status;
    private Long ownerUserId;
}
