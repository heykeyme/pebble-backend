package com.todo.rqc.dtos;

public record SendCodeResponse(String email, String message, long expiresInSeconds) {

    public static SendCodeResponse of(String email, long expiresInSeconds) {
        return new SendCodeResponse(email, "Verification code sent", expiresInSeconds);
    }
}
