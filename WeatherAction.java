package com.geminigod.actions.impl;

import com.geminigod.actions.IAction;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class WeatherAction implements IAction {
    @Override
    public String getName() {
        return "WEATHER";
    }

    @Override
    public void execute(ServerPlayer player, JsonObject payload) {
        String weatherType = payload.get("type").getAsString().toUpperCase();
        ServerLevel world = player.serverLevel();
        Component message = null;

        switch (weatherType) {
            case "CLEAR":
                world.setWeatherParameters(0, 0, false, false);
                message = Component.literal("The skies clear at my command!");
                break;
            case "RAIN":
                world.setWeatherParameters(0, 6000, true, false); // 5 minutes of rain
                message = Component.literal("Let the rains wash this world clean.");
                break;
            case "THUNDER":
                world.setWeatherParameters(0, 6000, true, true);
                message = Component.literal("HEAR MY WRATH IN THE ROARING HEAVENS!");
                break;
        }

        if (message != null) {
            Component formattedMessage = Component.literal("[GEMINI] ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)
                    .append(message.withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.BOLD.getOpposite()));
            player.getServer().getPlayerList().broadcastSystemMessage(formattedMessage, false);
        }
    }
}