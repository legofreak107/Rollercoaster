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
	public Integer rotation = 0;
	public Integer rotationTarget = 0;
	public Double tilt;
	public Double tiltTarget;
	public Boolean loop;
	public Boolean onTopOfSign = false;
	public Boolean autoRotation = true;
	public Boolean autoTilt = true;
	
}
