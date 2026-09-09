package com.todo.emc.dtos;

public record EmailCheckResponse(String email, boolean exists, String message) {
    
    public static EmailCheckResponse of(String email, boolean exists){
        String msg = exists ? "Email already registered" : "Email is available";
        return new EmailCheckResponse(email, exists, msg);
    }
}
