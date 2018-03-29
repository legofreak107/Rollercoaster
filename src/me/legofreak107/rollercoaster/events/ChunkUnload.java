package me.legofreak107.rollercoaster.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import me.legofreak107.rollercoaster.Main;
import me.legofreak107.rollercoaster.objects.PathPoint;
import me.legofreak107.rollercoaster.objects.Track;

public class ChunkUnload implements Listener{

	private Main plugin;
	
	public ChunkUnload(Main pl){
		plugin = pl;
	}
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent e){
		for(Track t : plugin.tracks){
			for(PathPoint loc : t.locs){
				if(loc.toLocation(e.getWorld()).getChunk() == e.getChunk()){
					e.setCancelled(true);
				}
			}
		}
	}
	
}
