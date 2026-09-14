package com.todo.rqc.services;

import com.todo.common.events.SendOtpEmailEvent;
import com.todo.common.messaging.NotificationRouting;
import com.todo.rqc.dtos.SendCodeResponse;
import com.todo.rqc.exceptions.EmailAlreadyRegisteredException;
import com.todo.rqc.repositories.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class OtpService {

    private static final String REDIS_KEY_PREFIX = "otp:";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final long ttlMinutes;

    public OtpService(UserRepository userRepository,
                       StringRedisTemplate redisTemplate,
                       RabbitTemplate rabbitTemplate,
                       @Value("${app.otp.ttl-minutes}") long ttlMinutes) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.ttlMinutes = ttlMinutes;
    }

    @Transactional(readOnly = true)
    public SendCodeResponse sendCode(String rawEmail) {
        String email = rawEmail.trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }

        String code = generateCode();
        Duration ttl = Duration.ofMinutes(ttlMinutes);
        redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + email, code, ttl);

        rabbitTemplate.convertAndSend(
                NotificationRouting.EXCHANGE,
                NotificationRouting.OTP_ROUTING_KEY,
                new SendOtpEmailEvent(email, code)
        );

        return SendCodeResponse.of(email, ttl.toSeconds());
    }

    private String generateCode() {
        int code = RANDOM.nextInt(10_000);
        return String.format("%04d", code);
    }
}
