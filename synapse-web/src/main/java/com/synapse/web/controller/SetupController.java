package com.synapse.web.controller;

import com.synapse.core.initialization.DataInitializationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for system setup operations.
 */
@RestController
@RequestMapping("/setup")
@CrossOrigin(origins = "*")
public class SetupController {

    @Autowired
    private DataInitializationService dataInitializationService;

    /**
     * Get system setup status.
     */
    @GetMapping("/status")
    public ResponseEntity<DataInitializationService.SetupStatus> getSetupStatus() {
        DataInitializationService.SetupStatus status = dataInitializationService.getSetupStatus();
        return ResponseEntity.ok(status);
    }

    /**
     * Check if system initialization is completed.
     */
    @GetMapping("/initialized")
    public ResponseEntity<Boolean> isInitialized() {
        boolean initialized = dataInitializationService.isInitialized();
        return ResponseEntity.ok(initialized);
    }
}