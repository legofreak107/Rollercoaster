package me.legofreak107.rollercoaster.objects;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class Seat {

	public double fb;
	public double lr;
	public ArmorStand parent;
	public ArmorStand holder;
	public Player inside;
	public Boolean locked = false;
	public Train train;
	public Cart cart;
	
}
