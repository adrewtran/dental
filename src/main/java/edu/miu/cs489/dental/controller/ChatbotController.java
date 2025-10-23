package edu.miu.cs489.dental.controller;

import edu.miu.cs489.dental.dto.ChatRequestDto;
import edu.miu.cs489.dental.dto.ChatResponseDto;
import edu.miu.cs489.dental.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/adsweb/api/v1/chatbot")
@CrossOrigin(origins = "*")
@Tag(name = "Chatbot", description = "AI-powered chatbot endpoints for dental assistance")
@SecurityRequirement(name = "bearerAuth")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @Operation(summary = "Send message to chatbot", description = "Send a message to the AI-powered dental chatbot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully received chatbot response",
                    content = @Content(schema = @Schema(implementation = ChatResponseDto.class)))
    })
    @PostMapping("/message")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_OFFICE_MANAGER')")
    public ResponseEntity<ChatResponseDto> sendMessage(@RequestBody ChatRequestDto request) {
        ChatResponseDto response = chatbotService.processMessage(request.getMessage());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get chatbot help", description = "Get help information from the chatbot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully received help information",
                    content = @Content(schema = @Schema(implementation = ChatResponseDto.class)))
    })
    @GetMapping("/help")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_OFFICE_MANAGER')")
    public ResponseEntity<ChatResponseDto> getHelp() {
        ChatResponseDto response = chatbotService.processMessage("help");
        return ResponseEntity.ok(response);
    }
}

