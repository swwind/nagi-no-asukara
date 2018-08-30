package me.swwind.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Career;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Main extends JavaPlugin implements Listener, CommandExecutor {

	private FileConfiguration config = getConfig();

	private HashMap<String, BossBar> bsr = new HashMap<>();

	private final String CONFIG_NAME = "oceanman";
	
	private Server server;
	
	private BossBar createWaterBar(Player player) {
		BossBar bossbar = server.createBossBar("Water Remains", BarColor.BLUE, BarStyle.SEGMENTED_6);
		bossbar.setVisible(true);
		bossbar.addPlayer(player);
		return bossbar;
	}

	@Override
	public void onEnable() {
		server = getServer();
		config.addDefault(CONFIG_NAME, new ArrayList<String>());
		server.getPluginManager().registerEvents(this, this);
		getCommand("summon-sea-master").setExecutor(this);

		// game interval
		final int TIMES_PRE_SECOND = 4;
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				List<Player> onlines = new ArrayList<>(getServer().getOnlinePlayers());

				List<String> list = config.getStringList(CONFIG_NAME);

				for (int i = 0; i < onlines.size(); ++i) {
					
					Player player = onlines.get(i);
					String name = player.getName();
					
					if (list.contains(name)) {

						if (!bsr.containsKey(name)) {
							bsr.put(name, createWaterBar(player));
						}
						
						BossBar bossbar = bsr.get(player.getName());
						double grs = bossbar.getProgress();

						if (player.getLocation().getBlock().isLiquid()) {
							grs += 1.0 / 20 / TIMES_PRE_SECOND;
						} else {
							grs -= 1.0 / 360 / TIMES_PRE_SECOND;
						}
						
						grs = Math.min(grs, 1);
						grs = Math.max(grs, 0);
						bossbar.setVisible(grs < 1);
						bossbar.setProgress(grs);
						
						if (!player.hasPotionEffect(PotionEffectType.WATER_BREATHING)) {
							giveAfterbirth(player);
						}
					}
				}
			}
		}, 5, 20 / TIMES_PRE_SECOND);
	}

	@Override
	public void onDisable() {
		// TODO nothing
	}

	private void giveAfterbirth(Player player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 100000, 1));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (sender instanceof Player) {

			Player p = (Player) sender;

			if (p.isOp()) {

				Villager v = (Villager) p.getWorld().spawnEntity(p.getLocation(), EntityType.VILLAGER);
				v.setCustomName("海神");
				v.setProfession(Profession.NITWIT);
				v.setCareer(Career.NITWIT, true);
				p.sendMessage("你已经召唤了海神！");

				return true;

			} else {

				p.sendMessage("只有管理员才有权限使用该指令");

				return false;

			}
		}
		return false;
	}

	@EventHandler
	public void onAskForAfterbirth(PlayerInteractEntityEvent e) {

		Player p = e.getPlayer();

		if (e.getRightClicked() instanceof Villager) {

			Villager v = (Villager) e.getRightClicked();

			if (v.getCustomName().equalsIgnoreCase("海神")) {

				Inventory inventory = p.getInventory();

				List<String> list = config.getStringList(CONFIG_NAME);

				if (list.contains(p.getName())) {
					p.sendMessage("你已经有胞衣了");
					return;
				}

				if (!inventory.containsAtLeast(new ItemStack(Material.PUFFERFISH), 5)) {
					p.sendMessage("<海神> 抱歉，我需要 5 个河豚才能帮你办事");
					return;
				}

				list.add(p.getName());
				config.set(CONFIG_NAME, list);
				inventory.removeItem(new ItemStack(Material.PUFFERFISH, 5));
				giveAfterbirth(p);
				p.sendMessage("<海神> 感谢你的河豚，我已经激活了你的胞衣");

				saveConfig();

			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player p = event.getEntity();

		List<String> list = config.getStringList(CONFIG_NAME);
		if (list.remove(p.getName())) {
			config.set(CONFIG_NAME, list);
			saveConfig();
		}

		bsr.remove(p.getName());
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		BossBar bossbar = bsr.get(p.getName());
		if (bossbar != null) {
			bossbar.addPlayer(p);
		}
	}
}
