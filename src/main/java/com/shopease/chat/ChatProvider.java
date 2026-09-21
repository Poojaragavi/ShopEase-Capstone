package com.shopease.chat;

/**
 * Strategy Pattern interface for AI Chatbot providers.
 */
public interface ChatProvider {
    /**
     * Generates a conversational reply to a user inquiry.
     *
     * @param userMessage the user's input query
     * @param context additional domain context
     * @return AI assistant reply string
     */
    String getReply(String userMessage, String context);

    /**
     * Returns the name of this provider (e.g. 'MockChatProvider', 'GeminiChatProvider').
     */
    String getProviderName();
}
