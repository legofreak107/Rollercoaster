package me.legofreak107.rollercoaster.objects;

import java.util.ArrayList;


import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

public class Cart {

	public ItemStack skin;
	public int place;
	public String name;
	public ArmorStand loco;
	public ArmorStand holder;
	public ArrayList<Seat> seats;
	public Integer pos;
	public Integer lastY;
	public Train train;
	
}
