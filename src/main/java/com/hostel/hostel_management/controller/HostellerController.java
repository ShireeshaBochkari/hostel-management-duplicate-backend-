package com.hostel.hostel_management.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hostel.hostel_management.dto.PagedResponse;
import com.hostel.hostel_management.dto.ProfileUpdateRequest;
import com.hostel.hostel_management.dto.UserResponse;
import com.hostel.hostel_management.entity.User;
import com.hostel.hostel_management.service.CurrentUserService;
import com.hostel.hostel_management.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/hostellers")
public class HostellerController {

    private final UserService userService;
    private final CurrentUserService currentUserService;

    public HostellerController(
            UserService userService,
            CurrentUserService currentUserService) {
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('WARDEN', 'ADMIN')")
    public PagedResponse<UserResponse> getAllHostellers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {

        Page<UserResponse> result = userService
                .pageHostellers(PageRequest.of(page, size, Sort.by("fullName").ascending()))
                .map(UserResponse::from);
        return PagedResponse.from(result);
    }

    @GetMapping("/me")
    public UserResponse getMyProfile() {
        return UserResponse.from(currentUserService.requireUser());
    }

    @PutMapping("/me")
    public UserResponse updateMyProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        User user = currentUserService.requireUser();
        return UserResponse.from(userService.updateProfile(user, request));
    }

    @GetMapping("/my-room")
    @PreAuthorize("hasRole('HOSTELER')")
    public UserResponse getMyRoom() {
        return UserResponse.from(currentUserService.requireUser());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('WARDEN', 'ADMIN')")
    public UserResponse updateHosteller(
            @PathVariable Long id,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return UserResponse.from(userService.updateHosteller(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('WARDEN', 'ADMIN')")
    public ResponseEntity<Void> deleteHosteller(@PathVariable Long id) {
        userService.deleteHosteller(id);
        return ResponseEntity.noContent().build();
    }
}
