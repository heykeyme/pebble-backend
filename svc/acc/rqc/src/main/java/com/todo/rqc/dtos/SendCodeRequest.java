package com.todo.rqc.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendCodeRequest(
        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        String email
) {
}
