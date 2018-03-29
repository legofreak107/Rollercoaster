package me.legofreak107.rollercoaster;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
	

	private FileConfiguration customLangConfig = null;
	private File customLangConfigFile = null;
	
	public void reloadCustomLangConfig(String name) {
	    if (customLangConfigFile == null) {
	    	customLangConfigFile = new File(plugin.getDataFolder(), name+".yml");
	    }
	    customLangConfig = YamlConfiguration.loadConfiguration(customLangConfigFile);
	}
	
	public FileConfiguration getCustomLangConfig(String name) {
	    if (customLangConfig == null) {
	        reloadCustomLangConfig(name);
	    }
	    return customLangConfig;
	}
	
	public void saveCustomLangConfig(String name) {
	    if (customLangConfig == null || customLangConfigFile == null) {
	        return;
	    }
	    try {
	        getCustomLangConfig(name).save(customLangConfigFile);
	    } catch (IOException ex) {
	    	plugin.getLogger().log(Level.SEVERE, "Could not save config to " + customLangConfigFile, ex);
	    }
	}
	
	
	private FileConfiguration customSaveConfig = null;
	private File customSaveConfigFile = null;
	
	public void reloadCustomSaveConfig() {
	    if (customSaveConfigFile == null) {
	    	customSaveConfigFile = new File(plugin.getDataFolder(), "save.yml");
	    }
	    customSaveConfig = YamlConfiguration.loadConfiguration(customSaveConfigFile);
	}
	
	public FileConfiguration getCustomSaveConfig() {
	    if (customSaveConfig == null) {
	        reloadCustomSaveConfig();
	    }
	    return customSaveConfig;
	}
	
	public void saveCustomSaveConfig() {
	    if (customSaveConfig == null || customSaveConfigFile == null) {
	        return;
	    }
	    try {
	        getCustomSaveConfig().save(customSaveConfigFile);
	    } catch (IOException ex) {
	        plugin.getLogger().log(Level.SEVERE, "Could not save config to " + customSaveConfigFile, ex);
	    }
	}
	
}
