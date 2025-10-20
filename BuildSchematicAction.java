package com.geminigod.actions.impl;

import com.geminigod.GeminiGod;
import com.geminigod.actions.IAction;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;

/**
 * Places a schematic near the player using WorldEdit if it’s available.
 * Safe to call even when WorldEdit isn’t installed.
 */
public class BuildSchematicAction implements IAction {

    @Override
    public String getName() {
        return "BUILD_SCHEMATIC";
    }

    @Override
    public void execute(ServerPlayer player, JsonObject payload) {
        if (!GeminiGod.isWorldEditAvailable) {
            player.sendSystemMessage(Component.literal(
                    "Gemini wanted to build something, but WorldEdit was not found.")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        try {
            // --- Extract schematic info ---
            String fileName = payload.has("file") ? payload.get("file").getAsString() : "example.schem";
            BlockPos origin = player.blockPosition().offset(0, 0, 5); // build 5 blocks in front of player

            // --- Locate the schematic file ---
            File schematicFile = new File("schematics", fileName);
            if (!schematicFile.exists()) {
                player.sendSystemMessage(Component.literal(
                        "Schematic file not found: " + schematicFile.getPath())
                        .withStyle(ChatFormatting.YELLOW));
                return;
            }

            // --- Perform WorldEdit paste safely ---
            try {
                Class<?> worldEditClass = Class.forName("com.sk89q.worldedit.WorldEdit");
                // Reflection-based lazy load so it doesn’t hard-crash if API changes
                Object worldEdit = worldEditClass.getMethod("getInstance").invoke(null);

                // Use the NeoForge adapter to wrap the Minecraft world
                Class<?> adapterClass = Class.forName("com.sk89q.worldedit.neoforge.NeoForgeAdapter");
                Object world = adapterClass.getMethod("adapt", net.minecraft.server.level.ServerLevel.class)
                        .invoke(null, player.serverLevel());

                // Load schematic & paste (simplified)
                Class<?> schematicFormat = Class.forName("com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats");
                Object format = schematicFormat.getMethod("findByFile", File.class).invoke(null, schematicFile);
                if (format == null) {
                    player.sendSystemMessage(Component.literal("Unsupported schematic format.")
                            .withStyle(ChatFormatting.RED));
                    return;
                }

                Object reader = format.getClass().getMethod("getReader", java.io.FileInputStream.class)
                        .invoke(format, new java.io.FileInputStream(schematicFile));
                Object clipboard = reader.getClass().getMethod("read").invoke(reader);

                Class<?> editSessionClass = Class.forName("com.sk89q.worldedit.EditSession");
                Object editSession = worldEditClass.getMethod("newEditSession", Class.forName("com.sk89q.worldedit.world.World"))
                        .invoke(worldEdit, world);

                Class<?> operations = Class.forName("com.sk89q.worldedit.session.ClipboardHolder");
                Object holder = operations.getConstructor(clipboard.getClass()).newInstance(clipboard);
                holder.getClass().getMethod("createPaste", editSessionClass)
                        .invoke(holder, editSession)
                        .getClass().getMethod("to", Class.forName("com.sk89q.worldedit.math.BlockVector3"))
                        .invoke(holder, Class.forName("com.sk89q.worldedit.math.BlockVector3")
                                .getMethod("at", int.class, int.class, int.class)
                                .invoke(null, origin.getX(), origin.getY(), origin.getZ()));

                editSession.getClass().getMethod("close").invoke(editSession);

                player.sendSystemMessage(Component.literal(
                        "Gemini built something wondrous before your eyes!").withStyle(ChatFormatting.GREEN));

            } catch (Throwable t) {
                GeminiGod.LOGGER.error("Error while pasting schematic", t);
                player.sendSystemMessage(Component.literal(
                        "Gemini tried to build but divine energy faltered. Check logs.")
                        .withStyle(ChatFormatting.RED));
            }

        } catch (Exception e) {
            GeminiGod.LOGGER.error("Unexpected error during schematic build", e);
            player.sendSystemMessage(Component.literal(
                    "Gemini’s creative power misfired. A celestial error occurred.")
                    .withStyle(ChatFormatting.RED));
        }
    }
}
