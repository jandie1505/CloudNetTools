package net.jandie1505.cloudnettools.commands;

import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.provider.CloudServiceProvider;
import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot;
import eu.cloudnetservice.modules.bridge.BridgeDocProperties;
import eu.cloudnetservice.modules.bridge.player.*;
import net.jandie1505.cloudnettools.CloudNetTools;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerlistCommand extends Command implements TabExecutor {
    private CloudNetTools plugin;

    public PlayerlistCommand(CloudNetTools plugin) {
        super("playerlist", "cloudnettools.glist", "glist", "listplayers");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        // permission check

        if (!sender.hasPermission("cloudnettools.jumpto")) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNo permission"));
            return;
        }

        sender.sendMessage(TextComponent.fromLegacyText("§eLoading player list..."));

        this.plugin.getProxy().getScheduler().runAsync(this.plugin, () -> {

            // get playermanager

            PlayerManager playerManager = InjectionLayer.ext().instance(ServiceRegistry.class).firstProvider(PlayerManager.class);

            // get player provider of all players

            List<CloudPlayer> players;

            if (args.length > 0) {

                if (args.length < 2) {
                    sender.sendMessage(TextComponent.fromLegacyText("§cNo servers specified"));
                    return;
                }

                switch (args[0]) {
                    case "task" -> {
                        PlayerProvider playerProvider = playerManager.taskOnlinePlayers(args[1]);
                        players = List.copyOf(playerProvider.players());
                    }
                    case "group" -> {
                        PlayerProvider playerProvider = playerManager.groupOnlinePlayers(args[1]);
                        players = List.copyOf(playerProvider.players());
                    }
                    case "service" -> {
                        CloudServiceProvider serviceProvider = InjectionLayer.ext().instance(CloudServiceProvider.class);
                        ServiceInfoSnapshot service = serviceProvider.serviceByName(args[1]);

                        if (service == null) {
                            sender.sendMessage(TextComponent.fromLegacyText("§cService does not exist"));
                            return;
                        }

                        List<ServicePlayer> servicePlayers = List.copyOf(service.readProperty(BridgeDocProperties.PLAYERS));
                        List<CloudPlayer> playerList = new ArrayList<>();

                        for (ServicePlayer servicePlayer : servicePlayers) {
                            CloudPlayer player = playerManager.onlinePlayer(servicePlayer.uniqueId());

                            if (player == null) {
                                continue;
                            }

                            playerList.add(player);

                        }

                        players = List.copyOf(playerList);

                    }
                    default -> {
                        sender.sendMessage(TextComponent.fromLegacyText("§cUsage: /glist task/group/service"));
                        return;
                    }
                }

            } else {
                players = List.copyOf(playerManager.onlinePlayers().players());
            }

            // admin permission

            boolean hasAdminPermission = sender.hasPermission("cloudnettools.adminlevel");

            // cache hidden players

            List<UUID> hiddenPlayers = this.plugin.getHiddenPlayers();

            // create list

            ComponentBuilder componentBuilder = new ComponentBuilder()
                    .append("Online players:")
                    .color(ChatColor.YELLOW);

            for (CloudPlayer player : players) {

                boolean isHidden = hiddenPlayers.contains(player.uniqueId());

                if (!hasAdminPermission && isHidden) {
                    continue;
                }

                ComponentBuilder hoverText = new ComponentBuilder()
                        .append("Name: ")
                        .color(ChatColor.GREEN)
                        .append(player.name())
                        .color(ChatColor.YELLOW)
                        .append("\n")
                        .append("UUID: ")
                        .color(ChatColor.GREEN)
                        .append(player.name())
                        .color(ChatColor.YELLOW)
                        .append("\n")
                        .append("Hidden: ")
                        .color(ChatColor.GREEN)
                        .append(String.valueOf(isHidden))
                        .color(ChatColor.YELLOW);

                NetworkServiceInfo proxy = player.loginService();
                NetworkServiceInfo server = player.connectedService();

                if (proxy != null && server != null) {

                    hoverText
                            .append("\n")
                            .append("Proxy: ")
                            .color(ChatColor.GREEN)
                            .append(proxy.serverName())
                            .color(ChatColor.YELLOW)
                            .append("\n")
                            .append("Server: ")
                            .color(ChatColor.GREEN)
                            .append(server.serverName())
                            .color(ChatColor.YELLOW);

                }

                componentBuilder.append(" ");
                componentBuilder.append(player.name());
                componentBuilder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.create()));

                if (isHidden) {
                    componentBuilder.color(ChatColor.GRAY);
                } else {
                    componentBuilder.color(ChatColor.YELLOW);
                }

            }

            if (players.isEmpty()) {
                componentBuilder.append(" ---")
                        .color(ChatColor.YELLOW);
            }

            sender.sendMessage(componentBuilder.create());

        });

    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {

        if (!sender.hasPermission("cloudnettools.glist")) {
            return List.of();
        }

        if (args.length == 1) {
            return List.of(
                    "task",
                    "group",
                    "service"
            );
        }

        return List.of();

    }
}
