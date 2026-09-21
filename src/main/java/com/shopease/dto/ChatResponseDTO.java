package com.shopease.dto;

import java.io.Serializable;

/**
 * Chatbot response payload DTO.
 */
public class ChatResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String reply;

    public ChatResponseDTO() {
    }

    public ChatResponseDTO(String reply) {
        this.reply = reply;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
