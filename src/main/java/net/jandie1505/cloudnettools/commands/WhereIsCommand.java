package net.jandie1505.cloudnettools.commands;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import eu.cloudnetservice.modules.bridge.player.NetworkServiceInfo;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import net.jandie1505.cloudnettools.CloudNetTools;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.List;
import java.util.UUID;

public class WhereIsCommand extends Command implements TabExecutor {
    private CloudNetTools plugin;

    public WhereIsCommand(CloudNetTools plugin) {
        super("whereis", "cloudnettools.whereis");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        // permission check

        if (!sender.hasPermission("cloudnettools.whereis")) {
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

        // get target

        CloudPlayer target;

        try {
            target = playerManager.onlinePlayer(UUID.fromString(args[0]));
        } catch (IllegalArgumentException e) {
            target = playerManager.firstOnlinePlayer(args[0]);
        }

        // check if target is online

        if (target == null) {
            sender.sendMessage(TextComponent.fromLegacyText("§cPlayer does not exist"));
            return;
        }

        // check if sender is allowed to see target

        if (!sender.hasPermission("cloudnettools.admin") && this.plugin.hasPermission(target.uniqueId(), "cloudnettools.adminlevel") && this.plugin.isHidden(target.uniqueId())) {
            sender.sendMessage(TextComponent.fromLegacyText("§cPlayer does not exist"));
            return;
        }

        // get network service info

        NetworkServiceInfo proxy = target.connectedService();
        NetworkServiceInfo server = target.connectedService();

        if (proxy == null || server == null) {
            sender.sendMessage(TextComponent.fromLegacyText("§cService does not exist"));
            return;
        }

        ComponentBuilder componentBuilder = new ComponentBuilder()
                .append("Where is player " + target.name() + ":")
                .color(ChatColor.GREEN)
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

        if (sender.hasPermission("cloudnettools.jumpto")) {
            componentBuilder
                    .append(" (")
                    .color(ChatColor.GREEN)
                    .append("jump")
                    .color(ChatColor.AQUA)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/jumpto " + target.uniqueId()))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Jump to the server of " + target.name()).color(ChatColor.AQUA).create()))
                    .append(")")
                    .color(ChatColor.GREEN);
        }

        sender.sendMessage(componentBuilder.create());

    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] strings) {
        return List.of();
    }
}
