package me.legofreak107.rollercoaster;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import me.legofreak107.rollercoaster.libs.CustomPath;
import me.legofreak107.rollercoaster.libs.CustomPathBuilder;
import me.legofreak107.rollercoaster.objects.Cart;
import me.legofreak107.rollercoaster.objects.Seat;
import me.legofreak107.rollercoaster.objects.Track;
import me.legofreak107.rollercoaster.objects.Train;
import net.minecraft.server.v1_12_R1.EntityArmorStand;

public class Main extends JavaPlugin implements Listener{
	
	public SaveAndLoad sal = new SaveAndLoad();
	public Methods methods = new Methods();
	public HashMap<ArmorStand, Seat> seatInfo = new HashMap<ArmorStand, Seat>();
	public ArrayList<Track> tracks = new ArrayList<Track>();
	public ArrayList<Train> trains = new ArrayList<Train>();
	
	@EventHandler
	public void onEntityClick(PlayerInteractAtEntityEvent e){
		Player p = e.getPlayer();
		Entity en = e.getRightClicked();
		if(en instanceof ArmorStand){
			if(methods.isSeat((ArmorStand) en)){
				Seat s = methods.getSeat((ArmorStand) en);
				if(!s.locked){
					//TODO: Fire enter event
					e.setCancelled(true);
					en.addPassenger(p);
				}
			}else if(en.getCustomName().contains("RollerCoaster")){
				e.setCancelled(true);
			}
		}
	}
	
	HashMap<Chunk, List<Cart>> cartsInChunk = new HashMap<Chunk, List<Cart>>();
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent e){
		for(Track t : tracks){
			for(Location loc : t.locs){
				if(loc.getChunk() == e.getChunk()){
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onExit(VehicleExitEvent e){
		if(e.getExited() instanceof ArmorStand){
			if(seatInfo.containsKey(e.getExited())){
				Seat s = seatInfo.get(e.getExited());
				if(s.locked){
					e.setCancelled(true);
				}
			}
		}
	}
	
	public void runStartup(){
		if(getConfig().contains("Tracks")) {
			for(String track : getConfig().getConfigurationSection("Tracks").getKeys(false)){
				Track t = methods.getTrack(track);
				tracks.add(t);
			}
		}
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run(){
				for(Seat s : seatInfo.values()){
					if(s.inside != null){
					Player p = s.inside;
						if(!p.isInsideVehicle()){
							ArmorStand a = s.holder;
							if(s.locked){
								a.addPassenger(p);
							}else{
								s.inside = null;
							}
						}
					}
				}
				for(Train t : trains){
					Track tr = t.track;
					for(Cart c : t.carts){
						if(t.riding){
							if(t.speed == null || t.speed == 0){
								t.speed = 1;
							}
							if(t.maxSpeed == null || t.maxSpeed == 0){
								t.maxSpeed = 10;
							}
							if(t.minSpeed == null || t.minSpeed == 0){
								t.minSpeed = 1;
							}
				        	if(tr.locs.size() >= c.pos + t.speed + 10){
								Location point = tr.locs.get(c.pos + t.speed);
								Location pointToLook = tr.locs.get(c.pos + t.speed + 1);	
								if(t.loco.lastY == null) {
									t.loco.lastY = point.getBlockY();
								}
								if(point.getBlockY() - t.loco.lastY <= -2) {
									if(t.loco == c) {
										if(t.speed < t.maxSpeed) {
											t.speed ++;
										}
										t.loco.lastY = point.getBlockY();
									}
								}else if(point.getBlockY() - t.loco.lastY >= 2) {
									if(t.loco == c) {
										if(t.speed > t.minSpeed) {
											t.speed --;
										}
										t.loco.lastY = point.getBlockY();
									}
								}
					        	Location loc = point;
					        	Location l2 = lookAt(loc,pointToLook);
				        		EntityArmorStand a1 = ((CraftArmorStand) c.holder).getHandle();
			        			a1.setLocation(loc.getX(), loc.getY(), loc.getZ(),l2.getYaw(),0); 
						        c.holder.setHeadPose(new EulerAngle(Math.toRadians(l2.getPitch()+Offset),0,Kantel));
						        for(Seat seat : c.seats){
									Location fb = c.holder.getLocation().add(c.holder.getLocation().getDirection().setY(0).normalize().multiply(seat.fb));
						    	    float z = (float)(fb.getZ() + ( seat.lr * Math.sin(Math.toRadians(fb.getYaw() + 90 * 0)))); 
						    	    float x = (float)(fb.getX() + ( seat.lr * Math.cos(Math.toRadians(fb.getYaw() + 90 * 0))));
						    	    EntityArmorStand s1 = ((CraftArmorStand)seat.holder).getHandle();
						    	    s1.setLocation(x, c.holder.getLocation().getY()-((Math.toRadians(l2.getPitch()+Offset)))*seat.fb, z, (float) (0), c.holder.getLocation().getPitch());
						        }
						        previous = l2.getYaw();
					        	c.pos = c.pos + t.speed;
					        	if(t.passedStation == null){
					        		t.passedStation = false;
					        	}
				        		if(t.loco == c && c.pos > c.place*20 && t.passedStation) {
				        			t.speed = 1;
				        			t.riding = false;
				        			t.passedStation = false;
	        						for(Cart c2 : t.carts){
	        							for(Seat s : c2.seats){
	        								s.locked = false;
	        							}
	        						}
				        			if(loop.containsKey(t)){
				        				startLoop(t);
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
		}, 0L, 1L);
	}
	
	public void startLoop(Train t){
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
			public void run(){
				for(int i = 0; i < t.carts.size(); i ++){
					Cart c = t.carts.get(i);
					c.pos = i * 20;
				}
				t.riding = true;
				for(Cart c2 : t.carts){
					for(Seat s : c2.seats){
						s.locked = true;
					}
				}
			}
		}, 20L * loop.get(t));
	}
	
	public HashMap<Train, Integer> loop = new HashMap<Train, Integer>();
	
	@Override
	public void onEnable(){
		Bukkit.getPluginManager().registerEvents(this, this);
		sal.plugin = this;
		methods.plugin = this;
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
			public void run(){
				runStartup();
			}
		}, 5L);
	}
	
	
	public FileConfiguration reloadCustomConfig(String name) {
	    File customConfigFile = new File(getDataFolder(), name+".yml");
	    return YamlConfiguration.loadConfiguration(customConfigFile);
	}
	
	public FileConfiguration getCustomConfig(String name) {
	    return reloadCustomConfig(name);
	}
	
	public void saveCustomConfig(String name) {
	    try {
	        getCustomConfig(name).save(new File(getDataFolder(), name+".yml"));
	    } catch (IOException ex) {
	        getLogger().log(Level.SEVERE, "Could not save config to " + new File(getDataFolder(), name+".yml"), ex);
	    }
	}
	
	public void saveDefaultConfig(String name) {
	    File customConfigFile = new File(getDataFolder(), name+".yml");
	    if (!customConfigFile.exists()) {            
	         saveResource(name+".yml", false);
	     }
	}
	
	 public static Location lookAt(Location loc, Location lookat) {
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
	 
	
	@Override
	public void onDisable(){
		//TODO: kill all trains
		for(Train t : trains){
			for(Cart c : t.carts){
				c.holder.remove();
				for(Seat s : c.seats){
					s.holder.remove();
				}
			}
			trains.remove(t);
		}
	}
	
    public static boolean checkMe(String s) {
        boolean amIValid = false;
        try {
         Integer.parseInt(s);
         // s is a valid integer!
         amIValid = true;
        } catch (NumberFormatException e) {
         //sorry, not an integer
         // we just move on, but you could have code here
        }
        return amIValid;
      }
    public static boolean checkMeb(String s) {
        boolean amIValid = false;
        try {
         Double.parseDouble(s);
         // s is a valid integer!
         amIValid = true;
        } catch (NumberFormatException e) {
         //sorry, not an integer
         // we just move on, but you could have code here
        }
        return amIValid;
      }
    
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("rc")){
			if(args.length > 0){
			if(args[0].equalsIgnoreCase("spawntrain")){
				if(sender.hasPermission("rollercoaster.spawntrain")) {
				if(args.length == 5){
					String track = args[1];
					String train = args[2];
					if(checkMe(args[3]) && checkMe(args[4])) {
						if(getConfig().contains("Tracks." + track)) {
							if(getConfig().contains("Trains." + train + "cart")) {
								Integer length = Integer.parseInt(args[3]);
								Integer loco = Integer.parseInt(args[4]);
								Boolean hasLoc = false;
								if(loco == 1)
									hasLoc = true;
								Train t = methods.spawnTrain(train, length, hasLoc, ((Player) sender).getLocation());
								t.track = methods.getTrackStorage(track);
								trains.add(t);
								sender.sendMessage("§3Train spawned!");
							}else {
								sender.sendMessage("§3Invalid train type, try /rc train list for a list of trains.");
							}
						}else {
							sender.sendMessage("§3Invalid track type, try /rc tracklist for a list of tracks.");
						}
					}else {
						sender.sendMessage("§3Usage: /rc spawntrain <trainname> <traintype> <cartamount>");
					}
				}else{
					sender.sendMessage("§3Usage: /rc spawntrain <trainname> <traintype> <cartamount>");
				}
				}else {
					sender.sendMessage("§4You don't have permissions to excecute this command!");
				}
			}else if(args[0].equalsIgnoreCase("starttrain")){
				if(sender.hasPermission("rollercoaster.starttrain")) {
				if(args.length == 2){
					String track = args[1];
					if(methods.isTrack(track)) {
						Train train = methods.getTrain(track);
						train.inStation = false;
						train.riding = true;
						for(int i = 0; i < train.carts.size(); i ++){
							Cart c = train.carts.get(i);
							c.pos = i * 20;
						}
						sender.sendMessage("§3Train started!");
					}else{
						sender.sendMessage("§3No track found by this name, type /rc tracklist for a list of tracks.");
					}
				}else{
					sender.sendMessage("§3Usage: /rc starttrain <trainname>");
				}
				}else {
					sender.sendMessage("§4You don't have permissions to excecute this command!");
				}
			}else if(args[0].equalsIgnoreCase("loop")){
				if(sender.hasPermission("rollercoaster.loop")) {
				if(args.length == 3){
					String track = args[1];
					if(checkMe(args[2])){
						if(methods.isTrack(track)) {
							Train train = methods.getTrain(track);
							sender.sendMessage("§3Train loop set to: " + args[2] + " seconds!");
							loop.put(train, Integer.parseInt(args[2]));
						}else{
							sender.sendMessage("§3No track found by this name, type /rc tracklist for a list of tracks.");
						}
					}else{
						sender.sendMessage("§3The argument you entered isn't a valid number!");
					}
				}else{
					sender.sendMessage("§3Usage: /rc loop <trackname> <loop seconds>");
				}
				}else {
					sender.sendMessage("§4You don't have permissions to excecute this command!");
				}
			}else if(args[0].equalsIgnoreCase("stoptrain")){
				if(sender.hasPermission("rollercoaster.stoptrain")) {
				if(args.length == 2){
					String track = args[1];
					if(methods.isTrack(track)) {
						Train train = methods.getTrain(track);
						train.inStation = false;
						train.riding = false;
						sender.sendMessage("§3Train stopped!");
					}else{
						sender.sendMessage("§3No track found by this name, type /rc tracklist for a list of tracks.");
					}
				}else{
					sender.sendMessage("§3Usage: /rc stoptrain <trainname>");
				}
				}else {
					sender.sendMessage("§4You don't have permissions to excecute this command!");
				}
			}else if(args[0].equalsIgnoreCase("addpoint")){
				if(sender.hasPermission("rollercoaster.createtrack")) {
				CustomPathBuilder.addPoint(((Player)sender).getLocation());
				sender.sendMessage("§3Point added!");
				}else {
					sender.sendMessage("§4You don't have permissions to excecute this command!");
				}
			}else if(args[0].equalsIgnoreCase("startpath")){
				if(sender.hasPermission("rollercoaster.createtrack")) {
				CustomPathBuilder.origin = ((Player)sender).getLocation();
				CustomPathBuilder.vectorList.clear();
				sender.sendMessage("§3Started the creation of a new path!");
				sender.sendMessage("§3You can now add points to the path by typing /rc addpoint");
				}else {
					sender.sendMessage("§4You don't have permissions to excecute this command!");
				}
			}else if(args[0].equalsIgnoreCase("train")){
				if(sender.hasPermission("rollercoaster.createtrain")) {
				if(args.length > 1){
					if(args[1].equalsIgnoreCase("create")){
						if(args.length == 5 && checkMe(args[3]) && checkMe(args[4])){
							Cart c = new Cart();
							c.name = args[2] + "cart";

							ArrayList<Seat> seats = new ArrayList<Seat>();
							for(int i = 0; i < Integer.parseInt(args[4]); i ++){
								Seat s = new Seat();
								s.fb = 0;
								s.lr = 0;
								seats.add(s);
							}
							c.seats = seats;
							c.skin = ((Player)sender).getInventory().getItemInMainHand();
							sal.saveTrain(c);
							
							Cart c2 = new Cart();
							c2.name = args[2] + "loco";

							ArrayList<Seat> seats2 = new ArrayList<Seat>();
							for(int i = 0; i < Integer.parseInt(args[3]); i ++){
								Seat s = new Seat();
								s.fb = 0;
								s.lr = 0;
								seats2.add(s);
							}
							c2.seats = seats2;
							c2.skin = ((Player)sender).getInventory().getItemInMainHand();
							sal.saveTrain(c2);
							sender.sendMessage("§3Train created.");
						}else{
							sender.sendMessage("§3Usage: /rc train create <typename> <seatsloco> <seatscart>");
						}
					}else
					if(args[1].equalsIgnoreCase("chairs")){
						if(args[2].equalsIgnoreCase("pos")){
							if(args.length == 8){
								String type = args[7];
								String name = args[3];
								if(type.equalsIgnoreCase("loco")){
									name = name+"loco";
								}else if(type.equalsIgnoreCase("cart")){
									name = name+"cart";
								}
								if(methods.isTrain(name)) {
									if(checkMe(args[4])) {
										if(checkMeb(args[5])) {
											if(checkMeb(args[6])) {
												Integer number = Integer.parseInt(args[4]);
												
												if(number <= getConfig().getInt("Trains." + name + ".seatcount") && number > -1){
													Double LR = Double.parseDouble(args[5]);
													Double FB = Double.parseDouble(args[6]);
													getConfig().set("Trains."+name+".seat"+number+".offsetlr", LR);
													getConfig().set("Trains."+name+".seat"+number+".offsetfb", FB);
													saveConfig();
													sender.sendMessage("§3Train edited.");
												}else{
													sender.sendMessage("§3This is not a valid seat number, please type a valid number.");
												}
											}else{
												sender.sendMessage("§3This is not a valid number, please type a valid number.");
											}
										}else{
											sender.sendMessage("§3This is not a valid number, please type a valid number.");
										}
									}else{
										sender.sendMessage("§3This is not a valid number, please type a valid number.");
									}
								}else{
									sender.sendMessage("§3No train found by this name, type /rc train list for a list of trains.");
								}
							}else{
								sender.sendMessage("§3Usage: /rc train chairs pos <name> <seatnumber> <lr> <fb> <cart/loco>");
							}
						}
					}else if(args[1].equalsIgnoreCase("setskin")){
							if(args.length == 4){
								String name = args[2];
									String type = args[3];
									if(type.equalsIgnoreCase("Loco")) {
										if(methods.isTrain(name + "loco")) {
											getConfig().set("Trains."+name+"loco.skin.materialid", ((Player)sender).getInventory().getItemInMainHand().getTypeId());
											getConfig().set("Trains."+name+"loco.skin.materialdata", ((Player)sender).getInventory().getItemInMainHand().getDurability() + "");
											saveConfig();
											sender.sendMessage("§3Train edited.");
										}else {
											sender.sendMessage("§3No train found by this name, type /rc train list for a list of trains.");
										}
									}else 
										if(type.equalsIgnoreCase("Cart")) {
											if(methods.isTrain(name + "cart")) {
												getConfig().set("Trains."+name+"cart.skin.materialid", ((Player)sender).getInventory().getItemInMainHand().getTypeId());
												getConfig().set("Trains."+name+"cart.skin.materialdata", ((Player)sender).getInventory().getItemInMainHand().getDurability() + "");
												saveConfig();
												sender.sendMessage("§3Train edited.");
											}else {
												sender.sendMessage("§3No train found by this name, type /rc train list for a list of trains.");
											}
										}else {
											sender.sendMessage("§3Invalid args, use Loco/Train instead!");
										}
								}
					}else if(args[1].equalsIgnoreCase("list")){
							sender.sendMessage("§3Train List:");
							if(getConfig().contains("Trains")) {
						    for(String s : getConfig().getConfigurationSection("Trains").getKeys(false)){
						    	if(s.contains("loco")){
						    		sender.sendMessage("§2" + s.replace("loco", ""));
						    	}
						    }
							}else {
								sender.sendMessage("§2You don't have any saved trains.");
							}
							
					}
				}
				}else {
					sender.sendMessage("§4You don't have permissions to excecute this command!");
				}
			}else if(args[0].equalsIgnoreCase("build")){
				if(sender.hasPermission("rollercoaster.createtrack")) {
				if(args.length == 2){
					String trainname = args[1];
					Track t = new Track();
					t.name = trainname;
					CustomPath path = CustomPathBuilder.build();
					t.locstosave = (ArrayList<Location>) CustomPathBuilder.vectorList;
					t.origin = CustomPathBuilder.origin;
					List<Location> as = new ArrayList<Location>();
					for (int i = 0; i <= path.getPathLenght(); i++) {
						Location loc = path.getPathPosition((double) i);
						loc.setPitch(0);
						loc.setYaw(0);
						as.add(loc);
					}
					t.locs = (ArrayList<Location>) as;
					tracks.add(t);
					sal.saveTrack(t);
					sender.sendMessage("§3Path build!");
				}else{
					sender.sendMessage("§3Usage: /rc build <trainname>");
				}
				}else {
					sender.sendMessage("§4You don't have permissions to excecute this command!");
				}
			}else if(args[0].equalsIgnoreCase("removetrain")){
				if(sender.hasPermission("rollercoaster.removetrain")) {
				if(args.length == 2){
					String trainname = args[1];
					if(methods.getTrain(trainname) != null){
						Train t = methods.getTrain(trainname);
						for(Cart c : t.carts){
							c.holder.remove();
							for(Seat s : c.seats){
								s.holder.remove();
							}
						}
						trains.remove(t);
						sender.sendMessage("§3Train removed!");
					}
				}else{
					sender.sendMessage("§3Usage: /rc removetrain <trainname>");
				}
				}else {
					sender.sendMessage("§4You don't have permissions to excecute this command!");
				}
			}else if(args[0].equalsIgnoreCase("tracklist")){
				if(sender.hasPermission("rollercoaster.tracklist")) {
				sender.sendMessage("§3Track List:");

				if(getConfig().contains("Tracks")) {
			    for(Track s : tracks){
			    	sender.sendMessage("§2" + s.name);
			    }
				}else {
					sender.sendMessage("§2You don't have any saved tracks! Please create one first!");
				}
				}else {
					sender.sendMessage("§4You don't have permissions to excecute this command!");
				}
			}else if(args[0].equalsIgnoreCase("locktrain")){
				if(sender.hasPermission("rollercoaster.lock")) {
				if(args.length == 2){
					String st = args[1];
					if(methods.getTrain(st) != null){
						Train t = methods.getTrain(st);
						for(Cart c : t.carts){
							for(Seat s : c.seats){
								s.locked = true;
							}
						}
						sender.sendMessage("§3Train locked!");
					}
				}else{
					sender.sendMessage("§3Usage: /rc locktrain <trainname>");
				}
				}else {
					sender.sendMessage("§4You don't have permissions to excecute this command!");
				}
			}else if(args[0].equalsIgnoreCase("reload")){
				if(sender.hasPermission("rollercoaster.reload")) {

					for(Train t : trains){
						for(Cart c : t.carts){
							c.holder.remove();
							for(Seat s : c.seats){
								s.holder.remove();
							}
						}
					}
					trains.clear();
					tracks.clear();
					if(getConfig().contains("Tracks")) {
						for(String track : getConfig().getConfigurationSection("Tracks").getKeys(false)){
							Track t = methods.getTrack(track);
							tracks.add(t);
						}
					}
					sender.sendMessage("§2Config reloaded!");
				}else {
					sender.sendMessage("§4You don't have permissions to excecute this command!");
				}
			}else if(args[0].equalsIgnoreCase("unlocktrain")){
				if(sender.hasPermission("rollercoaster.lock")) {
				if(args.length == 2){
					String st = args[1];
					if(methods.getTrain(st) != null){
						Train t = methods.getTrain(st);
						for(Cart c : t.carts){
							for(Seat s : c.seats){
								s.locked = false;
							}
						}
						sender.sendMessage("§3Train unlocked!");
					}
				}else{
					sender.sendMessage("§3Usage: /rc unlocktrain <trainname>");
				}
				}else {
					sender.sendMessage("§4You don't have permissions to excecute this command!");
				}
			}else if(args[0].equalsIgnoreCase("help")){
				if(args.length == 2){
					Integer page = Integer.parseInt(args[1]);
					if(page == 1) {
						sender.sendMessage("§2===============§8 RollerCoaster Help [1] §2===============");
						sender.sendMessage("§3/rc spawntrain <trainname> <traintype> <amount>");
						sender.sendMessage("§3/rc starttrain <trainname>");
						sender.sendMessage("§3/rc stoptrain <trainname>");
						sender.sendMessage("§3/rc startpath");
						sender.sendMessage("§3/rc addpoint");
						sender.sendMessage("§3/rc build <name>");
						sender.sendMessage("§3/rc help 2");
						sender.sendMessage("§2===============§8 RollerCoaster Help [1] §2===============");
					}else if(page == 2) {
						sender.sendMessage("§2===============§8 RollerCoaster Help [2] §2===============");
						sender.sendMessage("§3/rc removetrain <trainname>");
						sender.sendMessage("§3/rc locktrain <trainname>");
						sender.sendMessage("§3/rc unlocktrain <trainname>");
						sender.sendMessage("§3/rc train create <trainname>");
						sender.sendMessage("§3/rc train chairs set <chairamount> <name> <Loco/Cart>");
						sender.sendMessage("§3/rc train chairs pos <chair#> <left/right> <front/back> <name> <Loco/Cart>");
						sender.sendMessage("§3/rc train setskin <trainname> <Loco/Cart>");
						sender.sendMessage("§3/rc train list");
						sender.sendMessage("§3/rc tracklist");
						sender.sendMessage("§2===============§8 RollerCoaster Help [2] §2===============");
					}else {
						sender.sendMessage("§2===============§8 RollerCoaster Help [1] §2===============");
						sender.sendMessage("§3/rc spawntrain <trainname> <traintype> <amount>");
						sender.sendMessage("§3/rc starttrain <trainname>");
						sender.sendMessage("§3/rc stoptrain <trainname>");
						sender.sendMessage("§3/rc startpath");
						sender.sendMessage("§3/rc addpoint");
						sender.sendMessage("§3/rc build <name>");
						sender.sendMessage("§3/rc help 2");
						sender.sendMessage("§2===============§8 RollerCoaster Help [1] §2===============");
					}
				}else{
					sender.sendMessage("§3Usage: /rc help <page>");
				}
			}else{
				sender.sendMessage("§2===============§8 RollerCoaster Help [1] §2===============");
				sender.sendMessage("§3/rc spawntrain <trainname> <traintype> <amount>");
				sender.sendMessage("§3/rc starttrain <trainname>");
				sender.sendMessage("§3/rc stoptrain <trainname>");
				sender.sendMessage("§3/rc startpath");
				sender.sendMessage("§3/rc addpoint");
				sender.sendMessage("§3/rc build <name>");
				sender.sendMessage("§3/rc help 2");
				sender.sendMessage("§2===============§8 RollerCoaster Help [1] §2===============");
			}
			}else{
				sender.sendMessage("§2===============§8 RollerCoaster Help [1] §2===============");
				sender.sendMessage("§3/rc spawntrain <trainname> <traintype> <amount>");
				sender.sendMessage("§3/rc removetrain <trainname>");
				sender.sendMessage("§3/rc starttrain <trainname>");
				sender.sendMessage("§3/rc stoptrain <trainname>");
				sender.sendMessage("§3/rc startpath");
				sender.sendMessage("§3/rc addpoint");
				sender.sendMessage("§3/rc build <name>");
				sender.sendMessage("§3/rc help 2");
				sender.sendMessage("§2===============§8 RollerCoaster Help [1] §2===============");
			}
		}
		return false;
	}
	Integer Offset = 0;
	float previous = 0;
	Integer Kantel = 0;
}
