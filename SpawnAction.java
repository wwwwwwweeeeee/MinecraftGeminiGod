package com.geminigod.actions.impl;

import com.geminigod.actions.IAction;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;

import java.util.Optional;
import java.util.Random;

public class SpawnAction implements IAction {
    private final Random random = new Random();

    @Override
    public String getName() {
        return "SPAWN";
    }

    @Override
    public void execute(ServerPlayer player, JsonObject payload) {
        String entityId = payload.get("entity_id").getAsString();
        int count = payload.get("amount").getAsInt();
        int radius = payload.has("radius") ? payload.get("radius").getAsInt() : 5;

        Optional<EntityType<?>> entityTypeOptional = BuiltInRegistries.ENTITY_TYPE.getOptional(new ResourceLocation(entityId));
        if (entityTypeOptional.isPresent()) {
            player.getServer().getPlayerList().broadcastSystemMessage(Component.literal("[GEMINI] I shall bring new life into this world... or perhaps death.").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), false);
            for (int i = 0; i < count; i++) {
                spawnEntity(player, entityTypeOptional.get(), radius);
            }
        } else {
            player.sendSystemMessage(Component.literal("Gemini tried to summon a creature that doesn't exist: " + entityId).withStyle(ChatFormatting.RED));
        }
    }

    private void spawnEntity(ServerPlayer player, EntityType<?> entityType, int radius) {
        ServerLevel world = player.serverLevel();
        int x = player.blockPosition().getX() + random.nextInt(radius * 2 + 1) - radius;
        int z = player.blockPosition().getZ() + random.nextInt(radius * 2 + 1) - radius;
        int y = world.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, x, z);
        entityType.spawn(world, new BlockPos(x, y, z), MobSpawnType.EVENT);
    }
}