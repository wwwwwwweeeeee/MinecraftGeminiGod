package com.geminigod.actions.impl;

import com.geminigod.actions.IAction;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class ExplosionAction implements IAction {
    @Override
    public String getName() {
        return "EXPLOSION";
    }

    @Override
    public void execute(ServerPlayer player, JsonObject payload) {
        float strength = payload.has("strength") ? payload.get("strength").getAsFloat() : 2.0f;
        player.level().explode(null, player.getX(), player.getY(), player.getZ(), strength, Level.ExplosionInteraction.NONE);
    }
}