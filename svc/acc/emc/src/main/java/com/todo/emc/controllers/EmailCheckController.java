package com.todo.emc.controllers;

import com.todo.common.dtos.ApiResponse;
import com.todo.emc.dtos.EmailCheckResponse;
import com.todo.emc.services.EmailCheckService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/email")
public class EmailCheckController {

    private final EmailCheckService emailCheckService;

    public EmailCheckController(EmailCheckService emailCheckService) {
        this.emailCheckService = emailCheckService;
    }

    /**
     * GET /api/v1/auth/email/check?email=user@example.com
     */
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<EmailCheckResponse>> verifyEmail(
            @RequestParam("email") String email
    ) {
        EmailCheckResponse result = emailCheckService.checkEmailAvailability(email);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}