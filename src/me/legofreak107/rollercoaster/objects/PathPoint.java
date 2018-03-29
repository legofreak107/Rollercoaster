package me.legofreak107.rollercoaster.objects;

import org.bukkit.Location;
import org.bukkit.World;

public class PathPoint {

	public double x,y,z,tilt;
	
	public PathPoint(Double x,Double y,Double z,Double tilt){
		this.x = x;
		this.y = y;
		this.z = z;
		this.tilt = tilt;
	}
	
	public Location toLocation(World w){
		Location loc = new Location(w, x, y, z);
		return loc;
	}
}
