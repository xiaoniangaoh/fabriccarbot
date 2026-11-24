package com.example.fabriccarbot.command;

import com.example.fabriccarbot.FabricCarbot;
import com.example.fabriccarbot.data.BotManager;
import com.example.fabriccarbot.data.BotRecord;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class CarbotCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(literal("carbot")
                .requires(source -> source.permissions() instanceof LevelBasedPermissionSet set
                        && set.level().isEqualOrHigherThan(PermissionLevel.GAMEMASTERS))
                .then(literal("add")
                        .then(argument("botName", StringArgumentType.word())
                                .then(argument("alias", StringArgumentType.greedyString())
                                        .executes(ctx -> addBot(ctx,
                                                StringArgumentType.getString(ctx, "botName"),
                                                StringArgumentType.getString(ctx, "alias"))))))
                .then(literal("spawn")
                        .then(argument("alias", StringArgumentType.greedyString())
                                .executes(ctx -> spawnBot(ctx, StringArgumentType.getString(ctx, "alias")))))
                .then(literal("kill")
                        .then(argument("alias", StringArgumentType.greedyString())
                                .executes(ctx -> killBot(ctx, StringArgumentType.getString(ctx, "alias")))))
                .then(literal("list")
                        .executes(ctx -> listBots(ctx)))
                .then(literal("remove")
                        .then(argument("alias", StringArgumentType.greedyString())
                                .executes(ctx -> removeBot(ctx, StringArgumentType.getString(ctx, "alias")))))
                .then(literal("remove-bot")
                        .then(argument("botName", StringArgumentType.word())
                                .executes(ctx -> removeBotByName(ctx, StringArgumentType.getString(ctx, "botName")))))
        );
    }

    private static int addBot(CommandContext<CommandSourceStack> ctx, String botName, String alias) {
        CommandSourceStack source = ctx.getSource();
        BotManager manager = FabricCarbot.botManager;

        Vec3 pos = source.getPosition();
        String dim = source.getLevel().dimension().identifier().toString();
        Vec2 rot = source.getRotation();
        // Mojang 映射中旋转 Vec2 为 (x=pitch, y=yaw)，与命令行 facing <yaw> <pitch> 顺序相反
        float yaw = rot.y;
        float pitch = rot.x;

        manager.addBot(botName, alias, dim, pos.x, pos.y, pos.z, yaw, pitch);
        source.sendSuccess(() -> Component.literal(
                "§a已登记假人: §f" + botName + "§a (别名: §f" + alias + "§a)"), false);
        return 1;
    }

    private static int spawnBot(CommandContext<CommandSourceStack> ctx, String alias) {
        CommandSourceStack source = ctx.getSource();
        BotManager manager = FabricCarbot.botManager;
        MinecraftServer server = source.getServer();

        List<BotRecord> bots = manager.getBotsByAlias(alias);
        if (bots.isEmpty()) {
            source.sendFailure(Component.literal("§c未找到别名: §f" + alias));
            return 0;
        }

        final int count = bots.size();
        for (BotRecord bot : bots) {
            // Carpet 26.3 syntax: /player <name> spawn at <x y z> facing <yaw pitch> in <dimension>
            String dim = bot.dimension;
            String cmd = String.format(Locale.ROOT,
                    "player %s spawn at %.2f %.2f %.2f facing %.1f %.1f in %s",
                    bot.name, bot.x, bot.y, bot.z, bot.yaw, bot.pitch, dim);

            manager.executeCommand(server, source, cmd);
        }

        source.sendSuccess(() -> Component.literal("§a已召唤 " + count + " 个假人 (别名: §f" + alias + "§a)"), false);
        return 1;
    }

    private static int killBot(CommandContext<CommandSourceStack> ctx, String alias) {
        CommandSourceStack source = ctx.getSource();
        BotManager manager = FabricCarbot.botManager;
        MinecraftServer server = source.getServer();

        List<BotRecord> bots = manager.getBotsByAlias(alias);
        if (bots.isEmpty()) {
            source.sendFailure(Component.literal("§c未找到别名: §f" + alias));
            return 0;
        }

        final int count = bots.size();
        for (BotRecord bot : bots) {
            manager.executeCommand(server, source, "player " + bot.name + " kill");
        }

        source.sendSuccess(() -> Component.literal("§a已移除 " + count + " 个假人 (别名: §f" + alias + "§a)"), false);
        return 1;
    }

    private static int listBots(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        BotManager manager = FabricCarbot.botManager;
        Collection<BotRecord> bots = manager.getAllBots();

        if (bots.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§c暂无登记假人"), false);
            return 1;
        }

        MutableComponent header = Component.literal("§a已登记假人:\n");
        for (BotRecord bot : bots) {
            String aliases = (bot.aliases != null && !bot.aliases.isEmpty())
                    ? String.join(", ", bot.aliases)
                    : "(无)";

            MutableComponent line = Component.literal("§7- §f" + bot.name + " §7[§f" + aliases + "§7] @ §f" + bot.dimension + " ");

            // Spawn button
            MutableComponent spawnBtn = Component.literal("§a[召唤]")
                    .setStyle(Style.EMPTY
                            .withClickEvent(new ClickEvent.RunCommand("/carbot spawn " + bot.getFirstAliasOrName()))
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("点击召唤该位置的假人"))));

            // Kill button
            MutableComponent killBtn = Component.literal("§c[移除]")
                    .setStyle(Style.EMPTY
                            .withClickEvent(new ClickEvent.RunCommand("/carbot kill " + bot.getFirstAliasOrName()))
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("点击移除该位置的假人"))));

            // Remove record button
            MutableComponent removeBtn = Component.literal("§e[删除记录]")
                    .setStyle(Style.EMPTY
                            .withClickEvent(new ClickEvent.RunCommand("/carbot remove-bot " + bot.name))
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("点击删除该假人的登记记录"))));

            header.append(line).append(spawnBtn).append(" ").append(killBtn).append(" ").append(removeBtn).append("\n");
        }
        source.sendSuccess(() -> header, false);
        return 1;
    }

    private static int removeBot(CommandContext<CommandSourceStack> ctx, String alias) {
        CommandSourceStack source = ctx.getSource();
        BotManager manager = FabricCarbot.botManager;

        List<BotRecord> bots = manager.getBotsByAlias(alias);
        if (bots.isEmpty()) {
            source.sendFailure(Component.literal("§c未找到别名: §f" + alias));
            return 0;
        }

        int count = bots.size();
        manager.removeBot(alias);
        source.sendSuccess(() -> Component.literal("§a已删除 " + count + " 条假人记录 (别名: §f" + alias + "§a)"), false);
        return 1;
    }

    private static int removeBotByName(CommandContext<CommandSourceStack> ctx, String botName) {
        CommandSourceStack source = ctx.getSource();
        BotManager manager = FabricCarbot.botManager;

        BotRecord bot = manager.getBotByName(botName);
        if (bot == null) {
            source.sendFailure(Component.literal("§c未找到假人: §f" + botName));
            return 0;
        }

        manager.removeBotByName(botName);
        source.sendSuccess(() -> Component.literal("§a已删除假人记录: §f" + bot.name), false);
        return 1;
    }
}