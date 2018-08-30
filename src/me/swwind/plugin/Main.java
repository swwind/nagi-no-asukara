package me.swwind.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin implements Listener, CommandExecutor {

	private FileConfiguration config;
	private HashMap<String, BossBar> bsr;
	private Server server;
	private List<String> wetDeath;
	private List<String> asking;

	private final String CONFIG_NAME = "oceanman";
	
	private BossBar createWaterBar(Player player) {

		BossBar bossbar = server.createBossBar("Water Remains", BarColor.BLUE, BarStyle.SEGMENTED_6);
		bossbar.setVisible(true);
		bossbar.addPlayer(player);
		
		return bossbar;
	}

	@Override
	public void onEnable() {

		server = getServer();
		config = getConfig();
		bsr = new HashMap<>();
		wetDeath = new ArrayList<>();
		asking = new ArrayList<>();
		
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

				for (int i = 0; i < onlines.size(); ++ i) {
					
					Player player = onlines.get(i);
					String name = player.getName();
					
					if (list.contains(name)) {
						
						if (player.getGameMode() == GameMode.CREATIVE ||
							player.getGameMode() == GameMode.SPECTATOR) {
							return;
						}

						if (!bsr.containsKey(name)) {
							bsr.put(name, createWaterBar(player));
						}
						
						BossBar bossbar = bsr.get(player.getName());
						double grs = bossbar.getProgress();
						
						Block block = player.getLocation().getBlock();

						if (
							// in lava
							block.getType() == Material.LAVA ||
								
							// on fire
							player.getFireTicks() > 0
								
							) {
								
							// not good for afterbirth
							grs -= 1.0 / 5 / TIMES_PRE_SECOND;
						
						} else if (
							// in a water log
							block.getBlockData() instanceof Waterlogged ||
								
							// in water
							block.getType() == Material.WATER ||
								
							// in sea grass
							block.getType() == Material.KELP_PLANT ||
							block.getType() == Material.SEAGRASS ||
							block.getType() == Material.TALL_SEAGRASS ||
								
							// in rain
							(player.getWorld().hasStorm() && player.getLocation().getBlockY() >= player.getWorld().getHighestBlockYAt(player.getLocation()))
								
							) {
								
							grs += 1.0 / 20 / TIMES_PRE_SECOND;
							
						} else {
							
							// on land
							grs -= 1.0 / 360 / TIMES_PRE_SECOND;
						
						}
						
						grs = Math.min(grs, 1);
						grs = Math.max(grs, 0);
						bossbar.setVisible(grs < 1);
						bossbar.setProgress(grs);
						
						int health = getHeath(grs);
						
						if (health == 0) {
							wetDeath.add(name);
							player.setHealth(0);
						}
						player.setHealthScale(health * 2);
						
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

	private int getHeath(double grs) {
		int time = (int) (grs * 360);
		if (time >= 180) return 10;
		if (time >= 120) return 9;
		if (time >= 90)  return 8;
		if (time >= 60)  return 7;
		if (time >= 50)  return 6;
		if (time >= 40)  return 5;
		if (time >= 30)  return 4;
		if (time >= 20)  return 3;
		if (time >= 10)  return 2;
		if (time >= 1)   return 1;
		return 0;
	}

	private void giveAfterbirth(Player player) {

		player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 100000, 0));
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
	public void onAskSeaMaster(PlayerInteractEntityEvent e) {
		
		e.setCancelled(true);

		Player p = e.getPlayer();

		if (e.getRightClicked() instanceof Villager) {

			Villager v = (Villager) e.getRightClicked();

			if (v.getCustomName().equalsIgnoreCase("海神")) {
				
				if (asking.contains(p.getName())) {
					return;
				}
				
				p.sendMessage("<海神> 你想要我做什么？");
				p.sendMessage("1) 获取胞衣（需要 5 个河豚）");
				p.sendMessage("2) 换取 16 个海泡菜（需要 64 个海带）");
				asking.add(p.getName());
			}
		}
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		Player p = event.getPlayer();
		String name = p.getName();
		
		if (asking.contains(name)) {
			asking.remove(name);
			event.setCancelled(true);
			
			String message = event.getMessage();
			switch (message) {
			case "1": // ask for after birth
				handleAskForAfterbirth(p);
				break;
			case "2": // ask for sea pickles
				handleAskForSeaPickles(p);
				break;
			default:
				p.sendMessage(ChatColor.RED + "未知指令");
			}
		}
	}
	
	public void handleAskForAfterbirth(Player p) {
		Inventory inventory = p.getInventory();
		List<String> list = config.getStringList(CONFIG_NAME);

		if (list.contains(p.getName())) {
			p.sendMessage("<海神> 你已经有胞衣了");
			return;
		}

		if (!inventory.containsAtLeast(new ItemStack(Material.PUFFERFISH), 5)) {
			p.sendMessage("<海神> 抱歉，我需要 5 个河豚才能帮你办事");
			return;
		}

		list.add(p.getName());
		config.set(CONFIG_NAME, list);
		inventory.removeItem(new ItemStack(Material.PUFFERFISH, 5));
		p.sendMessage("<海神> 感谢你的河豚，你可以在水下自由呼吸了");
		
		saveConfig();
	}

	private void handleAskForSeaPickles(Player p) {
		Inventory inventory = p.getInventory();

		if (!inventory.containsAtLeast(new ItemStack(Material.KELP), 64)) {
			p.sendMessage("<海神> 我需要 64 个海带作为交换");
			return;
		}

		inventory.removeItem(new ItemStack(Material.KELP, 64));
		inventory.addItem(new ItemStack(Material.SEA_PICKLE, 16));
		p.sendMessage("<海神> 感谢你的海带");
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {

		Player p = event.getEntity();
		String name = p.getName();
		
		if (wetDeath.contains(name)) {
			event.setDeathMessage(name + " 干死了");
			wetDeath.remove(name);
		}

		List<String> list = config.getStringList(CONFIG_NAME);
		
		if (list.remove(name)) {
			config.set(CONFIG_NAME, list);
			saveConfig();
		}
		
		if (bsr.containsKey(name)) {
			BossBar b = bsr.get(name);
			b.removeAll();
			bsr.remove(name);
		}
		
		p.setHealthScale(20);
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
