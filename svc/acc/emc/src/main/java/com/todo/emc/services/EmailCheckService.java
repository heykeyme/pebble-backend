package com.todo.emc.services;

import com.todo.emc.dtos.EmailCheckResponse;
import com.todo.emc.repositories.EmailCheckerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailCheckService {

    private final EmailCheckerRepository emailCheckerRepository;

    public EmailCheckService(EmailCheckerRepository emailCheckerRepository) {
        this.emailCheckerRepository = emailCheckerRepository;
    }

    @Transactional(readOnly = true)
    public EmailCheckResponse checkEmailAvailability(String email) {
        String sanitizedEmail = (email != null) ? email.trim().toLowerCase() : "";
        boolean exists = emailCheckerRepository.existsByEmailIgnoreCase(sanitizedEmail);
        return EmailCheckResponse.of(sanitizedEmail, exists);
    }
}