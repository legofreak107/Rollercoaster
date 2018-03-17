package me.legofreak107.rollercoaster;

import org.bukkit.event.Listener;

import me.legofreak107.rollercoaster.objects.Cart;
import me.legofreak107.rollercoaster.objects.Track;

public class SaveAndLoad implements Listener{
	public Main plugin;
	
	public void saveTrain(Cart t){
		//naam
		//skin
		//seats
		plugin.getConfig().set("Trains." + t.name + ".skin.materialid", t.skin.getType().getId());
		plugin.getConfig().set("Trains." + t.name + ".skin.materialdata", t.skin.getData().getData());
		plugin.getConfig().set("Trains." + t.name + ".seatcount", t.seats.size());
		for(int i = 0; i < t.seats.size(); i ++){
			plugin.getConfig().set("Trains." + t.name + ".seat" + i + ".offsetfb", t.seats.get(i).fb);
			plugin.getConfig().set("Trains." + t.name + ".seat" + i + ".offsetlr", t.seats.get(i).lr);
		}
		plugin.saveConfig();
	}
	
	public void saveTrack(Track t){
		plugin.getConfig().set("Tracks." + t.name + ".locs", t.locstosave.size());
		plugin.getConfig().set("Tracks." + t.name + ".origin.x", t.origin.getX());
		plugin.getConfig().set("Tracks." + t.name + ".origin.y", t.origin.getY());
		plugin.getConfig().set("Tracks." + t.name + ".origin.z", t.origin.getZ());
		plugin.getConfig().set("Tracks." + t.name + ".origin.w", t.origin.getWorld().getName());
		for(int i = 0; i < t.locstosave.size(); i ++){
			plugin.getConfig().set("Tracks." + t.name + ".loc" + i + ".x", t.locstosave.get(i).getX());
			plugin.getConfig().set("Tracks." + t.name + ".loc" + i + ".y", t.locstosave.get(i).getY());
			plugin.getConfig().set("Tracks." + t.name + ".loc" + i + ".z", t.locstosave.get(i).getZ());
			plugin.getConfig().set("Tracks." + t.name + ".loc" + i + ".w", t.locstosave.get(i).getWorld().getName());
		}
		plugin.saveConfig();
	}
}
