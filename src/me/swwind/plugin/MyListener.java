package me.swwind.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MyListener implements Listener, CommandExecutor {

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
				
				if (inventory.containsAtLeast(new ItemStack(Material.PUFFERFISH), 5)) {
					
					inventory.removeItem(new ItemStack(Material.PUFFERFISH, 5));
					giveAfterbirth(p);
					p.sendMessage("<海神> 感谢你的河豚，我已经激活了你的胞衣");
					
					Bukkit.broadcastMessage(p.getName() + " 成为了大海的一员");

				} else {
					
					p.sendMessage("<海神> 抱歉，我需要 5 个河豚才能帮你办事");
					
				}
			}
		}
	}
}
