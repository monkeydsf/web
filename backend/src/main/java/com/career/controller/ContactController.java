package com.career.controller;

import com.career.dto.ApiResponse;
import com.career.dto.ContactMessageDto;
import com.career.dto.ContactMessageRequest;
import com.career.model.ContactMessage;
import com.career.repository.ContactMessageRepository;
import com.career.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    private final ContactMessageRepository contactMessageRepository;
    private final AuthService authService;

    public ContactController(ContactMessageRepository contactMessageRepository, AuthService authService) {
        this.contactMessageRepository = contactMessageRepository;
        this.authService = authService;
    }

    @PostMapping
    public ApiResponse submit(@Valid @RequestBody ContactMessageRequest request) {
        ContactMessage message = new ContactMessage();
        message.setName(request.name().trim());
        message.setEmail(request.email().trim());
        message.setSubject(request.subject().trim());
        message.setMessage(request.message().trim());
        contactMessageRepository.save(message);
        return new ApiResponse("消息已提交");
    }

    @GetMapping
    public List<ContactMessageDto> list(@RequestHeader("X-Token") String token) {
        authService.requireAdmin(token);
        return contactMessageRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ContactMessageDto::from)
                .toList();
    }
}
