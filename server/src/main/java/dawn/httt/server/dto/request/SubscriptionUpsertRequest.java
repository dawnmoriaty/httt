package dawn.httt.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionUpsertRequest {

    @NotBlank(message = "Tieu de khong duoc de trong.")
    private String title;

    private String description;

    @NotNull(message = "Status khong duoc de trong.")
    private Integer status;
}
