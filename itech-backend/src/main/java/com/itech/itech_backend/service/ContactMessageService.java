package com.itech.itech_backend.service;

import com.itech.itech_backend.model.ContactMessage;
import com.itech.itech_backend.repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactMessageService {

    @Autowired
    private ContactMessageRepository contactRepo;

    public void saveMessage(ContactMessage message) {
        contactRepo.save(message);
    }

    public List<ContactMessage> getAllMessages() {
        return contactRepo.findAll();
    }
}
