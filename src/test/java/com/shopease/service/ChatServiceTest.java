package com.shopease.service;

import com.shopease.chat.MockChatProvider;
import com.shopease.dto.ChatResponseDTO;
import com.shopease.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChatServiceTest {

    private ChatService chatService;

    @BeforeEach
    public void setUp() {
        chatService = new ChatService(new MockChatProvider());
    }

    @Test
    public void testGroceryQuestionAnswer() {
        ChatResponseDTO resp = chatService.processChat("session-1", "Do you sell grocery items?");
        assertNotNull(resp);
        assertNotNull(resp.getReply());
        assertTrue(resp.getReply().toLowerCase().contains("grocery") || resp.getReply().toLowerCase().contains("basmati"));
    }

    @Test
    public void testEmptyMessageThrowsValidation() {
        assertThrows(ValidationException.class, () ->
                chatService.processChat("session-1", "   "));
    }

    @Test
    public void testRateLimiting() {
        String session = "rate-limit-session";
        // 10 calls should succeed
        for (int i = 0; i < 10; i++) {
            assertNotNull(chatService.processChat(session, "Hello " + i));
        }
        // 11th call should throw ValidationException
        assertThrows(ValidationException.class, () ->
                chatService.processChat(session, "Hello 11"));
    }
}
