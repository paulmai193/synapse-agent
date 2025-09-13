package com.synapse.web.controller;

import com.synapse.core.service.UserService;
import com.synapse.data.entity.User;
import com.synapse.web.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for user management operations.
 */
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserDto.RegisterRequest request) {
        try {
            User user = userService.registerUser(request.getUsername(), request.getEmail(), request.getPassword());
            return ResponseEntity.ok(new UserDto(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('PROJECT_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userService.findAllActiveUsers();
        List<UserDto> userDtos = users.stream()
            .map(UserDto::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('PROJECT_ADMIN') or hasRole('DEPARTMENT_ADMIN') or #userId == authentication.principal.user.userId")
    public ResponseEntity<UserDto> getUser(@PathVariable UUID userId) {
        try {
            User user = userService.findAllActiveUsers().stream()
                .filter(u -> u.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            return ResponseEntity.ok(new UserDto(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> assignRole(@PathVariable UUID userId, 
                                         @Valid @RequestBody UserDto.RoleAssignmentRequest request) {
        try {
            userService.assignRole(userId, request.getRoleName());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{userId}/roles/{roleName}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> removeRole(@PathVariable UUID userId, @PathVariable String roleName) {
        try {
            userService.removeRole(userId, roleName);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{userId}/projects")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('PROJECT_ADMIN')")
    public ResponseEntity<Void> assignProject(@PathVariable UUID userId, 
                                            @Valid @RequestBody UserDto.ProjectAssignmentRequest request) {
        try {
            userService.assignProject(userId, request.getProjectId());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{userId}/projects")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('PROJECT_ADMIN')")
    public ResponseEntity<Void> removeFromProject(@PathVariable UUID userId) {
        try {
            userService.removeFromProject(userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{userId}/departments")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Void> assignDepartment(@PathVariable UUID userId, 
                                               @Valid @RequestBody UserDto.DepartmentAssignmentRequest request) {
        try {
            userService.assignDepartment(userId, request.getDepartmentId());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{userId}/departments/{departmentId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Void> removeFromDepartment(@PathVariable UUID userId, @PathVariable UUID departmentId) {
        try {
            userService.removeFromDepartment(userId, departmentId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> updateUserStatus(@PathVariable UUID userId, 
                                                @Valid @RequestBody UserDto.StatusUpdateRequest request) {
        try {
            userService.updateUserStatus(userId, request.getStatus());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> softDeleteUser(@PathVariable UUID userId) {
        try {
            userService.softDeleteUser(userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}