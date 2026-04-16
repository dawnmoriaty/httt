package dawn.httt.server.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class ApiResponses {

    private ApiResponses() {
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return response(HttpStatus.OK, "OK", message, data);
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return response(HttpStatus.CREATED, "CREATED", message, data);
    }

    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    private static <T> ResponseEntity<ApiResponse<T>> response(
            HttpStatus status,
            String code,
            String message,
            T data
    ) {
        ApiResponse<T> body = new ApiResponse<>(code, message, data);
        return ResponseEntity.status(status).body(body);
    }
}
