package com.todo.common.messaging;

// Shared RabbitMQ topology names for the notification flow (rqc publishes, nwk consumes).
public final class NotificationRouting {

    public static final String EXCHANGE = "notification.exchange";
    public static final String OTP_ROUTING_KEY = "notification.email.otp";
    public static final String OTP_QUEUE = "notification.email.otp.queue";

    private NotificationRouting() {}
}
