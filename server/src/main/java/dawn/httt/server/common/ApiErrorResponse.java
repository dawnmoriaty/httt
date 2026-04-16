package dawn.httt.server.common;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiErrorResponse {

    private String errorCode;
    private String message;
    private Map<String, String> fieldErrors;
}
