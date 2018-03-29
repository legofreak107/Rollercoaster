package me.legofreak107.rollercoaster.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.legofreak107.rollercoaster.objects.Seat;
import me.legofreak107.rollercoaster.objects.Train;

public class TrainEnterEvent extends Event{

    private static final HandlerList handlers = new HandlerList();
    private String message;
    private Train train;
    private Player player;
    private Seat seat;

    public TrainEnterEvent(String example, Train t, Player p, Seat s) {
        message = example;
        train = t;
        player = p;
        seat = s;
    }
    
    public Train getTrain(){
    	return train;
    }
    
    public Seat getSeat(){
    	return seat;
    }
    
    public Player getPlayer(){
    	return player;
    }

    public String getMessage() {
        return message;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
