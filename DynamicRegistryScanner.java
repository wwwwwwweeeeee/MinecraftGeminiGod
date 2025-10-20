package com.geminigod.util;

import com.geminigod.GeminiGod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DynamicRegistryScanner {

    private final List<String> itemKeys = new ArrayList<>();
    private final List<String> entityKeys = new ArrayList<>();
    private final List<String> biomeKeys = new ArrayList<>();
    private final Random random = new Random();

    public void scanRegistries() {
        GeminiGod.LOGGER.info("Scanning registries for modded content...");

        // Items
        for (Item item : BuiltInRegistries.ITEM) {
            itemKeys.add(BuiltInRegistries.ITEM.getKey(item).toString());
        }
        GeminiGod.LOGGER.info("Discovered {} items.", itemKeys.size());

        // Entities
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            if (entityType.canSummon()) {
                entityKeys.add(BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
            }
        }
        GeminiGod.LOGGER.info("Discovered {} summonable entities.", entityKeys.size());

        // Biomes
        for (Biome biome : BuiltInRegistries.BIOME) {
            ResourceLocation key = BuiltInRegistries.BIOME.getKey(biome);
            if (key != null) {
                biomeKeys.add(key.toString());
            }
        }
        GeminiGod.LOGGER.info("Discovered {} biomes.", biomeKeys.size());
        
        Collections.shuffle(itemKeys);
        Collections.shuffle(entityKeys);
        Collections.shuffle(biomeKeys);
    }

    private List<String> getRandomSamples(List<String> sourceList, int count) {
        if (sourceList.isEmpty() || count <= 0) {
            return Collections.emptyList();
        }
        List<String> samples = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (sourceList.isEmpty()) break;
            samples.add(sourceList.get(random.nextInt(sourceList.size())));
        }
        return samples;
    }

    public List<String> getItemSamples(int count) {
        return getRandomSamples(itemKeys, count);
    }

    public List<String> getEntitySamples(int count) {
        return getRandomSamples(entityKeys, count);
    }



    public List<String> getBiomeSamples(int count) {
        return getRandomSamples(biomeKeys, count);
    }
}