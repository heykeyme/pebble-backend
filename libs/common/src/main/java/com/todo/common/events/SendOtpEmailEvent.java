package com.todo.common.events;

// Published by rqc to notification.exchange, consumed by nwk to send the OTP email.
public record SendOtpEmailEvent(String email, String code) {
}
