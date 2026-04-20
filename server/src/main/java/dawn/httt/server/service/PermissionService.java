package dawn.httt.server.service;

import dawn.httt.server.constant.CommonStatusConstant;
import dawn.httt.server.constant.PermissionActionConstant;
import dawn.httt.server.dto.request.CreateModulePermissionsRequest;
import dawn.httt.server.dto.request.CreatePermissionRequest;
import dawn.httt.server.dto.response.PermissionResponse;
import dawn.httt.server.entity.PermissionEntity;
import dawn.httt.server.exception.BadRequestException;
import dawn.httt.server.repository.PermissionRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public Page<PermissionResponse> getAllPermissions(Pageable pageable) {
        return getAllPermissions(null, pageable);
    }

    public Page<PermissionResponse> getAllPermissions(String query, Pageable pageable) {
        Page<PermissionEntity> page = hasText(query)
                ? permissionRepository.searchByKeyword(query.trim(), pageable)
                : permissionRepository.findAllByOrderByModuleNameAscResourceNameAscActionNameAsc(pageable);

        return page.map(this::toResponse);
    }

    @Transactional
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        String moduleCode = request.getModuleCode().trim().toLowerCase();
        String moduleName = request.getModuleName().trim();
        String resourceCode = request.getResourceCode().trim().toLowerCase();
        String resourceName = request.getResourceName().trim();
        String actionCode = request.getActionCode().trim().toUpperCase();
        String actionName = request.getActionName().trim();

        if (permissionRepository.findByResourceCodeAndActionCode(resourceCode, actionCode).isPresent()) {
            throw new BadRequestException("PERMISSION_EXISTS", "Permission da ton tai.");
        }

        PermissionEntity permissionEntity = new PermissionEntity();
        permissionEntity.setModuleCode(moduleCode);
        permissionEntity.setModuleName(moduleName);
        permissionEntity.setResourceCode(resourceCode);
        permissionEntity.setResourceName(resourceName);
        permissionEntity.setActionCode(actionCode);
        permissionEntity.setActionName(actionName);
        permissionEntity.setStatus(CommonStatusConstant.STATUS_ACTIVE);

        return toResponse(permissionRepository.save(permissionEntity));
    }

    @Transactional
    public List<Long> ensureModulePermissions(
            String moduleCode,
            String moduleName,
            String resourceCode,
            String resourceName,
            List<CreateModulePermissionsRequest.ActionItem> actions
    ) {
        String normalizedModuleCode = moduleCode == null ? "" : moduleCode.trim().toLowerCase();
        String normalizedModuleName = moduleName == null ? "" : moduleName.trim();
        String normalizedResourceCode = resourceCode == null ? "" : resourceCode.trim().toLowerCase();
        String normalizedResourceName = resourceName == null ? "" : resourceName.trim();

        Map<String, String> normalizedActions = new LinkedHashMap<>();
        if (actions != null) {
            for (CreateModulePermissionsRequest.ActionItem actionItem : actions) {
                String actionCode = actionItem.getActionCode() == null
                        ? ""
                        : actionItem.getActionCode().trim().toUpperCase();
                if (actionCode.isEmpty()) {
                    continue;
                }

                String actionName = actionItem.getActionName() == null
                        ? actionCode
                        : actionItem.getActionName().trim();
                normalizedActions.putIfAbsent(actionCode, actionName);
            }
        }

        if (normalizedActions.isEmpty()) {
            for (String actionCode : PermissionActionConstant.DEFAULT_ACTIONS) {
                normalizedActions.put(actionCode, toActionName(actionCode));
            }
        }

        List<Long> permissionIds = new ArrayList<>();
        for (Map.Entry<String, String> actionEntry : normalizedActions.entrySet()) {
            String actionCode = actionEntry.getKey();
            String actionName = actionEntry.getValue();
            PermissionEntity permissionEntity = permissionRepository
                    .findByResourceCodeAndActionCode(normalizedResourceCode, actionCode)
                    .orElseGet(() -> {
                        PermissionEntity created = new PermissionEntity();
                        created.setModuleCode(normalizedModuleCode);
                        created.setModuleName(normalizedModuleName);
                        created.setResourceCode(normalizedResourceCode);
                        created.setResourceName(normalizedResourceName);
                        created.setActionCode(actionCode);
                        created.setActionName(actionName);
                        created.setStatus(CommonStatusConstant.STATUS_ACTIVE);
                        return permissionRepository.save(created);
                    });
            permissionIds.add(permissionEntity.getId());
        }

        return permissionIds;
    }

    private String toActionName(String actionCode) {
        return switch (actionCode) {
            case PermissionActionConstant.VIEW -> "Xem";
            case PermissionActionConstant.ADD -> "Them";
            case PermissionActionConstant.UPDATE -> "Sua";
            case PermissionActionConstant.DELETE -> "Xoa";
            case PermissionActionConstant.TERMINATE -> "Ket thuc";
            case PermissionActionConstant.CANCEL -> "Huy";
            case PermissionActionConstant.IMPORT -> "Nhap";
            case PermissionActionConstant.EXPORT -> "Xuat";
            default -> actionCode;
        };
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

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
