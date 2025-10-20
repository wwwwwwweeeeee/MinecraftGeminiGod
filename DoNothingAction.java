package com.geminigod.actions.impl;

import com.geminigod.actions.IAction;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class DoNothingAction implements IAction {
    @Override
    public String getName() {
        return "DO_NOTHING";
    }

    @Override
    public void execute(ServerPlayer player, JsonObject payload) {
        String reason = payload.get("reason").getAsString().replace("{playerName}", player.getName().getString());
        Component formattedMessage = Component.literal("[GEMINI whispers to you] ").withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(Component.literal(reason).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
        player.sendSystemMessage(formattedMessage);
    }
}