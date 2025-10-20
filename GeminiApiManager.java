package com.geminigod.api;

import com.geminigod.GeminiGod;
import com.geminigod.actions.ActionManager;
import com.geminigod.config.Config;
import com.geminigod.util.DynamicRegistryScanner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class GeminiApiManager {
    private static final Logger LOGGER = GeminiGod.LOGGER;
    private final DynamicRegistryScanner registryScanner;
    private final ActionManager actionManager;

    public GeminiApiManager(DynamicRegistryScanner registryScanner, ActionManager actionManager) {
        this.registryScanner = registryScanner;
        this.actionManager = actionManager;
    }

    public void callGeminiApi(ServerPlayer player, String message) {
        String apiKey = Config.API_KEY.get();
        if (apiKey.isEmpty() || "AIzaSyAJzycTE8pxlqFOyjN-4pYUjDhjdktGsQc".equals(apiKey)) {
            player.sendSystemMessage(Component.literal("GeminiGod API key is not configured on the server.").withStyle(ChatFormatting.RED));
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL("[https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=](https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=)" + apiKey);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String requestBody = JsonRequestHelper.createGeminiRequestBody(Config.PERSONA_PROMPT.get(), player, message, registryScanner, Config.AVAILABLE_SCHEMATICS.get());

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                        JsonObject responseJson = JsonParser.parseReader(reader).getAsJsonObject();
                        String geminiText = responseJson.getAsJsonArray("candidates")
                                .get(0).getAsJsonObject()
                                .getAsJsonObject("content")
                                .getAsJsonArray("parts")
                                .get(0).getAsJsonObject()
                                .get("text").getAsString();
                        
                        player.getServer().execute(() -> processApiResponse(player, geminiText));
                    }
                } else {
                    try (InputStreamReader errorReader = new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8)) {
                        String error = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                        LOGGER.warn("Gemini API call failed. Response Code: {} | Response: {}", responseCode, error);
                        player.getServer().execute(() -> player.sendSystemMessage(Component.literal("The heavens are silent. Your message was lost to the void.").withStyle(ChatFormatting.RED)));
                    }
                }
            } catch (Exception e) {
                LOGGER.error("An error occurred while calling the Gemini API.", e);
                player.getServer().execute(() -> player.sendSystemMessage(Component.literal("A cosmic interference prevented your message from being heard.").withStyle(ChatFormatting.RED)));
            }
        });
    }

    private void processApiResponse(ServerPlayer player, String rawResponse) {
        try {
            String cleanResponse = rawResponse.trim().replace("```json", "").replace("```", "");
            JsonObject actionJson = JsonParser.parseString(cleanResponse).getAsJsonObject();
            actionManager.executeAction(player, actionJson);
        } catch (JsonSyntaxException | IllegalStateException | NullPointerException e) {
            LOGGER.warn("Failed to parse Gemini's JSON response: {}", rawResponse, e);
            Component feedback = Component.literal("[GEMINI's chaotic thoughts]: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(rawResponse).withStyle(ChatFormatting.GRAY));
            player.sendSystemMessage(feedback);
        }
    }
}