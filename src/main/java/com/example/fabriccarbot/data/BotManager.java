package com.example.fabriccarbot.data;

import com.example.fabriccarbot.FabricCarbot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Locale;

public class BotManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path CONFIG_DIR = Paths.get("config", "fabriccarbot");
    private final Path BOTS_FILE = CONFIG_DIR.resolve("bots.json");

    private final Map<String, BotRecord> botsByName = new HashMap<>();
    private final Map<String, List<BotRecord>> botsByAlias = new HashMap<>();

    public void load() {
        try {
            if (!Files.exists(CONFIG_DIR)) Files.createDirectories(CONFIG_DIR);
            if (!Files.exists(BOTS_FILE)) return;

            String json = Files.readString(BOTS_FILE);
            List<BotRecord> records = GSON.fromJson(json, new TypeToken<List<BotRecord>>() {}.getType());
            if (records == null) return;

            for (BotRecord record : records) {
                botsByName.put(record.name, record);
                if (record.aliases != null) {
                    for (String alias : record.aliases) {
                        botsByAlias.computeIfAbsent(alias.toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(record);
                    }
                }
            }
            FabricCarbot.LOGGER.info("Loaded {} bot records", botsByName.size());
        } catch (Exception e) {
            FabricCarbot.LOGGER.error("Failed to load bot data", e);
        }
    }

    public void save() {
        try {
            if (!Files.exists(CONFIG_DIR)) Files.createDirectories(CONFIG_DIR);
            List<BotRecord> records = new ArrayList<>(botsByName.values());
            String json = GSON.toJson(records);
            Files.writeString(BOTS_FILE, json);
        } catch (IOException e) {
            FabricCarbot.LOGGER.error("Failed to save bot data", e);
        }
    }

    public void addBot(String botName, String alias, String dimension,
                       double x, double y, double z, float yaw, float pitch) {
        BotRecord record = new BotRecord(
                botName,
                alias != null ? List.of(alias) : List.of(),
                dimension, x, y, z, yaw, pitch);
        botsByName.put(botName, record);
        if (alias != null) {
            botsByAlias.computeIfAbsent(alias.toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(record);
        }
        save();
    }

    public void removeBot(String alias) {
        List<BotRecord> records = botsByAlias.remove(alias.toLowerCase(Locale.ROOT));
        if (records != null) {
            for (BotRecord record : records) {
                botsByName.remove(record.name);
            }
            save();
        }
    }

    public void removeBotByName(String botName) {
        BotRecord record = botsByName.remove(botName);
        if (record != null && record.aliases != null) {
            for (String alias : record.aliases) {
                List<BotRecord> list = botsByAlias.get(alias.toLowerCase(Locale.ROOT));
                if (list != null) {
                    list.remove(record);
                    if (list.isEmpty()) botsByAlias.remove(alias.toLowerCase(Locale.ROOT));
                }
            }
            save();
        }
    }

    public List<BotRecord> getBotsByAlias(String alias) {
        return botsByAlias.getOrDefault(alias.toLowerCase(Locale.ROOT), Collections.emptyList());
    }

    public BotRecord getBotByName(String botName) {
        return botsByName.get(botName);
    }

    public Collection<BotRecord> getAllBots() {
        return new ArrayList<>(botsByName.values());
    }

    public void executeCommand(MinecraftServer server, CommandSourceStack source, String command) {
        try {
            server.getCommands().getDispatcher().execute(command, source);
        } catch (CommandSyntaxException e) {
            FabricCarbot.LOGGER.error("Failed to execute command: {}", command, e);
        }
    }
}