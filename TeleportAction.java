package com.geminigod.actions.impl;

import com.geminigod.actions.IAction;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Random;

public class TeleportAction implements IAction {
    private final Random random = new Random();

    @Override
    public String getName() {
        return "TELEPORT";
    }

    @Override
    public void execute(ServerPlayer player, JsonObject payload) {
        int radius = payload.has("radius") ? payload.get("radius").getAsInt() : 100;

        for (int i = 0; i < 10; i++) { // Try 10 times to find a safe spot
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = radius * Math.sqrt(random.nextDouble());
            double x = player.getX() + distance * Math.cos(angle);
            double z = player.getZ() + distance * Math.sin(angle);

            ServerLevel world = player.serverLevel();
            int y = world.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z);

            BlockPos landingPos = new BlockPos((int) x, y, (int) z);
            BlockState groundState = world.getBlockState(landingPos.below());

            if (!groundState.is(BlockTags.FIRE) && !groundState.liquid()) {
                player.teleportTo(world, x, y, z, player.getYRot(), player.getXRot());
                return;
            }
        }
    }
}