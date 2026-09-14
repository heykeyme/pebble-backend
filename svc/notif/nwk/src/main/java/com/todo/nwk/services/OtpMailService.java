package com.todo.nwk.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class OtpMailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public OtpMailService(JavaMailSender mailSender,
                           @Value("${spring.mail.username}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendOtpEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Your verification code");
        message.setText("Your verification code is " + code + ". It expires in 5 minutes.");
        mailSender.send(message);
    }
}
