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
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
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

        BotRecord bot = manager.getBot(alias);
        if (bot == null) {
            source.sendFailure(Component.literal("§c未找到别名: §f" + alias));
            return 0;
        }

        // Carpet 26.3 syntax: /player <name> spawn at <x y z> facing <yaw pitch> in <dimension>
        String dim = bot.dimension;
        String cmd = String.format(Locale.ROOT,
                "player %s spawn at %.2f %.2f %.2f facing %.1f %.1f in %s",
                bot.name, bot.x, bot.y, bot.z, bot.yaw, bot.pitch, dim);

        manager.executeCommand(server, source, cmd);

        source.sendSuccess(() -> Component.literal("§a已召唤假人: §f" + bot.name + " §a到登记位置"), false);
        return 1;
    }

    private static int killBot(CommandContext<CommandSourceStack> ctx, String alias) {
        CommandSourceStack source = ctx.getSource();
        BotManager manager = FabricCarbot.botManager;
        MinecraftServer server = source.getServer();

        BotRecord bot = manager.getBot(alias);
        if (bot == null) {
            source.sendFailure(Component.literal("§c未找到别名: §f" + alias));
            return 0;
        }

        manager.executeCommand(server, source, "player " + bot.name + " kill");
        source.sendSuccess(() -> Component.literal("§a已移除假人: §f" + bot.name), false);
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

        StringBuilder sb = new StringBuilder("§a已登记假人:\n");
        for (BotRecord bot : bots) {
            String aliases = (bot.aliases != null && !bot.aliases.isEmpty())
                    ? String.join(", ", bot.aliases)
                    : "(无)";
            sb.append("§7- §f")
              .append(bot.name)
              .append(" §7[§f")
              .append(aliases)
              .append("§7] @ §f")
              .append(bot.dimension)
              .append("\n");
        }
        source.sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }

    private static int removeBot(CommandContext<CommandSourceStack> ctx, String alias) {
        CommandSourceStack source = ctx.getSource();
        BotManager manager = FabricCarbot.botManager;

        BotRecord bot = manager.getBot(alias);
        if (bot == null) {
            source.sendFailure(Component.literal("§c未找到别名: §f" + alias));
            return 0;
        }

        manager.removeBot(alias);
        source.sendSuccess(() -> Component.literal("§a已删除假人记录: §f" + bot.name), false);
        return 1;
    }
}