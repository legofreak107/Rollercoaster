package me.legofreak107.rollercoaster.api;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.legofreak107.rollercoaster.objects.Cart;
import me.legofreak107.rollercoaster.objects.Seat;
import me.legofreak107.rollercoaster.objects.Train;

public class TrainStopEvent extends Event{

    private static final HandlerList handlers = new HandlerList();
    private String message;
    private Train train;

    public TrainStopEvent(String example, Train t) {
        message = example;
        train = t;
    }
    
    public Train getTrain(){
    	return train;
    }
    
    public List<Seat> getSeats(){
    	List<Seat> s = new ArrayList<Seat>();
    	for(Cart c : train.carts){
    		for(Seat se : c.seats){
    			s.add(se);
    		}
    	}
    	return s;
    }
    
    public List<Player> getPlayers(){
    	List<Player> s = new ArrayList<Player>();
    	for(Cart c : train.carts){
    		for(Seat se : c.seats){
    			for(Entity e : se.holder.getPassengers()){
    				if(e instanceof Player){
    					s.add((Player)e);
    				}
    			}
    		}
    	}
    	return s;
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
