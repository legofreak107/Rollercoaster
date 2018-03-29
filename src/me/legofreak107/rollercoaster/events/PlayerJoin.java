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
			if(plugin.sal.getCustomSaveConfig().contains("Saved")){
				for(String s : plugin.sal.getCustomSaveConfig().getConfigurationSection("Saved").getKeys(false)){
			        Integer loopSeconds = plugin.sal.getCustomSaveConfig().getInt("Saved." + s + ".loopSeconds");
			        Integer cartOffset = plugin.sal.getCustomSaveConfig().getInt("Saved." + s + ".cartOffset");
			        Integer minSpeed = plugin.sal.getCustomSaveConfig().getInt("Saved." + s + ".minSpeed");
			        Integer maxSpeed = plugin.sal.getCustomSaveConfig().getInt("Saved." + s + ".maxSpeed");
			        Integer trainLength = plugin.sal.getCustomSaveConfig().getInt("Saved." + s + ".trainLength");
			        Boolean hasLoco = plugin.sal.getCustomSaveConfig().getBoolean("Saved." + s + ".hasLoco");
			        Boolean isSmall = plugin.sal.getCustomSaveConfig().getBoolean("Saved." + s + ".isSmall");
			        String trainName = plugin.sal.getCustomSaveConfig().getString("Saved." + s + ".trainName");
			        Train t = plugin.getAPI().spawnTrain(trainName, trainLength, hasLoco, plugin.getAPI().getTrack(s).origin, isSmall, plugin.getAPI().getTrack(s), minSpeed, maxSpeed, cartOffset);
			        plugin.loop.put(t, loopSeconds);
			        plugin.getAPI().startTrain(s);
			        plugin.sal.getCustomSaveConfig().set("Saved." + s, null);
				}
				plugin.sal.getCustomSaveConfig().set("Saved", null);
				plugin.trainsSpawned = true;
			}
		}
	}
	
}
