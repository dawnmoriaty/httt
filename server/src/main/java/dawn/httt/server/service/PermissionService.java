package dawn.httt.server.service;

import dawn.httt.server.dto.response.PermissionResponse;
import dawn.httt.server.entity.PermissionEntity;
import dawn.httt.server.repository.PermissionRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAllByOrderByModuleNameAscResourceNameAscActionNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PermissionResponse toResponse(PermissionEntity permissionEntity) {
        return PermissionResponse.builder()
                .id(permissionEntity.getId())
                .moduleCode(permissionEntity.getModuleCode())
                .moduleName(permissionEntity.getModuleName())
                .resourceCode(permissionEntity.getResourceCode())
                .resourceName(permissionEntity.getResourceName())
                .actionCode(permissionEntity.getActionCode())
                .actionName(permissionEntity.getActionName())
                .status(permissionEntity.getStatus())
                .build();
    }
}
