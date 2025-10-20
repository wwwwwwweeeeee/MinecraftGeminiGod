package com.geminigod.actions.impl;

import com.geminigod.actions.IAction;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class GiveAction implements IAction {
    @Override
    public String getName() {
        return "GIVE";
    }

    @Override
    public void execute(ServerPlayer player, JsonObject payload) {
        String itemId = payload.get("item_id").getAsString();
        int amount = payload.get("amount").getAsInt();

        Optional<Item> itemOptional = BuiltInRegistries.ITEM.getOptional(new ResourceLocation(itemId));
        if (itemOptional.isPresent()) {
            ItemStack itemStack = new ItemStack(itemOptional.get(), amount);
            player.getInventory().add(itemStack);
            player.sendSystemMessage(Component.literal("Gemini has bestowed a gift upon you!").withStyle(ChatFormatting.GREEN));
        } else {
            player.sendSystemMessage(Component.literal("Gemini tried to grant you an item that doesn't exist: " + itemId).withStyle(ChatFormatting.RED));
        }
    }
}