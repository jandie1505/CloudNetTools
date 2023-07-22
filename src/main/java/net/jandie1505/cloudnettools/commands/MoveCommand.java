package net.jandie1505.cloudnettools.commands;

import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.provider.CloudServiceProvider;
import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.driver.service.ServiceEnvironment;
import eu.cloudnetservice.driver.service.ServiceEnvironmentType;
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import eu.cloudnetservice.modules.bridge.player.executor.PlayerExecutor;
import net.jandie1505.cloudnettools.CloudNetTools;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MoveCommand extends Command implements TabExecutor {
    private final CloudNetTools plugin;

    public MoveCommand(CloudNetTools plugin) {
        super("move", "cloudnettools.move", "mv");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        // permission check

        if (!sender.hasPermission("cloudnettools.move")) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNo permission"));
            return;
        }

        // check for arguments

        if (args.length < 2) {
            sender.sendMessage(TextComponent.fromLegacyText("§cUsage: /move <player...> <server>"));
            return;
        }

        // get player manager

        PlayerManager playerManager = InjectionLayer.ext().instance(ServiceRegistry.class).firstProvider(PlayerManager.class);

        // get service provider

        CloudServiceProvider serviceProvider = InjectionLayer.ext().instance(CloudServiceProvider.class);

        // get service

        ServiceInfoSnapshot cloudService = serviceProvider.serviceByName(args[args.length - 1]);

        if (cloudService == null) {
            sender.sendMessage(TextComponent.fromLegacyText("§cServer does not exist"));
            return;
        }

        if (!cloudService.serviceId().environmentName().equals(ServiceEnvironmentType.MINECRAFT_SERVER.name())) {
            sender.sendMessage(TextComponent.fromLegacyText("§cServer cannot be a proxy"));
            return;
        }

        // admin permission

        boolean hasAdminPermission = sender.hasPermission("cloudnettools.adminlevel");

        // get players#

        List<String> successPlayers = new ArrayList<>();
        List<String> failedPlayers = new ArrayList<>();

        for (int i = 0; i < args.length - 1; i++) {

            CloudPlayer player;

            try {
                player = playerManager.onlinePlayer(UUID.fromString(args[i]));
            } catch (IllegalArgumentException e) {
                player = playerManager.firstOnlinePlayer(args[i]);
            }

            if (player == null) {
                failedPlayers.add(args[i]);
                continue;
            }

            if (!hasAdminPermission && this.plugin.hasPermission(player.uniqueId(), "cloudnettools.adminlevel")) {
                failedPlayers.add(player.name());
                continue;
            }

            PlayerExecutor playerExecutor = playerManager.playerExecutor(player.uniqueId());
            playerExecutor.connect(cloudService.name());
            successPlayers.add(player.name());

        }

        // send result message

        ComponentBuilder resultMessage = new ComponentBuilder()
                .append("Move command results:")
                .color(ChatColor.GREEN)
                .append("\n")
                .append("Target: ")
                .color(ChatColor.GREEN)
                .append(cloudService.name())
                .color(ChatColor.YELLOW)
                .append("\n")
                .append("Success:")
                .color(ChatColor.GREEN);

        for (String player : successPlayers) {

            resultMessage
                    .append(" " + player)
                    .color(ChatColor.YELLOW);

        }

        if (successPlayers.isEmpty()) {

            resultMessage
                    .append(" ---")
                    .color(ChatColor.YELLOW);

        }

        resultMessage
                .append("\n")
                .append("Failed:")
                .color(ChatColor.GREEN);

        for (String player : failedPlayers) {

            resultMessage
                    .append(" " + player)
                    .color(ChatColor.YELLOW);

        }

        if (failedPlayers.isEmpty()) {

            resultMessage
                    .append(" ---")
                    .color(ChatColor.YELLOW);

        }

        sender.sendMessage(resultMessage.create());

    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
