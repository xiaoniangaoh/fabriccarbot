package com.example.fabriccarbot;

import carpet.CarpetServer;
import carpet.CarpetExtension;
import com.example.fabriccarbot.command.CarbotCommand;
import com.example.fabriccarbot.data.BotManager;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricCarbot implements ModInitializer, CarpetExtension {
    public static final String MOD_ID = "fabriccarbot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static BotManager botManager;

    @Override
    public void onInitialize() {
        botManager = new BotManager();
        botManager.load();
        CarpetServer.manageExtension(this);
        LOGGER.info("FabricCarbot initialized");
    }

    @Override
    public void onServerLoaded(MinecraftServer server) {
        // Server loaded, data packs ready
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        CarbotCommand.register(dispatcher, buildContext);
    }

    @Override
    public String version() {
        return "1.0.0";
    }
}