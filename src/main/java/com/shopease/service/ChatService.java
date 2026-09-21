package com.shopease.service;

import com.shopease.chat.ChatProvider;
import com.shopease.chat.GeminiChatProvider;
import com.shopease.chat.MockChatProvider;
import com.shopease.dto.ChatResponseDTO;
import com.shopease.exception.ValidationException;
import com.shopease.util.ConfigUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service orchestrating AI Chatbot requests, session rate-limiting, and repeated question caching.
 */
public class ChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private static final int MAX_MESSAGE_LENGTH = 500;
    private static final int MAX_REQUESTS_PER_MINUTE = 10;

    private final ChatProvider chatProvider;
    // Cache: sessionId:query -> reply
    private final Map<String, String> queryCache = new ConcurrentHashMap<>();
    // Rate Limiter: sessionId -> RateTracker
    private final Map<String, RateTracker> rateLimiterMap = new ConcurrentHashMap<>();

    public ChatService() {
        String providerType = ConfigUtil.get("ai.chatbot.provider", "mock");
        if ("gemini".equalsIgnoreCase(providerType)) {
            this.chatProvider = new GeminiChatProvider();
        } else {
            this.chatProvider = new MockChatProvider();
        }
        logger.info("Initialized ChatService with provider: {}", chatProvider.getProviderName());
    }

    public ChatService(ChatProvider chatProvider) {
        this.chatProvider = chatProvider;
    }

    public ChatResponseDTO processChat(String sessionId, String userMessage) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = "anonymous-session";
        }

        if (userMessage == null || userMessage.trim().isEmpty()) {
            throw new ValidationException("Chat message cannot be empty.");
        }

        if (userMessage.length() > MAX_MESSAGE_LENGTH) {
            throw new ValidationException("Message exceeds maximum allowed length of " + MAX_MESSAGE_LENGTH + " characters.");
        }

        // 1. Session Rate Limiting Check
        checkRateLimit(sessionId);

        // 2. Query Cache Check
        String cacheKey = sessionId + ":" + userMessage.trim().toLowerCase();
        if (queryCache.containsKey(cacheKey)) {
            logger.debug("Serving cached chatbot reply for session {}", sessionId);
            return new ChatResponseDTO(queryCache.get(cacheKey));
        }

        // 3. Delegate to ChatProvider
        String reply;
        try {
            reply = chatProvider.getReply(userMessage.trim(), "ShopEase Marketplace");
            if (reply == null || reply.trim().isEmpty()) {
                reply = "I'm here to help with your ShopEase shopping questions. Please ask about products, orders, or categories!";
            }
        } catch (Exception e) {
            logger.error("Error generating chat reply from provider", e);
            reply = "I apologize, but I am having trouble answering right now. Please try again shortly or browse our product catalog!";
        }

        // 4. Store in cache
        queryCache.put(cacheKey, reply);
        return new ChatResponseDTO(reply);
    }

    private void checkRateLimit(String sessionId) {
        long now = System.currentTimeMillis();
        rateLimiterMap.compute(sessionId, (key, tracker) -> {
            if (tracker == null || now - tracker.windowStartTime > 60000) {
                return new RateTracker(now, 1);
            }
            if (tracker.requestCount >= MAX_REQUESTS_PER_MINUTE) {
                throw new ValidationException("Chat rate limit exceeded. Maximum " + MAX_REQUESTS_PER_MINUTE + " messages per minute allowed.");
            }
            tracker.requestCount++;
            return tracker;
        });
    }

    private static class RateTracker {
        long windowStartTime;
        int requestCount;

        RateTracker(long windowStartTime, int requestCount) {
            this.windowStartTime = windowStartTime;
            this.requestCount = requestCount;
        }
    }
}
