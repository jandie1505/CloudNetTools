package net.jandie1505.cloudnettools;

import eu.cloudnetservice.driver.inject.InjectionLayer;
import net.jandie1505.cloudnettools.commands.JumpToCommand;
import net.md_5.bungee.api.plugin.Plugin;

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

        this.getProxy().getPluginManager().registerCommand(this, new JumpToCommand(this));

    }

    @Override
    public void onDisable() {
        this.getProxy().getPluginManager().unregisterListeners(this);
        this.getProxy().getPluginManager().unregisterCommands(this);
    }
}
