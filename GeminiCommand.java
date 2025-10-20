package com.geminigod.commands;

import com.geminigod.GeminiGod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class GeminiCommand {
    public GeminiCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gemini")
            .then(Commands.argument("message", StringArgumentType.greedyString())
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                String message = StringArgumentType.getString(context, "message");

                player.sendSystemMessage(Component.literal("You ponder and send a message to the heavens...").withStyle(ChatFormatting.GRAY));
                GeminiGod.getInstance().getApiManager().callGeminiApi(player, message);

                return 1;
            }))
            .executes(context -> {
                context.getSource().sendFailure(Component.literal("You must send a message to Gemini. Usage: /gemini <message>"));
                return 0;
            })
        );
    }
}