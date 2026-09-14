package com.todo.nwk.listeners;

import com.todo.common.events.SendOtpEmailEvent;
import com.todo.common.messaging.NotificationRouting;
import com.todo.nwk.services.OtpMailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OtpEmailListener {

    private final OtpMailService otpMailService;

    public OtpEmailListener(OtpMailService otpMailService) {
        this.otpMailService = otpMailService;
    }

    @RabbitListener(queues = NotificationRouting.OTP_QUEUE)
    public void handle(SendOtpEmailEvent event) {
        otpMailService.sendOtpEmail(event.email(), event.code());
    }
}
