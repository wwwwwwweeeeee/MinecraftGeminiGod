package com.geminigod.actions;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;

public interface IAction {
    /**
     * Executes the defined action.
     * @param player The player who is the target or origin of the action.
     * @param payload The JSON object containing parameters for the action.
     */
    void execute(ServerPlayer player, JsonObject payload);

    /**
     * @return The unique name of the action (e.g., "GIVE", "SPAWN").
     */
    String getName();
}