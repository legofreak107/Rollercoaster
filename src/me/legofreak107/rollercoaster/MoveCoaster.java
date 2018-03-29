package me.legofreak107.rollercoaster;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import me.legofreak107.rollercoaster.objects.Cart;
import me.legofreak107.rollercoaster.objects.Seat;
import me.legofreak107.rollercoaster.objects.Track;
import me.legofreak107.rollercoaster.objects.Train;
import net.minecraft.server.v1_12_R1.EntityArmorStand;

public class MoveCoaster {
	
	 public Location lookAt(Location loc, Location lookat) {
	        //Clone the loc to prevent applied changes to the input loc
	        loc = loc.clone();

	        // Values of change in distance (make it relative)
	        double dx = lookat.getX() - loc.getX();
	        double dy = lookat.getY() - loc.getY();
	        double dz = lookat.getZ() - loc.getZ();

	        // Set yaw
	        if (dx != 0) {
	            // Set yaw start value based on dx
	            if (dx < 0) {
	                loc.setYaw((float) (1.5 * Math.PI));
	            } else {
	                loc.setYaw((float) (0.5 * Math.PI));
	            }
	            loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
	        } else if (dz < 0) {
	            loc.setYaw((float) Math.PI);
	        }

	        // Get the distance from dx/dz
	        double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));

	        // Set pitch
	        loc.setPitch((float) -Math.atan(dy / dxz));

	        // Set values, convert to degrees (invert the yaw since Bukkit uses a different yaw dimension format)
	        loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
	        loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);

	        return loc;
	    }
	 
	 public Main plugin;
	 
	public void updateMovement(){
		for(Train t : plugin.trains){
			Track tr = t.track;
			for(Cart c : t.carts){
				if(t.riding){
					if(t.speed == null){
						t.speed = t.minSpeed;
					}
					if(c.tilt == null || c.tilt == 0){
						c.tilt = 0D;
					}
					if(c.tiltTarget == null || c.tiltTarget == 0){
						c.tiltTarget = 0D;
					}
					if(t.maxSpeed == null || t.maxSpeed == 0){
						t.maxSpeed = 10;
					}
					if(t.minSpeed == null || t.minSpeed <= 0){
						t.minSpeed = 1;
					}
					if(c.rotation == null){
						c.rotation = 0;
					}
					if(c.holder.getLocation().add(new Vector(0,-2,0)).getBlock().getType() == Material.SIGN_POST ||
							c.holder.getLocation().add(new Vector(0,-2,0)).getBlock().getType() == Material.WALL_SIGN){
						if(!c.onTopOfSign){
						c.onTopOfSign = true;
						Sign s = (Sign) c.holder.getLocation().add(new Vector(0,-2,0)).getBlock().getState();
						if(s.getLine(0).equalsIgnoreCase("[rc]")){
							if(s.getLine(1).equalsIgnoreCase("maxspeed")){
								Integer d = Integer.parseInt(s.getLine(2));
								t.maxSpeed = d;
							}else if(s.getLine(1).equalsIgnoreCase("locked")){
								Integer d = Integer.parseInt(s.getLine(2));
								if(d == 1){
									t.locked = true;
								}else if(d == 0){
									t.locked = false;
								}
							}else if(s.getLine(1).equalsIgnoreCase("speed")){
								Integer d = Integer.parseInt(s.getLine(2));
								t.speed = d;
							}else if(s.getLine(1).equalsIgnoreCase("autotilt")){
								Boolean d = Boolean.parseBoolean(s.getLine(2));
								c.autoTilt = d;
							}else if(s.getLine(1).equalsIgnoreCase("upsidedown")){
								Boolean d = Boolean.parseBoolean(s.getLine(2));
								c.loop = d;
							}else if(s.getLine(1).equalsIgnoreCase("autorotation")){
								Boolean d = Boolean.parseBoolean(s.getLine(2));
								c.autoRotation = d;
							}else if(s.getLine(1).equalsIgnoreCase("rotation")){
								c.rotationTarget = Integer.parseInt(s.getLine(2));
							}else if(s.getLine(1).equalsIgnoreCase("wait")){
								if(t.loco == c){
									int oldSpeed = t.speed;
									t.speed = 0;
									plugin.wait1(Integer.parseInt(s.getLine(2)), oldSpeed, t);
								}
							}
								else if(s.getLine(1).equalsIgnoreCase("setskin")){
								String type = (s.getLine(2));
								if(type.equalsIgnoreCase("loco")){
									if(t.loco == c){
										ItemStack item = new ItemStack(Material.STONE, 1, (short)Short.parseShort(s.getLine(3).split(":")[1]));
										item.setTypeId(Integer.parseInt(s.getLine(3).split(":")[0]));
										c.holder.setHelmet(item);
									}
								}else{
									if(t.loco != c){
										ItemStack item = new ItemStack(Material.STONE, 1, (short)Short.parseShort(s.getLine(3).split(":")[1]));
										item.setTypeId(Integer.parseInt(s.getLine(3).split(":")[0]));
										c.holder.setHelmet(item);
									}
								}
							}else if(s.getLine(1).equalsIgnoreCase("minspeed")){
								Integer d = Integer.parseInt(s.getLine(2));
								t.minSpeed = d;
							}
						}
						}
					}else 
					if(c.holder.getLocation().add(new Vector(0,-1,0)).getBlock().getType() == Material.SIGN_POST ||
							c.holder.getLocation().add(new Vector(0,-1,0)).getBlock().getType() == Material.WALL_SIGN){
						if(!c.onTopOfSign){
						c.onTopOfSign = true;
						Sign s = (Sign) c.holder.getLocation().add(new Vector(0,-1,0)).getBlock().getState();
						if(s.getLine(0).equalsIgnoreCase("[rc]")){
							if(s.getLine(1).equalsIgnoreCase("maxspeed")){
								Integer d = Integer.parseInt(s.getLine(2));
								t.maxSpeed = d;
							}else if(s.getLine(1).equalsIgnoreCase("speed")){
								Integer d = Integer.parseInt(s.getLine(2));
								t.speed = d;
							}else if(s.getLine(1).equalsIgnoreCase("rotation")){
								c.rotationTarget = Integer.parseInt(s.getLine(2));
							}else if(s.getLine(1).equalsIgnoreCase("wait")){
								if(t.loco == c){
									int oldSpeed = t.speed;
									t.speed = 0;
									plugin.wait1(Integer.parseInt(s.getLine(2)), oldSpeed, t);
								}
							}else if(s.getLine(1).equalsIgnoreCase("locked")){
								Integer d = Integer.parseInt(s.getLine(2));
								if(d == 1){
									t.locked = true;
								}else if(d == 0){
									t.locked = false;
								}
							}else if(s.getLine(1).equalsIgnoreCase("upsidedown")){
								Boolean d = Boolean.parseBoolean(s.getLine(2));
								c.loop = d;
							}else if(s.getLine(1).equalsIgnoreCase("autorotation")){
								Boolean d = Boolean.parseBoolean(s.getLine(2));
								c.autoRotation = d;
							}else if(s.getLine(1).equalsIgnoreCase("autospeed")){
								Boolean d = Boolean.parseBoolean(s.getLine(2));
								t.autoSpeed = d;
							}else if(s.getLine(1).equalsIgnoreCase("autotilt")){
								Boolean d = Boolean.parseBoolean(s.getLine(2));
								c.autoTilt = d;
							}else if(s.getLine(1).equalsIgnoreCase("setskin")){
								String type = (s.getLine(2));
								if(type.equalsIgnoreCase("loco")){
									if(t.loco == c){
										ItemStack item = new ItemStack(Material.STONE, 1, (short)Short.parseShort(s.getLine(3).split(":")[1]));
										item.setTypeId(Integer.parseInt(s.getLine(3).split(":")[0]));
										c.holder.setHelmet(item);
									}
								}else{
									if(t.loco != c){
										ItemStack item = new ItemStack(Material.STONE, 1, (short)Short.parseShort(s.getLine(3).split(":")[1]));
										item.setTypeId(Integer.parseInt(s.getLine(3).split(":")[0]));
										item.setDurability((short)Short.parseShort(s.getLine(3).split(":")[1]));
										c.holder.setHelmet(item);
									}
								}
							}else if(s.getLine(1).equalsIgnoreCase("minspeed")){
								Integer d = Integer.parseInt(s.getLine(2));
								t.minSpeed = d;
							}
						}
						}
					}else{
						if(c.onTopOfSign){
							c.onTopOfSign = false;
						}
					}
					if(t.speed == 0){
						
					}else{
			        	if(tr.locs.size() >= c.pos + (int)t.speed + 10){
							Location point = tr.locs.get(c.pos + (int)t.speed).toLocation(tr.origin.getWorld());
							Location pointToLook = tr.locs.get(c.pos + (int)t.speed + 1).toLocation(tr.origin.getWorld());
							if(t.loco.lastY == null) {
								t.loco.lastY = point.getBlockY();
							}
							if(t.loco == c) {
								if(point.getBlockY() - t.loco.lastY < -2) {
										if(t.speed <= t.maxSpeed) {
											t.speed += 2;
										}
										t.loco.lastY = point.getBlockY();
								}else if(point.getBlockY() - t.loco.lastY > 2) {
										if(t.speed >= t.minSpeed) {
											t.speed -= 2;
										}
										t.loco.lastY = point.getBlockY();
								}
							}
							if(c.rotationTarget == 0){
								if(c.rotation < -3){
									c.rotation += 3;
								}else if(c.rotation > 3){
									c.rotation -= 3;
								}else{
									c.rotation = 0;
								}
							}else if(c.rotationTarget > 0){
								if(c.rotation < c.rotationTarget){
									c.rotation += 3;
								}
							}else if(c.rotationTarget < 0){
								if(c.rotation > c.rotationTarget){
									c.rotation -= 3;
								}
							}
							if(c.tiltTarget > 40){
								c.tiltTarget = 40D;
							}
							if(c.tiltTarget < -40){
								c.tiltTarget = -40D;
							}
							if(c.tiltTarget == 0){
								if(c.tilt < -5){
									c.tilt += 5;
								}else if(c.tilt > 5){
									c.tilt -= 5;
								}else{
									c.tilt = 0D;
								}
							}else if(c.tiltTarget > 0){
								if(c.tilt < c.tiltTarget){
									c.tilt += 5;
								}
							}else if(c.tiltTarget < 0){
								if(c.tilt > c.tiltTarget){
									c.tilt -= 5;
								}
							}
				        	Location loc = point;
				        	Location l2 = lookAt(loc,pointToLook);
				        	Location l3 = lookAt(loc,tr.locs.get(c.pos + (int)t.speed + 5).toLocation(tr.origin.getWorld()));
				        	if(c.autoTilt){
				        		double angle = c.holder.getLocation().getYaw() - l3.getYaw();
				        		angle = (double) (((int)angle) * t.speed);
				        		c.tiltTarget = (double) plugin.round(angle, 5);
				        	}
			        		EntityArmorStand a1 = ((CraftArmorStand) c.holder).getHandle();
			        		if(c.autoRotation){
				        		if(c.loop != null && c.loop){
						        	c.holder.setHeadPose(new EulerAngle(Math.toRadians(-(l2.getPitch()+180)),0,Math.toRadians(c.tilt)));
				        			a1.setLocation(loc.getX(), loc.getY()-2, loc.getZ(),l2.getYaw()-180 + c.rotation,(float) Math.toDegrees(c.holder.getHeadPose().getX())-180);
				        		}else{
						        	c.holder.setHeadPose(new EulerAngle(Math.toRadians(l2.getPitch()),0,Math.toRadians(c.tilt)));
				        			a1.setLocation(loc.getX(), loc.getY(), loc.getZ(),l2.getYaw() + c.rotation,(float) Math.toDegrees(c.holder.getHeadPose().getX()));
				        		}
			        		}else{
				        		if(c.loop != null && c.loop){
				        			a1.setLocation(loc.getX(), loc.getY(), loc.getZ(),-180 + c.rotation,0);
						        	c.holder.setHeadPose(new EulerAngle(Math.toRadians(-(l2.getPitch()+180)),0,Math.toRadians(c.tilt)));
				        		}else{
				        			a1.setLocation(loc.getX(), loc.getY(), loc.getZ(),0 + c.rotation,0); 
						        	c.holder.setHeadPose(new EulerAngle(Math.toRadians(l2.getPitch()),0,Math.toRadians(c.tilt)));
				        		}
			        		}
					        for(Seat seat : c.seats){
								Location fb = c.holder.getLocation().add(c.holder.getLocation().getDirection().setY(0).normalize().multiply(seat.fb));
					    	    float z = (float)(fb.getZ() + ( seat.lr * Math.sin(Math.toRadians(fb.getYaw() + 90 * 0)))); 
					    	    float x = (float)(fb.getX() + ( seat.lr * Math.cos(Math.toRadians(fb.getYaw() + 90 * 0))));
					    	    EntityArmorStand s1 = ((CraftArmorStand)seat.holder).getHandle();
					    	    s1.setLocation(x, c.holder.getLocation().getY()-((Math.toRadians(l2.getPitch())))*seat.fb, z, (float) (0), c.holder.getLocation().getPitch());
					        }
					        plugin.previous = l2.getYaw();
				        	c.pos = c.pos + (int)t.speed;
				        	if(t.passedStation == null){
				        		t.passedStation = false;
				        	}
			        		if(t.loco == c && c.pos > (c.place+1)*t.cartOffset && t.passedStation) {
			        			t.speed = t.minSpeed;
			        			t.riding = false;
			        			plugin.setActive(t.track.name, true);
			        			loc.getWorld().playSound(loc, Sound.BLOCK_LAVA_EXTINGUISH, 10, 10);
			        			t.passedStation = false;
			        			t.locked = false;
        						for(Cart c2 : t.carts){
        							c2.pos = (c2.place+1) * t.cartOffset;
        							c2.tilt = 0D;
        							c2.tiltTarget = 0D;

        							Location Tloc = tr.locs.get(c2.pos + (int)t.speed).toLocation(tr.origin.getWorld());
        			        		EntityArmorStand Ta1 = ((CraftArmorStand) c2.holder).getHandle();
        				        	Ta1.setLocation(Tloc.getX(), Tloc.getY(), Tloc.getZ(),0 + c2.rotation,0); 
        						    c2.holder.setHeadPose(new EulerAngle(Math.toRadians(l2.getPitch()),0,Math.toRadians(c2.tilt)));
        							for(Seat s : c2.seats){
        								s.locked = false;
        								Location fb = c2.holder.getLocation().add(c2.holder.getLocation().getDirection().setY(0).normalize().multiply(s.fb));
        					    	    float z = (float)(fb.getZ() + ( s.lr * Math.sin(Math.toRadians(fb.getYaw() + 90 * 0)))); 
        					    	    float x = (float)(fb.getX() + ( s.lr * Math.cos(Math.toRadians(fb.getYaw() + 90 * 0))));
        					    	    EntityArmorStand s1 = ((CraftArmorStand)s.holder).getHandle();
        					    	    s1.setLocation(x, c2.holder.getLocation().getY()-((Math.toRadians(l2.getPitch())))*s.fb, z, (float) (0), c2.holder.getLocation().getPitch());
        							}
        						}
			        			if(plugin.loop.containsKey(t)){
			        				plugin.startLoop(t);
			        			}
			        		}
						}else{
			        		c.pos = 0;
			        		t.passedStation = true;
			        	}
					}
				}
			}
		}
	}
	
}
