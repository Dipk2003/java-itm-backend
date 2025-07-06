package com.itech.itech_backend.controller;

import com.itech.itech_backend.dto.ContactMessageDto;
import com.itech.itech_backend.model.ContactMessage;
import com.itech.itech_backend.service.ContactMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contact")
@RequiredArgsConstructor
@CrossOrigin
public class ContactMessageController {

    private final ContactMessageService contactService;

    @PostMapping
    public ContactMessage saveMessage(@RequestBody ContactMessageDto dto) {
        return contactService.saveMessage(dto);
    }
}
