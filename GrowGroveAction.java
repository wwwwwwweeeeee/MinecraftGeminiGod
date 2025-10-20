package com.geminigod.actions.impl;

import com.geminigod.actions.IAction;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import java.util.List;
import java.util.Optional;

public class GrowGroveAction implements IAction {
    private static final List<ResourceKey<ConfiguredFeature<?, ?>>> MAGICAL_TREES = List.of(
            TreeFeatures.CHERRY,
            TreeFeatures.FANCY_OAK,
            TreeFeatures.AZALEA_TREE,
            TreeFeatures.MEGA_SPRUCE
    );

    @Override
    public String getName() {
        return "GROW_GROVE";
    }

    @Override
    public void execute(ServerPlayer player, JsonObject payload) {
        ServerLevel world = player.serverLevel();
        BlockPos center = player.blockPosition();
        RandomSource random = world.getRandom();

        for (int i = 0; i < random.nextInt(5) + 7; i++) {
            int x = center.getX() + random.nextInt(16) - 8;
            int z = center.getZ() + random.nextInt(16) - 8;
            int y = world.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, x, z);
            BlockPos spawnPos = new BlockPos(x, y, z);
            
            ResourceKey<ConfiguredFeature<?, ?>> treeKey = MAGICAL_TREES.get(random.nextInt(MAGICAL_TREES.size()));
            
            Optional<Holder.Reference<ConfiguredFeature<?, ?>>> feature = world.registryAccess()
                    .registryOrThrow(Registries.CONFIGURED_FEATURE)
                    .getHolder(treeKey);
                    
            feature.ifPresent(f -> f.value().place(world, world.getChunkSource().getGenerator(), random, spawnPos));
        }
    }
}