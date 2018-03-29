package me.legofreak107.rollercoaster.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.legofreak107.rollercoaster.Main;
import me.legofreak107.rollercoaster.objects.Train;

public class PlayerJoin implements Listener{

	private Main plugin;
	
	public PlayerJoin(Main pl){
		plugin = pl;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		if(!plugin.trainsSpawned){
			if(plugin.getCustomSaveConfig().contains("Saved")){
				for(String s : plugin.getCustomSaveConfig().getConfigurationSection("Saved").getKeys(false)){
			        Integer loopSeconds = plugin.getCustomSaveConfig().getInt("Saved." + s + ".loopSeconds");
			        Integer cartOffset = plugin.getCustomSaveConfig().getInt("Saved." + s + ".cartOffset");
			        Integer minSpeed = plugin.getCustomSaveConfig().getInt("Saved." + s + ".minSpeed");
			        Integer maxSpeed = plugin.getCustomSaveConfig().getInt("Saved." + s + ".maxSpeed");
			        Integer trainLength = plugin.getCustomSaveConfig().getInt("Saved." + s + ".trainLength");
			        Boolean hasLoco = plugin.getCustomSaveConfig().getBoolean("Saved." + s + ".hasLoco");
			        Boolean isSmall = plugin.getCustomSaveConfig().getBoolean("Saved." + s + ".isSmall");
			        String trainName = plugin.getCustomSaveConfig().getString("Saved." + s + ".trainName");
			        Train t = plugin.getAPI().spawnTrain(trainName, trainLength, hasLoco, plugin.getAPI().getTrack(s).origin, isSmall, plugin.getAPI().getTrack(s), minSpeed, maxSpeed, cartOffset);
			        plugin.loop.put(t, loopSeconds);
			        plugin.getAPI().startTrain(s);
			        plugin.getCustomSaveConfig().set("Saved." + s, null);
				}
				plugin.getCustomSaveConfig().set("Saved", null);
				plugin.trainsSpawned = true;
			}
		}
	}
	
}
