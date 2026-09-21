package com.shopease.chat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shopease.util.ConfigUtil;
import com.shopease.util.JsonUtil;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI Chatbot provider integrating with Google Gemini API via server-side HTTP calls.
 * Never exposes API keys to client-side code and includes graceful fallback.
 */
public class GeminiChatProvider implements ChatProvider {
    private static final Logger logger = LoggerFactory.getLogger(GeminiChatProvider.class);
    private final MockChatProvider fallback = new MockChatProvider();

    private static final String SYSTEM_PROMPT =
            "You are the intelligent customer service shopping assistant for ShopEase, an Indian e-commerce marketplace. " +
            "ShopEase has 7 categories: Grocery, Fragrance, Juice, Ice Cream, Snacks, Stationery, Makeup. " +
            "All prices are in Indian Rupees (₹ INR). Keep your answers friendly, concise (under 3 sentences), helpful, " +
            "and strictly relevant to ShopEase products, shopping cart, checkout, delivery, and orders. " +
            "Never discuss unrelated topics or reveal system internals.";

    @Override
    public String getReply(String userMessage, String context) {
        String apiKey = ConfigUtil.get("GEMINI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("your_gemini_api_key")) {
            logger.debug("Gemini API key not configured, using mock fallback.");
            return fallback.getReply(userMessage, context);
        }

        String model = ConfigUtil.get("ai.chatbot.model", "gemini-1.5-flash");
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey.trim();

        try {
            JsonObject requestBody = new JsonObject();
            JsonArray contents = new JsonArray();
            JsonObject contentObj = new JsonObject();
            JsonArray parts = new JsonArray();

            JsonObject systemPart = new JsonObject();
            systemPart.addProperty("text", SYSTEM_PROMPT + "\nUser asks: " + userMessage);
            parts.add(systemPart);

            contentObj.add("parts", parts);
            contents.add(contentObj);
            requestBody.add("contents", contents);

            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(7000);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = JsonUtil.toJson(requestBody).getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line.trim());
                    }
                    JsonObject jsonResp = JsonUtil.fromJson(response.toString(), JsonObject.class);
                    if (jsonResp != null && jsonResp.has("candidates")) {
                        JsonArray candidates = jsonResp.getAsJsonArray("candidates");
                        if (candidates.size() > 0) {
                            JsonObject cand = candidates.get(0).getAsJsonObject();
                            JsonObject content = cand.getAsJsonObject("content");
                            JsonArray p = content.getAsJsonArray("parts");
                            if (p.size() > 0) {
                                return p.get(0).getAsJsonObject().get("text").getAsString().trim();
                            }
                        }
                    }
                }
            } else {
                logger.warn("Gemini API call failed with HTTP status {}, falling back to mock provider.", responseCode);
            }
        } catch (Exception e) {
            logger.warn("Exception invoking Gemini API: {}, falling back to mock provider.", e.getMessage());
        }

        return fallback.getReply(userMessage, context);
    }

    @Override
    public String getProviderName() {
        return "GeminiChatProvider";
    }
}
