package net.jandie1505.cloudnettools;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import net.jandie1505.cloudnettools.commands.JumpToCommand;
import net.jandie1505.cloudnettools.commands.WhereIsCommand;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.UUID;

public class CloudNetTools extends Plugin {

    @Override
    public void onEnable() {

        try {
            Class.forName("eu.cloudnetservice.driver.inject.InjectionLayer");
        } catch (Exception e) {
            this.onDisable();
            this.getLogger().severe("This plugin does not work without CloudNet");
            return;
        }

        this.getProxy().getPluginManager().registerCommand(this, new WhereIsCommand(this));
        this.getProxy().getPluginManager().registerCommand(this, new JumpToCommand(this));

    }

    @Override
    public void onDisable() {
        this.getProxy().getPluginManager().unregisterListeners(this);
        this.getProxy().getPluginManager().unregisterCommands(this);
    }

    public boolean hasPermission(UUID uniqueId, String permission) {

        ProxiedPlayer proxiedTarget = this.getProxy().getPlayer(uniqueId);

        if (proxiedTarget != null) {
            return proxiedTarget.hasPermission(permission);
        } else {

            try {
                Class.forName("net.luckperms.api.LuckPerms");

                LuckPerms luckPerms = LuckPermsProvider.get();

                User lpTarget = luckPerms.getUserManager().loadUser(uniqueId).join();

                if (lpTarget == null) {
                    return false;
                }

                return lpTarget.getCachedData().getPermissionData().checkPermission(permission).asBoolean();

            } catch (ClassNotFoundException | IllegalStateException e) {
                return false;
            }

        }

    }

    public boolean isHidden(UUID uniqueId) {

        try {
            Class.forName("de.myzelyam.api.vanish.BungeeVanishAPI");

            return BungeeVanishAPI.getAllInvisiblePlayers().contains(uniqueId);
        } catch (ClassNotFoundException e) {
            return false;
        }

    }
}
