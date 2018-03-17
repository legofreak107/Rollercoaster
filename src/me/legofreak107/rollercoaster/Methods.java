package me.legofreak107.rollercoaster;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import me.legofreak107.rollercoaster.libs.CustomPath;
import me.legofreak107.rollercoaster.libs.CustomPathBuilder;
import me.legofreak107.rollercoaster.objects.Cart;
import me.legofreak107.rollercoaster.objects.Seat;
import me.legofreak107.rollercoaster.objects.Track;
import me.legofreak107.rollercoaster.objects.Train;

public class Methods {

	public Main plugin;
	
	public Cart getCart(String name){
		Cart c = new Cart();
		c.name = name;
		ItemStack item = new ItemStack(Material.AIR, 1, (short)Short.parseShort(plugin.getConfig().getString("Trains." + name + ".skin.materialdata")));
		item.setTypeId(plugin.getConfig().getInt("Trains." + name + ".skin.materialid"));
		c.skin = item;
		ArrayList<Seat> seats = new ArrayList<Seat>();
		for(int i = 0; i < plugin.getConfig().getInt("Trains." + name + ".seatcount"); i ++){
			Seat s = new Seat();
			s.fb =  plugin.getConfig().getInt("Trains." + name + ".seat" + i + ".offsetfb");
			s.lr =  plugin.getConfig().getInt("Trains." + name + ".seat" + i + ".offsetlr");
			seats.add(s);
		}
		c.seats = seats;
		return c;
	}
	
	public Cart getCart(ArmorStand name){
		for(Train t : plugin.trains){
			for(Cart c : t.carts){
				if(c.holder == name){
					return c;
				}
			}
		}
		return null;
	}
	
	public Track getTrack(String name){
		Track t = new Track();
		ArrayList<Location> loc = new ArrayList<Location>();
		Location origin = new Location(Bukkit.getWorld(plugin.getConfig().getString("Tracks." + name + ".origin.w")),
				(Double)plugin.getConfig().get("Tracks." + name + ".origin.x"),
				(Double)plugin.getConfig().get("Tracks." + name + ".origin.y"),
				(Double)plugin.getConfig().get("Tracks." + name + ".origin.z")
				);
		for(int i = 0; i < plugin.getConfig().getInt("Tracks." + name + ".locs"); i ++){
			Location l = new Location(Bukkit.getWorld(plugin.getConfig().getString("Tracks." + name + ".loc" + i + ".w")),
					(Double)plugin.getConfig().get("Tracks." + name + ".loc" + i + ".x"),
					(Double)plugin.getConfig().get("Tracks." + name + ".loc" + i + ".y"),
					(Double)plugin.getConfig().get("Tracks." + name + ".loc" + i + ".z")
					);
			loc.add(l);
		}
		t.locstosave = loc;
		t.locs = generateTrack(loc, origin);
		t.origin = origin;
		t.name = name;
		return t;
	}
	
	public Train getTrain(String name){
		Train t = null;
		for(Train tr : plugin.trains){
			if(tr.track.name.equalsIgnoreCase(name)){
				t = tr;
			}
		}
		return t;
	}
	public Train getTrain(Cart name){
		Train t = null;
		for(Train tr : plugin.trains){
			if(tr.carts.contains(name)){
				t = tr;
			}

		}
		return t;
	}
	
	public Track getTrackStorage(String name){
		Track t = null;
		for(Track tr : plugin.tracks){
			if(tr.name.equalsIgnoreCase(name)){
				t = tr;
			}
		}
		return t;
	}
	
	public Boolean isTrack(String name){
		Boolean t = false;
		for(String s : plugin.getConfig().getConfigurationSection("Tracks").getKeys(false)){
			if(s.equalsIgnoreCase(name)){
				t = true;
			}
		}
		return t;
	}
	
	public Boolean isTrain(String name){
		Boolean t = false;
		for(String s : plugin.getConfig().getConfigurationSection("Trains").getKeys(false)){
			if(s.equalsIgnoreCase(name)){
				t = true;
			}
		}
		return t;
	}
	
	public ArrayList<Location> generateTrack(ArrayList<Location> locs, Location origin){
		CustomPathBuilder.origin = origin;
		CustomPathBuilder.vectorList = locs;
		CustomPath path = CustomPathBuilder.build();
		ArrayList<Location> as = new ArrayList<Location>();
		for (int i = 0; i <= path.getPathLenght(); i++) {
			Location loc = path.getPathPosition((double) i);
			loc.setPitch(0);
			loc.setYaw(0);
			as.add(loc);
		}
		return as;
	}
	
	public Train spawnTrain(String name, Integer length, Boolean hasLoco, Location loc){
		ArrayList<Cart> carts = new ArrayList<Cart>();
		Train train = new Train();
		ArmorStand loco = null;
		for(int i = 0; i < length; i ++){
			if(i == length-1 && hasLoco){
				Cart t = getCart(name + "loco");
				ArmorStand cart = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);									
				cart.setSmall(true);
				cart.setCollidable(false);
				cart.setAI(false);
				cart.setGravity(false);
				cart.setVisible(false);
				cart.setHelmet(t.skin);
				cart.setCustomName("RollerCoaster");
				t.holder = cart;
				t.place = i;
				loco = cart;
				ArrayList<Seat> seats = new ArrayList<Seat>();
				for(Seat s : t.seats){
					ArmorStand se = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);						
					se.setSmall(true);
					se.setCollidable(false);
					se.setAI(false);
					se.setGravity(false);
					se.setVisible(false);
					s.holder = se;
					s.parent = cart;
					s.locked = false;
					seats.add(s);
					plugin.seatInfo.put(se, s);
				}
				t.seats = seats;
				carts.add(t);
				t.train = train;
				train.loco = t;
			}else{
				Cart t = getCart(name + "cart");
				ArmorStand cart = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);						
				cart.setSmall(true);
				cart.setCollidable(false);
				cart.setAI(false);
				cart.setGravity(false);
				cart.setVisible(false);
				cart.setHelmet(t.skin);
				cart.setCustomName("RollerCoaster");
				t.holder = cart;
				t.place = i;
				t.loco = loco;
				ArrayList<Seat> seats = new ArrayList<Seat>();
				for(Seat s : t.seats){
					ArmorStand se = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);				
					se.setSmall(true);
					se.setCollidable(false);
					se.setAI(false);
					se.setGravity(false);
					se.setVisible(false);
					s.holder = se;
					s.parent = cart;
					s.locked = false;
					seats.add(s);
					plugin.seatInfo.put(se, s);
				}
				if(i == length-1){
					loco = cart;
					train.loco = t;
				}
				t.seats = seats;
				t.train = train;
				carts.add(t);
			}
		}
		train.carts = carts;
		train.inStation = true;
		train.riding = false;
		return train;
	}
	
	public Boolean isSeat(ArmorStand a){
		Boolean check = false;
		if(plugin.seatInfo.containsKey(a)){
			check = true;
		}
		return check;
	}

	public Seat getSeat(ArmorStand a){
		return plugin.seatInfo.get(a);
	}
}
