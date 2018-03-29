package me.legofreak107.rollercoaster.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityDismountEvent;

import me.legofreak107.rollercoaster.Main;
import me.legofreak107.rollercoaster.api.TrainLeaveEvent;
import me.legofreak107.rollercoaster.objects.Seat;

public class EntityDismount implements Listener{
	
	private Main plugin;
	
	public EntityDismount(Main pl){
		plugin = pl;
	}
	
	@EventHandler
	public void onExit2(EntityDismountEvent e){
		if(e.getDismounted() instanceof ArmorStand){
			if(plugin.seatInfo.containsKey(e.getDismounted())){
				Seat s = plugin.seatInfo.get(e.getDismounted());
				if(s.locked || s.train.locked){
					s.holder.addPassenger(e.getEntity());
				}else{
					TrainLeaveEvent event = new TrainLeaveEvent("TrainLeaveEvent", s.train, (Player)e.getEntity(), s);
					Bukkit.getServer().getPluginManager().callEvent(event);
				}
			}
		}
	}
	
}
