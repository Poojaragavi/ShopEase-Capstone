package com.shopease.controller;

import com.shopease.dto.ChatRequestDTO;
import com.shopease.dto.ChatResponseDTO;
import com.shopease.service.ChatService;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * REST API for AI Chatbot interactions.
 * Versioned Endpoint: POST /api/v1/chat
 */
@WebServlet(name = "ChatApiServlet", urlPatterns = "/api/v1/chat")
public class ChatApiServlet extends BaseServlet {
    private final ChatService chatService = new ChatService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(true);
        String sessionId = session.getId();

        try {
            ChatRequestDTO requestDTO = readJsonBody(req, ChatRequestDTO.class);
            if (requestDTO == null || requestDTO.getMessage() == null || requestDTO.getMessage().trim().isEmpty()) {
                sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_INPUT", "A message string is required.");
                return;
            }

            ChatResponseDTO responseDTO = chatService.processChat(sessionId, requestDTO.getMessage());
            sendSuccess(resp, responseDTO);
        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}
