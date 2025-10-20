package com.geminigod.api;

import com.geminigod.util.DynamicRegistryScanner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.stream.Collectors;

public final class JsonRequestHelper {
    private static final Gson GSON = new GsonBuilder().create();

    public static String createGeminiRequestBody(String personaPromptTemplate, ServerPlayer player, String message, DynamicRegistryScanner scanner, List<? extends String> availableSchematics) {
        String populatedPrompt = populatePrompt(personaPromptTemplate, player, scanner, availableSchematics);

        JsonObject root = new JsonObject();
        JsonArray contents = new JsonArray();
        
        // The system prompt and user message are combined into a single user turn for compatibility
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        JsonArray parts = new JsonArray();
        JsonObject systemPart = new JsonObject();
        systemPart.addProperty("text", populatedPrompt);
        parts.add(systemPart);
        JsonObject userPart = new JsonObject();
        userPart.addProperty("text", "\n\nHere is the mortal's message to me:\n" + message);
        parts.add(userPart);
        
        userMessage.add("parts", parts);
        contents.add(userMessage);
        
        root.add("contents", contents);

        // Generation settings
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 1.0);
        generationConfig.addProperty("topP", 0.95);
        generationConfig.addProperty("maxOutputTokens", 2048);
        root.add("generationConfig", generationConfig);

        return GSON.toJson(root);
    }

    private static String populatePrompt(String template, ServerPlayer player, DynamicRegistryScanner scanner, List<? extends String> schematics) {
        String playerLocation = String.format("world '%s' at x:%.0f, y:%.0f, z:%.0f",
                player.level().dimension().location(), player.getX(), player.getY(), player.getZ());

        long worldTime = player.level().getDayTime();
        long hours = (worldTime / 1000 + 6) % 24;
        long minutes = (worldTime % 1000) * 60 / 1000;
        String formattedTime = String.format("%02d:%02d", hours, minutes);

        String onlinePlayers = player.getServer().getPlayerList().getPlayers().stream()
                .map(p -> p.getName().getString())
                .collect(Collectors.joining(", "));

        String itemSamples = String.join(", ", scanner.getItemSamples(20));
        String entitySamples = String.join(", ", scanner.getEntitySamples(15));
        String biomeSamples = String.join(", ", scanner.getBiomeSamples(10));
        String schematicList = String.join(", ", schematics);

        return template
                .replace("{playerName}", player.getName().getString())
                .replace("{playerLocation}", playerLocation)
                .replace("{worldTime}", formattedTime)
                .replace("{onlinePlayers}", onlinePlayers.isEmpty() ? "none" : onlinePlayers)
                .replace("{itemSamples}", itemSamples.isEmpty() ? "N/A" : itemSamples)
                .replace("{entitySamples}", entitySamples.isEmpty() ? "N/A" : entitySamples)
                .replace("{biomeSamples}", biomeSamples.isEmpty() ? "N/A" : biomeSamples)
                .replace("{schematicList}", schematicList.isEmpty() ? "none" : schematicList);
    }
}