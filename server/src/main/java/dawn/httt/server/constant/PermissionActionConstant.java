package dawn.httt.server.constant;

import java.util.List;

public final class PermissionActionConstant {

    public static final String VIEW = "VIEW";
    public static final String ADD = "ADD";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String TERMINATE = "TERMINATE";
    public static final String CANCEL = "CANCEL";
    public static final String IMPORT = "IMPORT";
    public static final String EXPORT = "EXPORT";

    public static final List<String> DEFAULT_ACTIONS = List.of(
            VIEW,
            ADD,
            UPDATE,
            DELETE,
            TERMINATE,
            CANCEL,
            IMPORT,
            EXPORT
    );

    private PermissionActionConstant() {
    }
}
