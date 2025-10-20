package com.geminigod.actions;

import com.geminigod.GeminiGod;
import com.geminigod.actions.impl.*;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public class ActionManager {
    private final Map<String, IAction> actions = new HashMap<>();

    public void registerActions() {
        // Register all implemented actions here
        register(new SayAction());
        register(new DoNothingAction());
        register(new GiveAction());
        register(new SpawnAction());
        register(new EffectAction());
        register(new WeatherAction());
        register(new BuildSchematicAction());
        register(new ExplosionAction());
        register(new TeleportAction());
        register(new GrowGroveAction());
    }

    private void register(IAction action) {
        if (action == null || action.getName() == null) {
            GeminiGod.LOGGER.warn("Attempted to register a null or unnamed action — skipping.");
            return;
        }
        actions.put(action.getName().toUpperCase(), action);
        GeminiGod.LOGGER.debug("Registered Gemini action: {}", action.getName());
    }

    public void executeAction(ServerPlayer player, JsonObject actionJson) {
        if (player == null || actionJson == null) {
            GeminiGod.LOGGER.warn("Attempted to execute action with null player or payload.");
            return;
        }

        try {
            String actionName = actionJson.has("action") ? actionJson.get("action").getAsString().toUpperCase() : "";
            JsonObject payload = actionJson.has("payload") ? actionJson.getAsJsonObject("payload") : new JsonObject();

            IAction action = actions.get(actionName);
            if (action != null) {
                action.execute(player, payload);
            } else {
                GeminiGod.LOGGER.warn("Gemini tried to perform an unknown action: {}", actionName);
                player.sendSystemMessage(Component.literal("Gemini's intentions were unclear, and its power dissipated harmlessly.")
                        .withStyle(ChatFormatting.YELLOW));
            }
        } catch (Exception e) {
            GeminiGod.LOGGER.error("Error executing Gemini action with payload: {}", actionJson, e);
            player.sendSystemMessage(Component.literal("Gemini's power surged uncontrollably and failed. A divine error occurred.")
                    .withStyle(ChatFormatting.RED));
        }
    }
}
