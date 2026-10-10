package com.helpdesk.service;

import com.helpdesk.domain.Role;
import com.helpdesk.domain.UserAccount;
import com.helpdesk.dto.PageResponse;
import com.helpdesk.dto.UpdateUserRequest;
import com.helpdesk.dto.UserResponse;
import com.helpdesk.exception.BadRequestException;
import com.helpdesk.exception.NotFoundException;
import com.helpdesk.mapper.ApiMapper;
import com.helpdesk.repository.UserRepository;
import com.helpdesk.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(Role role, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Page<UserAccount> result = role == null
                ? userRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(safePage, safeSize))
                : userRepository.findByRoleOrderByFirstNameAsc(role, PageRequest.of(safePage, safeSize));
        return PageResponse.from(result, ApiMapper::toUser);
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request, UserPrincipal actor) {
        UserAccount user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (actor.getId().equals(user.getId()) && (request.role() != Role.ADMIN || !request.enabled())) {
            throw new BadRequestException("You cannot remove your own administrator access");
        }

        boolean wasActiveAdmin = user.getRole() == Role.ADMIN && user.isEnabled();
        boolean willBeActiveAdmin = request.role() == Role.ADMIN && request.enabled();
        if (wasActiveAdmin && !willBeActiveAdmin && userRepository.countByRoleAndEnabledTrue(Role.ADMIN) <= 1) {
            throw new BadRequestException("At least one enabled administrator is required");
        }

        user.setRole(request.role());
        user.setEnabled(request.enabled());
        return ApiMapper.toUser(user);
    }
}
