package dawn.httt.server.controller;

import dawn.httt.server.common.ApiResponse;
import dawn.httt.server.common.ApiResponses;
import dawn.httt.server.common.PageResponse;
import dawn.httt.server.constant.PermissionActionConstant;
import dawn.httt.server.dto.request.CreateTenantGroupMemberRequest;
import dawn.httt.server.dto.request.CreateTenantGroupRequest;
import dawn.httt.server.dto.request.UpdateTenantGroupMemberRequest;
import dawn.httt.server.dto.request.UpdateTenantGroupRequest;
import dawn.httt.server.dto.response.TenantGroupMemberResponse;
import dawn.httt.server.dto.response.TenantGroupResponse;
import dawn.httt.server.security.RequirePermission;
import dawn.httt.server.service.TenantGroupService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tenant-groups")
public class TenantGroupController {

    private final TenantGroupService tenantGroupService;

    public TenantGroupController(TenantGroupService tenantGroupService) {
        this.tenantGroupService = tenantGroupService;
    }

    @GetMapping
    @RequirePermission(resource = "tenant_group", action = PermissionActionConstant.VIEW)
    public ResponseEntity<ApiResponse<PageResponse<TenantGroupResponse>>> listTenantGroups(
            @RequestParam(name = "q", required = false) String query,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponses.ok("Lay danh sach nhom nguoi thue thanh cong.", PageResponse.from(tenantGroupService.getAll(query, pageable)));
    }

    @GetMapping("/{tenantGroupId}")
    @RequirePermission(resource = "tenant_group", action = PermissionActionConstant.VIEW)
    public ResponseEntity<ApiResponse<TenantGroupResponse>> getTenantGroup(@PathVariable Long tenantGroupId) {
        return ApiResponses.ok("Lay chi tiet nhom nguoi thue thanh cong.", tenantGroupService.getById(tenantGroupId));
    }

    @PostMapping
    @RequirePermission(resource = "tenant_group", action = PermissionActionConstant.ADD)
    public ResponseEntity<ApiResponse<TenantGroupResponse>> createTenantGroup(@Valid @RequestBody CreateTenantGroupRequest request) {
        return ApiResponses.created("Tao nhom nguoi thue thanh cong.", tenantGroupService.create(request));
    }

    @PutMapping("/{tenantGroupId}")
    @RequirePermission(resource = "tenant_group", action = PermissionActionConstant.UPDATE)
    public ResponseEntity<ApiResponse<TenantGroupResponse>> updateTenantGroup(
            @PathVariable Long tenantGroupId,
            @Valid @RequestBody UpdateTenantGroupRequest request
    ) {
        return ApiResponses.ok("Cap nhat nhom nguoi thue thanh cong.", tenantGroupService.update(tenantGroupId, request));
    }

    @DeleteMapping("/{tenantGroupId}")
    @RequirePermission(resource = "tenant_group", action = PermissionActionConstant.DELETE)
    public ResponseEntity<Void> deleteTenantGroup(@PathVariable Long tenantGroupId) {
        tenantGroupService.delete(tenantGroupId);
        return ApiResponses.noContent();
    }

    @GetMapping("/{tenantGroupId}/members")
    @RequirePermission(resource = "tenant_group_member", action = PermissionActionConstant.VIEW)
    public ResponseEntity<ApiResponse<PageResponse<TenantGroupMemberResponse>>> listMembers(
            @PathVariable Long tenantGroupId,
            @RequestParam(name = "q", required = false) String query,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ApiResponses.ok(
                "Lay danh sach thanh vien thanh cong.",
                PageResponse.from(tenantGroupService.getMembers(tenantGroupId, query, pageable))
        );
    }

    @PostMapping("/{tenantGroupId}/members")
    @RequirePermission(resource = "tenant_group_member", action = PermissionActionConstant.ADD)
    public ResponseEntity<ApiResponse<TenantGroupMemberResponse>> addMember(
            @PathVariable Long tenantGroupId,
            @Valid @RequestBody CreateTenantGroupMemberRequest request
    ) {
        return ApiResponses.created("Them thanh vien thanh cong.", tenantGroupService.addMember(tenantGroupId, request));
    }

    @PutMapping("/{tenantGroupId}/members/{memberId}")
    @RequirePermission(resource = "tenant_group_member", action = PermissionActionConstant.UPDATE)
    public ResponseEntity<ApiResponse<TenantGroupMemberResponse>> updateMember(
            @PathVariable Long tenantGroupId,
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateTenantGroupMemberRequest request
    ) {
        return ApiResponses.ok("Cap nhat thanh vien thanh cong.", tenantGroupService.updateMember(tenantGroupId, memberId, request));
    }

    @DeleteMapping("/{tenantGroupId}/members/{memberId}")
    @RequirePermission(resource = "tenant_group_member", action = PermissionActionConstant.DELETE)
    public ResponseEntity<Void> removeMember(
            @PathVariable Long tenantGroupId,
            @PathVariable Long memberId
    ) {
        tenantGroupService.removeMember(tenantGroupId, memberId);
        return ApiResponses.noContent();
    }
}
