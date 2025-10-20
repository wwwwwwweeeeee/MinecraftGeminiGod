package com.geminigod.actions.impl;

import com.geminigod.actions.IAction;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Optional;

public class EffectAction implements IAction {
    @Override
    public String getName() {
        return "EFFECT";
    }

    @Override
    public void execute(ServerPlayer player, JsonObject payload) {
        String effectName = payload.get("effect").getAsString();
        int duration = payload.get("duration_seconds").getAsInt() * 20; // to ticks
        int amplifier = payload.get("amplifier").getAsInt();

        Optional<MobEffect> effectOptional = BuiltInRegistries.MOB_EFFECT.getOptional(new ResourceLocation(effectName.toLowerCase()));
        if (effectOptional.isPresent()) {
            player.addEffect(new MobEffectInstance(effectOptional.get(), duration, amplifier));
            player.sendSystemMessage(Component.literal("You feel a divine power course through you!").withStyle(ChatFormatting.GREEN));
        } else {
            player.sendSystemMessage(Component.literal("Gemini tried to grant you a non-existent effect: " + effectName).withStyle(ChatFormatting.RED));
        }
    }
}