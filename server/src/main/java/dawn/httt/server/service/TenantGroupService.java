package dawn.httt.server.service;

import dawn.httt.server.constant.CommonStatusConstant;
import dawn.httt.server.constant.ContractStatusConstant;
import dawn.httt.server.constant.RoleCodeConstant;
import dawn.httt.server.constant.TenantMemberRoleConstant;
import dawn.httt.server.dto.request.CreateTenantGroupMemberRequest;
import dawn.httt.server.dto.request.CreateTenantGroupRequest;
import dawn.httt.server.dto.request.UpdateTenantGroupMemberRequest;
import dawn.httt.server.dto.request.UpdateTenantGroupRequest;
import dawn.httt.server.dto.response.TenantGroupMemberResponse;
import dawn.httt.server.dto.response.TenantGroupResponse;
import dawn.httt.server.entity.ContractEntity;
import dawn.httt.server.entity.TenantGroupEntity;
import dawn.httt.server.entity.TenantGroupMemberEntity;
import dawn.httt.server.entity.UserEntity;
import dawn.httt.server.exception.BadRequestException;
import dawn.httt.server.exception.ForbiddenException;
import dawn.httt.server.exception.NotFoundException;
import dawn.httt.server.repository.ContractRepository;
import dawn.httt.server.repository.TenantGroupMemberRepository;
import dawn.httt.server.repository.TenantGroupRepository;
import dawn.httt.server.repository.UserRepository;
import dawn.httt.server.security.AuthenticatedUser;
import dawn.httt.server.security.CurrentAuthenticatedUserProvider;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantGroupService {

    private final TenantGroupRepository tenantGroupRepository;
    private final TenantGroupMemberRepository tenantGroupMemberRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final CurrentAuthenticatedUserProvider currentAuthenticatedUserProvider;

    public TenantGroupService(
            TenantGroupRepository tenantGroupRepository,
            TenantGroupMemberRepository tenantGroupMemberRepository,
            UserRepository userRepository,
            ContractRepository contractRepository,
            CurrentAuthenticatedUserProvider currentAuthenticatedUserProvider
    ) {
        this.tenantGroupRepository = tenantGroupRepository;
        this.tenantGroupMemberRepository = tenantGroupMemberRepository;
        this.userRepository = userRepository;
        this.contractRepository = contractRepository;
        this.currentAuthenticatedUserProvider = currentAuthenticatedUserProvider;
    }

    @Transactional(readOnly = true)
    public Page<TenantGroupResponse> getAll(Pageable pageable) {
        AuthenticatedUser currentUser = currentAuthenticatedUserProvider.getCurrentUser();
        if (isSuperAdmin(currentUser)) {
            return tenantGroupRepository.findAllByOrderByIdDesc(pageable).map(this::toResponse);
        }

        return tenantGroupRepository.findAllByRepresentativeUserIdOrderByIdDesc(currentUser.getUserId(), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<TenantGroupResponse> getAll(String query, Pageable pageable) {
        AuthenticatedUser currentUser = currentAuthenticatedUserProvider.getCurrentUser();
        boolean hasQuery = hasText(query);

        Page<TenantGroupEntity> page;
        if (isSuperAdmin(currentUser)) {
            page = hasQuery
                    ? tenantGroupRepository.searchByKeyword(query.trim(), pageable)
                    : tenantGroupRepository.findAllByOrderByIdDesc(pageable);
        } else {
            page = hasQuery
                    ? tenantGroupRepository.searchByRepresentativeAndKeyword(currentUser.getUserId(), query.trim(), pageable)
                    : tenantGroupRepository.findAllByRepresentativeUserIdOrderByIdDesc(currentUser.getUserId(), pageable);
        }

        List<Long> ids = page.getContent().stream().map(TenantGroupEntity::getId).toList();
        Map<Long, TenantGroupEntity> groupMap = tenantGroupRepository.findByIdIn(ids)
                .stream()
                .collect(Collectors.toMap(TenantGroupEntity::getId, Function.identity()));

        return page.map(group -> toResponse(groupMap.getOrDefault(group.getId(), group)));
    }

    @Transactional(readOnly = true)
    public TenantGroupResponse getById(Long tenantGroupId) {
        TenantGroupEntity tenantGroupEntity = getAuthorizedTenantGroup(tenantGroupId);
        return toResponse(tenantGroupEntity);
    }

    @Transactional
    public TenantGroupResponse create(CreateTenantGroupRequest request) {
        validateCodeForCreate(request.getCode());

        UserEntity representative = getRepresentativeUser(request.getRepresentativeUserId());
        Integer status = normalizeStatus(request.getStatus(), true);

        TenantGroupEntity tenantGroupEntity = new TenantGroupEntity();
        tenantGroupEntity.setCode(request.getCode().trim().toUpperCase(Locale.ROOT));
        tenantGroupEntity.setName(request.getName().trim());
        tenantGroupEntity.setRepresentativeUserId(representative.getId());
        tenantGroupEntity.setStatus(status);
        tenantGroupEntity.setNote(trimToNull(request.getNote()));

        TenantGroupEntity saved = tenantGroupRepository.save(tenantGroupEntity);
        ensureRepresentativeMember(saved, representative.getId());
        normalizeRepresentativeRoles(saved.getId(), representative.getId());

        return toResponse(saved);
    }

    @Transactional
    public TenantGroupResponse update(Long tenantGroupId, UpdateTenantGroupRequest request) {
        TenantGroupEntity tenantGroupEntity = getAuthorizedTenantGroup(tenantGroupId);

        UserEntity representative = getRepresentativeUser(request.getRepresentativeUserId());
        Integer status = normalizeStatus(request.getStatus(), false);

        tenantGroupEntity.setName(request.getName().trim());
        tenantGroupEntity.setRepresentativeUserId(representative.getId());
        tenantGroupEntity.setStatus(status);
        tenantGroupEntity.setNote(trimToNull(request.getNote()));

        ensureCannotDeactivateWhenActiveContract(tenantGroupEntity);
        ensureRepresentativeMember(tenantGroupEntity, representative.getId());
        normalizeRepresentativeRoles(tenantGroupEntity.getId(), representative.getId());

        return toResponse(tenantGroupRepository.save(tenantGroupEntity));
    }

    @Transactional
    public void delete(Long tenantGroupId) {
        TenantGroupEntity tenantGroupEntity = getAuthorizedTenantGroup(tenantGroupId);

        if (hasActiveContract(tenantGroupEntity.getId())) {
            throw new BadRequestException("TENANT_GROUP_HAS_ACTIVE_CONTRACT", "Nhom dang co hop dong hieu luc, khong the xoa.");
        }

        tenantGroupMemberRepository.deleteByTenantGroup_Id(tenantGroupEntity.getId());
        tenantGroupRepository.delete(tenantGroupEntity);
    }

    @Transactional(readOnly = true)
    public Page<TenantGroupMemberResponse> getMembers(Long tenantGroupId, Pageable pageable) {
        TenantGroupEntity tenantGroupEntity = getAuthorizedTenantGroup(tenantGroupId);
        return tenantGroupMemberRepository
                .findByTenantGroup_IdOrderByIdAsc(tenantGroupEntity.getId(), pageable)
                .map(this::toMemberResponse);
    }

    @Transactional(readOnly = true)
    public Page<TenantGroupMemberResponse> getMembers(Long tenantGroupId, String query, Pageable pageable) {
        TenantGroupEntity tenantGroupEntity = getAuthorizedTenantGroup(tenantGroupId);
        Page<TenantGroupMemberEntity> page = hasText(query)
                ? tenantGroupMemberRepository.searchByTenantGroupAndKeyword(tenantGroupEntity.getId(), query.trim(), pageable)
                : tenantGroupMemberRepository.findByTenantGroup_IdOrderByIdAsc(tenantGroupEntity.getId(), pageable);

        return page.map(this::toMemberResponse);
    }

    @Transactional
    public TenantGroupMemberResponse addMember(Long tenantGroupId, CreateTenantGroupMemberRequest request) {
        TenantGroupEntity tenantGroupEntity = getAuthorizedTenantGroup(tenantGroupId);
        UserEntity userEntity = getMemberUser(request.getUserId());
        Integer memberRole = normalizeMemberRole(request.getMemberRole());

        if (tenantGroupMemberRepository.existsByTenantGroup_IdAndUser_Id(tenantGroupEntity.getId(), userEntity.getId())) {
            throw new BadRequestException("TENANT_GROUP_MEMBER_EXISTS", "Nguoi dung da ton tai trong nhom.");
        }

        TenantGroupMemberEntity memberEntity = new TenantGroupMemberEntity();
        memberEntity.setTenantGroup(tenantGroupEntity);
        memberEntity.setUser(userEntity);
        memberEntity.setMemberRole(memberRole);
        memberEntity.setJoinedAt(parseDateOrNull(request.getJoinedAt(), "joinedAt"));
        memberEntity.setLeftAt(parseDateOrNull(request.getLeftAt(), "leftAt"));
        memberEntity.setIdCardNumber(trimToNull(request.getIdCardNumber()));
        memberEntity.setIdCardFront(trimToNull(request.getIdCardFront()));
        memberEntity.setIdCardBack(trimToNull(request.getIdCardBack()));

        validateMemberDateRange(memberEntity.getJoinedAt(), memberEntity.getLeftAt());
        TenantGroupMemberEntity saved = tenantGroupMemberRepository.save(memberEntity);

        if (memberRole.equals(TenantMemberRoleConstant.REPRESENTATIVE)) {
            tenantGroupEntity.setRepresentativeUserId(userEntity.getId());
            tenantGroupRepository.save(tenantGroupEntity);
            normalizeRepresentativeRoles(tenantGroupEntity.getId(), userEntity.getId());
        }

        return toMemberResponse(saved);
    }

    @Transactional
    public TenantGroupMemberResponse updateMember(Long tenantGroupId, Long memberId, UpdateTenantGroupMemberRequest request) {
        TenantGroupEntity tenantGroupEntity = getAuthorizedTenantGroup(tenantGroupId);
        TenantGroupMemberEntity memberEntity = tenantGroupMemberRepository
                .findByIdAndTenantGroup_Id(memberId, tenantGroupEntity.getId())
                .orElseThrow(() -> new NotFoundException("TENANT_GROUP_MEMBER_NOT_FOUND", "Khong tim thay thanh vien trong nhom."));

        Integer memberRole = normalizeMemberRole(request.getMemberRole());
        memberEntity.setMemberRole(memberRole);
        memberEntity.setJoinedAt(parseDateOrNull(request.getJoinedAt(), "joinedAt"));
        memberEntity.setLeftAt(parseDateOrNull(request.getLeftAt(), "leftAt"));
        memberEntity.setIdCardNumber(trimToNull(request.getIdCardNumber()));
        memberEntity.setIdCardFront(trimToNull(request.getIdCardFront()));
        memberEntity.setIdCardBack(trimToNull(request.getIdCardBack()));

        validateMemberDateRange(memberEntity.getJoinedAt(), memberEntity.getLeftAt());
        TenantGroupMemberEntity saved = tenantGroupMemberRepository.save(memberEntity);

        if (memberRole.equals(TenantMemberRoleConstant.REPRESENTATIVE)) {
            tenantGroupEntity.setRepresentativeUserId(saved.getUser().getId());
            tenantGroupRepository.save(tenantGroupEntity);
            normalizeRepresentativeRoles(tenantGroupEntity.getId(), saved.getUser().getId());
        }

        return toMemberResponse(saved);
    }

    @Transactional
    public void removeMember(Long tenantGroupId, Long memberId) {
        TenantGroupEntity tenantGroupEntity = getAuthorizedTenantGroup(tenantGroupId);
        TenantGroupMemberEntity memberEntity = tenantGroupMemberRepository
                .findByIdAndTenantGroup_Id(memberId, tenantGroupEntity.getId())
                .orElseThrow(() -> new NotFoundException("TENANT_GROUP_MEMBER_NOT_FOUND", "Khong tim thay thanh vien trong nhom."));

        if (memberEntity.getUser().getId().equals(tenantGroupEntity.getRepresentativeUserId())) {
            throw new BadRequestException("TENANT_GROUP_REPRESENTATIVE_REMOVE_FORBIDDEN", "Khong the xoa nguoi dai dien hien tai.");
        }

        tenantGroupMemberRepository.delete(memberEntity);
    }

    private TenantGroupEntity getAuthorizedTenantGroup(Long tenantGroupId) {
        AuthenticatedUser currentUser = currentAuthenticatedUserProvider.getCurrentUser();
        TenantGroupEntity tenantGroupEntity = tenantGroupRepository.findById(tenantGroupId)
                .orElseThrow(() -> new NotFoundException("TENANT_GROUP_NOT_FOUND", "Khong tim thay nhom nguoi thue."));

        if (isSuperAdmin(currentUser)) {
            return tenantGroupEntity;
        }

        if (!tenantGroupEntity.getRepresentativeUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("DATA_OWNERSHIP_FORBIDDEN", "Ban chi co the thao tac tren nhom do ban quan ly.");
        }

        return tenantGroupEntity;
    }

    private void validateCodeForCreate(String rawCode) {
        if (rawCode == null || rawCode.trim().isEmpty()) {
            throw new BadRequestException("TENANT_GROUP_CODE_REQUIRED", "Ma nhom khong duoc de trong.");
        }

        String normalizedCode = rawCode.trim().toUpperCase(Locale.ROOT);
        if (tenantGroupRepository.existsByCode(normalizedCode)) {
            throw new BadRequestException("TENANT_GROUP_CODE_EXISTS", "Ma nhom da ton tai.");
        }
    }

    private UserEntity getRepresentativeUser(Long userId) {
        UserEntity userEntity = getMemberUser(userId);
        if (userEntity.getStatus() == null || userEntity.getStatus() != CommonStatusConstant.STATUS_ACTIVE) {
            throw new BadRequestException("REPRESENTATIVE_USER_INACTIVE", "Nguoi dai dien phai la tai khoan dang hoat dong.");
        }
        return userEntity;
    }

    private UserEntity getMemberUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "Khong tim thay user."));
    }

    private Integer normalizeStatus(Integer status, boolean allowNull) {
        if (status == null) {
            if (allowNull) {
                return CommonStatusConstant.STATUS_ACTIVE;
            }
            throw new BadRequestException("TENANT_GROUP_STATUS_REQUIRED", "Trang thai nhom khong duoc de trong.");
        }

        if (status != CommonStatusConstant.STATUS_ACTIVE && status != CommonStatusConstant.STATUS_INACTIVE) {
            throw new BadRequestException("TENANT_GROUP_STATUS_INVALID", "Trang thai nhom khong hop le.");
        }

        return status;
    }

    private Integer normalizeMemberRole(Integer memberRole) {
        if (memberRole == null) {
            throw new BadRequestException("TENANT_MEMBER_ROLE_REQUIRED", "Vai tro thanh vien khong duoc de trong.");
        }

        if (memberRole != TenantMemberRoleConstant.REPRESENTATIVE && memberRole != TenantMemberRoleConstant.MEMBER) {
            throw new BadRequestException("TENANT_MEMBER_ROLE_INVALID", "Vai tro thanh vien khong hop le.");
        }

        return memberRole;
    }

    private LocalDate parseDateOrNull(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException exception) {
            throw new BadRequestException("INVALID_DATE_FORMAT", String.format("Truong %s phai theo dinh dang yyyy-MM-dd.", fieldName));
        }
    }

    private void validateMemberDateRange(LocalDate joinedAt, LocalDate leftAt) {
        if (joinedAt != null && leftAt != null && leftAt.isBefore(joinedAt)) {
            throw new BadRequestException("TENANT_MEMBER_DATE_RANGE_INVALID", "Ngay roi nhom phai lon hon hoac bang ngay tham gia.");
        }
    }

    private void ensureCannotDeactivateWhenActiveContract(TenantGroupEntity tenantGroupEntity) {
        if (tenantGroupEntity.getStatus() != null
                && tenantGroupEntity.getStatus().equals(CommonStatusConstant.STATUS_INACTIVE)
                && hasActiveContract(tenantGroupEntity.getId())) {
            throw new BadRequestException(
                    "TENANT_GROUP_HAS_ACTIVE_CONTRACT",
                    "Khong the chuyen nhom sang ngung hoat dong khi con hop dong hieu luc."
            );
        }
    }

    private boolean hasActiveContract(Long tenantGroupId) {
        List<ContractEntity> contracts = contractRepository.findByTenantGroupIdAndStatus(tenantGroupId, ContractStatusConstant.ACTIVE);
        return !contracts.isEmpty();
    }

    private void ensureRepresentativeMember(TenantGroupEntity tenantGroupEntity, Long representativeUserId) {
        TenantGroupMemberEntity representative = tenantGroupMemberRepository
                .findByTenantGroup_IdAndUser_Id(tenantGroupEntity.getId(), representativeUserId)
                .orElse(null);

        if (representative == null) {
            TenantGroupMemberEntity memberEntity = new TenantGroupMemberEntity();
            memberEntity.setTenantGroup(tenantGroupEntity);
            memberEntity.setUser(getMemberUser(representativeUserId));
            memberEntity.setMemberRole(TenantMemberRoleConstant.REPRESENTATIVE);
            memberEntity.setJoinedAt(LocalDate.now());
            tenantGroupMemberRepository.save(memberEntity);
            return;
        }

        if (!representative.getMemberRole().equals(TenantMemberRoleConstant.REPRESENTATIVE)) {
            representative.setMemberRole(TenantMemberRoleConstant.REPRESENTATIVE);
            tenantGroupMemberRepository.save(representative);
        }
    }

    private TenantGroupResponse toResponse(TenantGroupEntity tenantGroupEntity) {
        UserEntity representative = userRepository.findById(tenantGroupEntity.getRepresentativeUserId()).orElse(null);

        long memberCount = tenantGroupMemberRepository.countByTenantGroup_Id(tenantGroupEntity.getId());

        return TenantGroupResponse.builder()
                .id(tenantGroupEntity.getId())
                .code(tenantGroupEntity.getCode())
                .name(tenantGroupEntity.getName())
                .representativeUserId(tenantGroupEntity.getRepresentativeUserId())
                .representativeFullName(representative != null ? representative.getFullName() : null)
                .status(tenantGroupEntity.getStatus())
                .note(tenantGroupEntity.getNote())
                .memberCount(memberCount)
                .hasActiveContract(hasActiveContract(tenantGroupEntity.getId()))
                .build();
    }

    private void normalizeRepresentativeRoles(Long tenantGroupId, Long representativeUserId) {
        List<TenantGroupMemberEntity> representatives = new ArrayList<>(tenantGroupMemberRepository.findRepresentatives(tenantGroupId));

        for (TenantGroupMemberEntity memberEntity : representatives) {
            if (memberEntity.getUser().getId().equals(representativeUserId)) {
                continue;
            }

            memberEntity.setMemberRole(TenantMemberRoleConstant.MEMBER);
            tenantGroupMemberRepository.save(memberEntity);
        }
    }

    private TenantGroupMemberResponse toMemberResponse(TenantGroupMemberEntity memberEntity) {
        return TenantGroupMemberResponse.builder()
                .id(memberEntity.getId())
                .tenantGroupId(memberEntity.getTenantGroup().getId())
                .userId(memberEntity.getUser().getId())
                .username(memberEntity.getUser().getUsername())
                .fullName(memberEntity.getUser().getFullName())
                .email(memberEntity.getUser().getEmail())
                .memberRole(memberEntity.getMemberRole())
                .joinedAt(memberEntity.getJoinedAt() == null ? null : memberEntity.getJoinedAt().toString())
                .leftAt(memberEntity.getLeftAt() == null ? null : memberEntity.getLeftAt().toString())
                .idCardNumber(memberEntity.getIdCardNumber())
                .idCardFront(memberEntity.getIdCardFront())
                .idCardBack(memberEntity.getIdCardBack())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isSuperAdmin(AuthenticatedUser currentUser) {
        return currentUser != null
                && currentUser.getRoleCodes() != null
                && currentUser.getRoleCodes().contains(RoleCodeConstant.SUPER_ADMIN);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
