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
				v.setCustomName("����");
				v.setProfession(Profession.NITWIT);
				p.sendMessage("���Ѿ��ٻ��˺���");

				return true;
				
			} else {
				
				p.sendMessage("ֻ�й���Ա����Ȩ��ʹ�ø�ָ��");
				
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
			
			if (v.getCustomName().equalsIgnoreCase("����")) {
				
				Inventory inventory = p.getInventory();
				
				if (inventory.containsAtLeast(new ItemStack(Material.PUFFERFISH), 5)) {
					
					inventory.removeItem(new ItemStack(Material.PUFFERFISH, 5));
					giveAfterbirth(p);
					p.sendMessage("<����> ��л��ĺ��࣬���Ѿ���������İ���");
					
					Bukkit.broadcastMessage(p.getName() + " ��Ϊ�˴󺣵�һԱ");

				} else {
					
					p.sendMessage("<����> ��Ǹ������Ҫ 5 ��������ܰ������");
					
				}
			}
		}
	}
}
