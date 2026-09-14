package com.todo.rqc.controllers;

import com.todo.common.dtos.ApiResponse;
import com.todo.rqc.dtos.SendCodeRequest;
import com.todo.rqc.dtos.SendCodeResponse;
import com.todo.rqc.services.OtpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class SendCodeController {

    private final OtpService otpService;

    public SendCodeController(OtpService otpService) {
        this.otpService = otpService;
    }

    /**
     * POST /api/v1/auth/send-code
     * Generates an OTP for an email not yet registered, caches it in Redis (TTL 5 min),
     * and publishes a SendOtpEmailEvent for the notification worker to deliver.
     */
    @PostMapping("/send-code")
    public ResponseEntity<ApiResponse<SendCodeResponse>> sendCode(@Valid @RequestBody SendCodeRequest request) {
        SendCodeResponse result = otpService.sendCode(request.email());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
