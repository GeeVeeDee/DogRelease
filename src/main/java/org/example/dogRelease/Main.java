package org.example.dogRelease;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new PlayerInteractHandler(this), this);

        getLogger().info("DogRelease Plugin - made by GeeVeeDee");
    }

    @Override
    public void onDisable() {
    }
}
