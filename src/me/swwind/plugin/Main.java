package me.swwind.plugin;

import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	@Override
	public void onEnable() {
		Server server = getServer();
		PluginManager pluginManager = server.getPluginManager();
		MyListener listener = new MyListener();
		pluginManager.registerEvents(listener, this);
		getCommand("summon-sea-master").setExecutor(listener);
	}

	@Override
	public void onDisable() {
		// TODO nothing
	}
}
