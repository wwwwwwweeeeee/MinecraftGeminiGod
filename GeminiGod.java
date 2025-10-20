package com.geminigod;

import com.geminigod.actions.ActionManager;
import com.geminigod.api.GeminiApiManager;
import com.geminigod.commands.GeminiCommand;
import com.geminigod.config.Config;
import com.geminigod.util.DynamicRegistryScanner;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.MinecraftForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

import java.util.List;
import java.util.Random;

@Mod(GeminiGod.MOD_ID)
public class GeminiGod {
    public static final String MOD_ID = "geminigod";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static boolean isWorldEditAvailable = false;

    private static GeminiGod instance;

    private final DynamicRegistryScanner registryScanner = new DynamicRegistryScanner();
    private final ActionManager actionManager = new ActionManager();
    private final GeminiApiManager apiManager = new GeminiApiManager(registryScanner, actionManager);
    private final Random random = new Random();
    private int randomEventTicker = 0;

    public GeminiGod() {
        instance = this;

        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SPEC, "geminigod-server.toml");
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("GeminiGod is preparing to watch the world.");

        try {
            Class.forName("com.sk89q.worldedit.neoforge.NeoForgeAdapter");
            isWorldEditAvailable = true;
            LOGGER.info("Successfully hooked into WorldEdit (NeoForge). Building actions are enabled.");
        } catch (ClassNotFoundException e) {
            LOGGER.warn("WorldEdit not found. The BUILD_SCHEMATIC action will be disabled.");
        }

        actionManager.registerActions();
    }

    public static GeminiGod getInstance() {
        return instance;
    }

    public GeminiApiManager getApiManager() {
        return apiManager;
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.GAME)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            new GeminiCommand(event.getDispatcher());
        }

        @SubscribeEvent
        public static void onServerStarting(ServerStartingEvent event) {
            GeminiGod.getInstance().registryScanner.scanRegistries();
        }

        @SubscribeEvent
        public static void onServerTick(ServerTickEvent.Post event) {
            GeminiGod.getInstance().handleRandomEvents(event);
        }
    }

    private void handleRandomEvents(ServerTickEvent.Post event) {
        if (Config.RANDOM_EVENT_INTERVAL.get() <= 0) return;

        randomEventTicker++;
        long intervalTicks = Config.RANDOM_EVENT_INTERVAL.get() * 60 * 20L;

        if (randomEventTicker >= intervalTicks) {
            randomEventTicker = 0;
            List<ServerPlayer> players = event.getServer().getPlayerList().getPlayers();

            if (!players.isEmpty()) {
                ServerPlayer target = players.get(random.nextInt(players.size()));
                LOGGER.info("Gemini is planning a random event for {}", target.getName().getString());

                String randomPrompt = "You feel a surge of divine power. You look down upon the world and notice the mortal " +
                        target.getName().getString() + ". You decide to do something interesting, unexpected, and fitting for this strange world. What will you do?";

                apiManager.callGeminiApi(target, randomPrompt);
            }
        }
    }
}
