package com.itech.itech_backend.service;

import com.itech.itech_backend.dto.ContactMessageDto;
import com.itech.itech_backend.model.ContactMessage;
import com.itech.itech_backend.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactMessageService {

    private final ContactMessageRepository contactRepo;

    public ContactMessage saveMessage(ContactMessageDto dto) {
        return contactRepo.save(ContactMessage.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .message(dto.getMessage())
                .build());
    }
}
