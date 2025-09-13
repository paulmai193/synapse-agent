package com.synapse.web.controller;

import com.synapse.core.service.AuditService;
import com.synapse.data.entity.AuditLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST controller for audit log management.
 */
@RestController
@RequestMapping("/audit")
@CrossOrigin(origins = "*")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @GetMapping("/logs")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(Pageable pageable) {
        Page<AuditLog> logs = auditService.getAuditLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/search")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Page<AuditLog>> searchAuditLogs(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            Pageable pageable) {
        
        Page<AuditLog> logs = auditService.searchAuditLogs(userId, actionType, resourceType, startTime, endTime, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/user/{userId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or #userId == authentication.principal.user.userId")
    public ResponseEntity<Page<AuditLog>> getUserAuditLogs(@PathVariable UUID userId, Pageable pageable) {
        Page<AuditLog> logs = auditService.getUserAuditLogs(userId, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/action/{actionType}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByAction(@PathVariable String actionType, Pageable pageable) {
        Page<AuditLog> logs = auditService.getAuditLogsByAction(actionType, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<AuditService.AuditStatistics> getAuditStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        AuditService.AuditStatistics statistics = auditService.getAuditStatistics(startTime, endTime);
        return ResponseEntity.ok(statistics);
    }
}