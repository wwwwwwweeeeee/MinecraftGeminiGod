package com.geminigod.actions.impl;

import com.geminigod.actions.IAction;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SayAction implements IAction {
    @Override
    public String getName() {
        return "SAY";
    }

    @Override
    public void execute(ServerPlayer player, JsonObject payload) {
        String message = payload.get("message").getAsString().replace("{playerName}", player.getName().getString());
        Component formattedMessage = Component.literal("[GEMINI] ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)
                .append(Component.literal(message).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.BOLD.getOpposite()));
        player.getServer().getPlayerList().broadcastSystemMessage(formattedMessage, false);
    }
}