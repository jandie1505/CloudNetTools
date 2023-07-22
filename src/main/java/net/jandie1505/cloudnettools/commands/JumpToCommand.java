package net.jandie1505.cloudnettools.commands;

import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import eu.cloudnetservice.modules.bridge.player.NetworkServiceInfo;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import eu.cloudnetservice.modules.bridge.player.executor.PlayerExecutor;
import net.jandie1505.cloudnettools.CloudNetTools;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.List;
import java.util.UUID;

public class JumpToCommand extends Command implements TabExecutor {
    CloudNetTools plugin;

    public JumpToCommand(CloudNetTools plugin) {
        super("jumpto", "cloudnettools.jumpto");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        // player check

        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cThis command can only be executed by a player"));
            return;
        }

        // permission check

        if (!sender.hasPermission("cloudnettools.jumpto")) {
            sender.sendMessage(TextComponent.fromLegacyText("§cNo permission"));
            return;
        }

        // prevent index out of bounds

        if (args.length < 1) {
            sender.sendMessage(TextComponent.fromLegacyText("§cYou need to specify a player"));
            return;
        }

        // get player manager

        PlayerManager playerManager = InjectionLayer.ext().instance(ServiceRegistry.class).firstProvider(PlayerManager.class);

        // get cloud player of current player + null check

        CloudPlayer player = playerManager.onlinePlayer(((ProxiedPlayer) sender).getUniqueId());

        if (player == null) {
            sender.sendMessage(TextComponent.fromLegacyText("§cError with your player"));
            return;
        }

        // get target (from uuid or name)

        CloudPlayer target;

        try {
            target = playerManager.onlinePlayer(UUID.fromString(args[0]));
        } catch (IllegalArgumentException e) {
            target = playerManager.firstOnlinePlayer(args[0]);
        }

        // target null check

        if (target == null) {
            sender.sendMessage(TextComponent.fromLegacyText("§cPlayer does not exist"));
            return;
        }

        // get service of target + null check

        NetworkServiceInfo serviceInfo = target.connectedService();

        if (serviceInfo == null) {
            sender.sendMessage(TextComponent.fromLegacyText("§cPlayer is currently not connected to any services"));
            return;
        }

        // connect to service of target

        playerManager.playerExecutor(player.uniqueId()).connect(serviceInfo.serverName());
        sender.sendMessage(TextComponent.fromLegacyText("§aJumping to " + target.name() + " (" + serviceInfo.serverName() + ")"));

    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] strings) {
        return List.of();
    }
}
