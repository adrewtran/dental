package edu.miu.cs489.dental.controller;

import edu.miu.cs489.dental.dto.ChatRequestDto;
import edu.miu.cs489.dental.dto.ChatResponseDto;
import edu.miu.cs489.dental.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/adsweb/api/v1/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/message")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_OFFICE_MANAGER')")
    public ResponseEntity<ChatResponseDto> sendMessage(@RequestBody ChatRequestDto request) {
        ChatResponseDto response = chatbotService.processMessage(request.getMessage());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/help")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_OFFICE_MANAGER')")
    public ResponseEntity<ChatResponseDto> getHelp() {
        ChatResponseDto response = chatbotService.processMessage("help");
        return ResponseEntity.ok(response);
    }
}

