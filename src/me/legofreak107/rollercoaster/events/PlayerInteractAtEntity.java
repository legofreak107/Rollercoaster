package me.legofreak107.rollercoaster.events;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import me.legofreak107.rollercoaster.Main;
import me.legofreak107.rollercoaster.api.TrainEnterEvent;
import me.legofreak107.rollercoaster.objects.Seat;
import me.legofreak107.rollercoaster.objects.Train;

public class PlayerInteractAtEntity implements Listener{

	private Main plugin;
	
	public PlayerInteractAtEntity(Main pl){
		plugin = pl;
	}
	
	@EventHandler
	public void onEntityClick(PlayerInteractAtEntityEvent e){
		Player p = e.getPlayer();
		Entity en = e.getRightClicked();
		if(en instanceof ArmorStand){
			if(plugin.getAPI().isSeat((ArmorStand) en)){
				Seat s = plugin.getAPI().getSeat((ArmorStand) en);
				Train t = s.train;
				e.setCancelled(true);
				if(!s.locked || !t.locked){
					TrainEnterEvent event = new TrainEnterEvent("TrainEnterEvent", t, p, s);
					Bukkit.getServer().getPluginManager().callEvent(event);
					e.setCancelled(true);
					en.addPassenger(p);
				}
			}else if(en.getCustomName().contains("RollerCoaster")){
				e.setCancelled(true);
			}
		}
	}
	
}
