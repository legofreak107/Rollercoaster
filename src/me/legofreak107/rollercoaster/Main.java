package me.legofreak107.rollercoaster;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import me.legofreak107.rollercoaster.api.API;
import me.legofreak107.rollercoaster.api.TrainLockEvent;
import me.legofreak107.rollercoaster.api.TrainStartEvent;
import me.legofreak107.rollercoaster.api.TrainStopEvent;
import me.legofreak107.rollercoaster.api.TrainUnlockEvent;
import me.legofreak107.rollercoaster.events.ChunkUnload;
import me.legofreak107.rollercoaster.events.EntityDismount;
import me.legofreak107.rollercoaster.events.PlayerInteractAtEntity;
import me.legofreak107.rollercoaster.events.PlayerJoin;
import me.legofreak107.rollercoaster.libs.CustomPath;
import me.legofreak107.rollercoaster.libs.CustomPathBuilder;
import me.legofreak107.rollercoaster.libs.LangFile;
import me.legofreak107.rollercoaster.objects.Cart;
import me.legofreak107.rollercoaster.objects.PathPoint;
import me.legofreak107.rollercoaster.objects.Receiver;
import me.legofreak107.rollercoaster.objects.Seat;
import me.legofreak107.rollercoaster.objects.Track;
import me.legofreak107.rollercoaster.objects.Train;

public class Main extends JavaPlugin implements Listener{
	
	//Save and load file (Used for config management)
	public SaveAndLoad sal = new SaveAndLoad();
	
	//Seat info (Used to get the store the Seats to the connected ArmorStands)
	public HashMap<ArmorStand, Seat> seatInfo = new HashMap<ArmorStand, Seat>();
	
	//Stored Tracks
	public ArrayList<Track> tracks = new ArrayList<Track>();
	
	//Stored Trains
	public ArrayList<Train> trains = new ArrayList<Train>();
	
	//Stored visible points (While creating a path)
	public ArrayList<ArmorStand> pointsVisible = new ArrayList<ArmorStand>();
	
	//Stored loops (Keep track of looped trains with amount of seconds)
	public HashMap<Train, Integer> loop = new HashMap<Train, Integer>();
	
	//Enabled language file
	public String langFile;
	
	//Are trains spawned on startup
	public Boolean trainsSpawned = false;
	
	//List of sign receivers to disable/enable
	public HashMap<Integer,Receiver> receivers = new HashMap<Integer,Receiver>();
	
	//Current id for receiver signs
	public Integer id = 0;
	
	
	//Startup (Start the timer to run the trains)
	public void runStartup(){
		MoveCoaster mc = new MoveCoaster();
		mc.plugin = this;
		if(getConfig().contains("Tracks")) {
			for(String track : getConfig().getConfigurationSection("Tracks").getKeys(false)){
				Track t = getAPI().getTrack(track);
				tracks.add(t);
			}
		}
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run(){
				mc.updateMovement();
			}
		}, 0L, 1L);
	}
	
	//Enable/Disable receiver signs
	public void setActive(String name, Boolean active){
		if(active){
			for(World w : Bukkit.getWorlds()){
				for(Chunk c : w.getLoadedChunks()){
					for(BlockState b : c.getTileEntities()){
						if(b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN){
							Sign s = (Sign) b;
							if(s.getLine(0).equalsIgnoreCase("[rc]")){
								if(s.getLine(1).equalsIgnoreCase("receiver")){
									if(s.getLine(2).equalsIgnoreCase(name)){
										Receiver r = new Receiver();
										r.active = active;
										r.name = name;
										r.loc = b.getLocation();
										r.id = id+1;
										receivers.put(id+1, r);
										id ++;
										b.getLocation().getBlock().setType(Material.REDSTONE_TORCH_ON);
									}
								}
							}
						}
					}
				}
			}
		}else{
			for(int i = 1; i < id + 1; i ++){
				Receiver r = receivers.get(i);
				if(r.name.equalsIgnoreCase(name)){
					r.loc.getBlock().setType(Material.SIGN_POST);
					Sign s = (Sign) r.loc.getBlock().getState();
					s.setLine(0, "[RC]");
					s.setLine(1, "receiver");
					s.setLine(2, name);
					s.update();
					r.active = active;
				}
			}
		}
	}

	//Used to load in coasters from a bezier curve in blender (BETA)
	public void loadCoaster(String name, World w) throws NumberFormatException, IOException{
		ArrayList<PathPoint> track = new ArrayList<PathPoint>();
		Integer i = 0;
		   for(String s : Files.readAllLines(Paths.get(getDataFolder() + "/beziercurves/"+name+".txt"))){
		      String[] splitted = s.split(" ");
		      Double x = Double.valueOf(splitted[0]);
		      Double y= Double.valueOf(splitted[1]);
		      Double z = Double.valueOf(splitted[2]);
		      Double tilt = Double.valueOf(splitted[3]);
		      PathPoint t = new PathPoint(x,y,z,tilt);
		      track.add(t);
		      if(i > 100){
				ArmorStand a = (ArmorStand) t.toLocation(w).getWorld().spawnEntity(t.toLocation(w), EntityType.ARMOR_STAND);
				a.setCustomName("test");
				a.setGravity(false);
				i = 0;
		      }
		      i ++;
		   }
		   Bukkit.broadcastMessage("§2Added: " + track.size() + " points");
			Track t = new Track();
			Location origin = new Location(Bukkit.getWorld("world"),
					0,
					0,
					0
					);
			t.locs = track;
			t.origin = origin;
			t.name = name;
			tracks.add(t);
	}
	
	//Round up to nearest multiple of x
	int round(double i, int v){
		if(i < 0){
		    return (int) (Math.ceil(i/v) * v);	
		}else{
		    return (int) (Math.floor(i/v) * v);
		}
	}
	
	//Wait x amount of seconds. After that resume the ride
	public void wait(int waitTime, int oldSpeed, Train t){
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
			public void run(){
				t.speed = oldSpeed;
				t.riding = true;
			}
		}, 20 * waitTime);
	}
	
	//Delay for the loop to restart
	public void startLoop(Train t){
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
			public void run(){
    			setActive(t.track.name, false);
				for(int i = 0; i < t.carts.size(); i ++){
					Cart c = t.carts.get(i);
					c.pos = i * t.cartOffset;
				}
				t.riding = true;
				t.locked = true;
				TrainStartEvent event1 = new TrainStartEvent("TrainStartEvent", t);
				Bukkit.getServer().getPluginManager().callEvent(event1);
				TrainLockEvent event = new TrainLockEvent("TrainLockEvent", t);
				Bukkit.getServer().getPluginManager().callEvent(event);
				for(Cart c2 : t.carts){
					for(Seat s : c2.seats){
						s.locked = true;
					}
				}
			}
		}, 20L * loop.get(t));
	}
	
	//Start of the plugin
	@Override
	public void onEnable(){
		Bukkit.getPluginManager().registerEvents(new ChunkUnload(this), this);
		Bukkit.getPluginManager().registerEvents(new EntityDismount(this), this);
		Bukkit.getPluginManager().registerEvents(new PlayerInteractAtEntity(this), this);
		Bukkit.getPluginManager().registerEvents(new PlayerJoin(this), this);
		sal.plugin = this;
		LangFile lm = new LangFile();
		langFile = "LANG_EN";
		lm.generateLanguageFile(this);
		getAPI().plugin = this;
		if(Bukkit.getOnlinePlayers().size() > 0){
			if(sal.getCustomSaveConfig().contains("Saved")){
				for(String s : sal.getCustomSaveConfig().getConfigurationSection("Saved").getKeys(false)){
			        Integer loopSeconds = sal.getCustomSaveConfig().getInt("Saved." + s + ".loopSeconds");
			        Integer cartOffset = sal.getCustomSaveConfig().getInt("Saved." + s + ".cartOffset");
			        Integer minSpeed = sal.getCustomSaveConfig().getInt("Saved." + s + ".minSpeed");
			        Integer maxSpeed = sal.getCustomSaveConfig().getInt("Saved." + s + ".maxSpeed");
			        Integer trainLength = sal.getCustomSaveConfig().getInt("Saved." + s + ".trainLength");
			        Boolean hasLoco = sal.getCustomSaveConfig().getBoolean("Saved." + s + ".hasLoco");
			        Boolean isSmall = sal.getCustomSaveConfig().getBoolean("Saved." + s + ".isSmall");
			        String trainName = sal.getCustomSaveConfig().getString("Saved." + s + ".trainName");
			        Train t = getAPI().spawnTrain(trainName, trainLength, hasLoco, getAPI().getTrack(s).origin, isSmall, getAPI().getTrack(s), minSpeed, maxSpeed, cartOffset);
			        loop.put(t, loopSeconds);
			        getAPI().startTrain(s);
			        sal.getCustomSaveConfig().set("Saved." + s, null);
				}
		        sal.getCustomSaveConfig().set("Saved", null);
		        trainsSpawned = true;
			}
		}
		if(getConfig().contains("Settings.languageFile")){
			langFile = (String)getConfig().get("Settings.languageFile");
		}else{
			getConfig().set("Settings.languageFile", "LANG_EN");
			saveConfig();
			langFile = "LANG_EN";
			lm.generateLanguageFile(this);
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
			public void run(){
				runStartup();
			}
		}, 5L);
	}

	
	//Disable plugin
	@Override
	public void onDisable(){
	    Iterator<?> it = loop.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        Train t = (Train) pair.getKey();
	        Integer time = (Integer) pair.getValue();
	        sal.getCustomSaveConfig().set("Saved." + t.track.name + ".loopSeconds", time);
	        sal.getCustomSaveConfig().set("Saved." + t.track.name + ".cartOffset", t.cartOffset);
	        sal.getCustomSaveConfig().set("Saved." + t.track.name + ".minSpeed", t.minSpeed);
	        sal.getCustomSaveConfig().set("Saved." + t.track.name + ".maxSpeed", t.maxSpeed);
	        sal.getCustomSaveConfig().set("Saved." + t.track.name + ".trainLength", t.carts.size());
	        sal.getCustomSaveConfig().set("Saved." + t.track.name + ".hasLoco", t.hasLoco);
	        sal.getCustomSaveConfig().set("Saved." + t.track.name + ".isSmall", t.carts.get(0).holder.isSmall());
	        sal.getCustomSaveConfig().set("Saved." + t.track.name + ".trainName", t.trainName);
	        sal.saveCustomSaveConfig();
	        it.remove(); // avoids a ConcurrentModificationException
	    }
		for(ArmorStand ar : pointsVisible){
			ar.remove();
		}
		for(int i = 1; i < id + 1; i ++){
			Receiver r = receivers.get(i);
			setActive(r.name, false);
		}
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
	
	//Check if input string is a number
    public boolean checkMe(String s) {
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
    
    //Check if input string is a double
    public boolean checkMeb(String s) {
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
    
    //Get message from the language file
    public String getMessage(String path){
    	return ((String) sal.getCustomLangConfig(langFile).get(path)).replace("&", "§");
    }
    
    //On command
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		//Main check for /rc
		if(cmd.getName().equalsIgnoreCase("rc")){
			//Check if arguments are more then 0
			if(args.length > 0){
				//Check if first argument is spawntrain (/rc spawntrain)
				if(args[0].equalsIgnoreCase("spawntrain")){
					//Check if user has permissions
					if(sender.hasPermission("rollercoaster.spawntrain")) {
						//Check if command has the correct amount of arguments
						if(args.length == 9){
							String track = args[1];
							String train = args[2];
							//Check if all the arguments entered are valid
							if(checkMe(args[3]) && checkMe(args[4]) && checkMe(args[5]) && checkMe(args[6]) && checkMe(args[7]) && checkMe(args[8])) {
								if(getAPI().isTrack(track)) {
									if(getConfig().contains("Trains." + train + "cart")) {
										//Spawn in train using the given args
										Integer length = Integer.parseInt(args[3]);
										Integer loco = Integer.parseInt(args[4]);
										Integer small = Integer.parseInt(args[8]);
										Boolean tilt = false;
										Boolean hasLoc = false;
										Boolean isSmall = false;
										if(loco == 1)
											hasLoc = true;
										if(small == 1)
											isSmall = true;
										Train t = getAPI().spawnTrain(train, length, hasLoc, ((Player) sender).getLocation(), isSmall, getAPI().getTrack(track), Integer.parseInt(args[6]), Integer.parseInt(args[7]), Integer.parseInt(args[5]));
										t.track = getAPI().getTrackStorage(track);
										t.tilt = tilt;
										t.cartOffset = Integer.parseInt(args[5]);
										t.minSpeed = Integer.parseInt(args[6]);
										t.maxSpeed = Integer.parseInt(args[7]);
										trains.add(t);
										sender.sendMessage(getMessage("Message.trainSpawned"));
									}else {
										//Train is not valid
										sender.sendMessage(getMessage("Error.invalidTrain"));
									}	
								}else {
									//Track is not valid
									sender.sendMessage(getMessage("Error.invalidTrack"));
								}
							}else {
								//Wrong command usage
								sender.sendMessage(getMessage("Usage.spawnTrain"));
							}
						}else{
							//Wrong command usage
							sender.sendMessage(getMessage("Usage.spawnTrain"));
						}
					}else {
						//User does not have permissions
						sender.sendMessage(getMessage("Error.noPermissions"));
					}
				//Check if first argument is starttrain (/rc starttrain)
				}else if(args[0].equalsIgnoreCase("starttrain")){
					//Check if user has permissions
					if(sender.hasPermission("rollercoaster.starttrain")) {
						//Check if command entered has correct amount of arguments
						if(args.length == 2){
							String track = args[1];
							//Check if second argument is a track
							if(getAPI().isTrack(track)) {
								//Start the ride
								getAPI().startTrain(track);
								sender.sendMessage(getMessage("Message.trainStarted"));
							}else{
								//Entered track is not valid
								sender.sendMessage(getMessage("Error.invalidTrack"));
							}
						}else{
							//Entered command has wrong syntax
							sender.sendMessage(getMessage("Usage.startTrain"));
						}
					}else {
						//User does not have permissions
						sender.sendMessage(getMessage("Error.noPermissions"));
					}
				//BETA (used for the bezier curve tracks) IGNORE THIS
				}else if(args[0].equalsIgnoreCase("loadcoaster")){
					if(sender.hasPermission("rollercoaster.loadcoaster")) {
					if(args.length == 2){
						String track = args[1];
						try {
							loadCoaster(track, ((Player)sender).getWorld());
						} catch (NumberFormatException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}else{
						sender.sendMessage(getMessage("Usage.startTrain"));
					}
					}else {
						sender.sendMessage(getMessage("Error.noPermissions"));
					}
				//Check if first argument is loop (/rc loop)
				}else if(args[0].equalsIgnoreCase("loop")){
					//Check if user has permissions
					if(sender.hasPermission("rollercoaster.loop")) {
						//Check if command has correct amount of args
						if(args.length == 3){
							String track = args[1];
							//Check if given args are correct
							if(checkMe(args[2])){
								if(getAPI().isTrack(track)) {
									//Set the loop for the given train
									Train train = getAPI().getTrain(track);
									sender.sendMessage(getMessage("Message.loopSet").replace("%seconds%", args[2]).replace("%track%", track));
									loop.put(train, Integer.parseInt(args[2]));
								}else{
									//Entered track is not valid
									sender.sendMessage(getMessage("Error.invalidTrack"));
								}
							}else{
								//Command syntax is invalid
								sender.sendMessage(getMessage("Usage.loop"));
							}
						}else{
							//Command syntax is invalid
							sender.sendMessage(getMessage("Usage.loop"));
						}
					}else {
						//User does not have permissions
						sender.sendMessage(getMessage("Error.noPermissions"));
					}
				//Check if first argument is stoptrain (/rc stoptrain)
				}else if(args[0].equalsIgnoreCase("stoptrain")){
					//Check if user has permissions
					if(sender.hasPermission("rollercoaster.stoptrain")) {
						//Check if command has the right amount of args
						if(args.length == 2){
							String track = args[1];
							//Check if given args are correct
							if(getAPI().isTrack(track)) {
								//Stop the given train
								Train train = getAPI().getTrain(track);
								train.inStation = false;
								train.riding = false;
								TrainStopEvent event = new TrainStopEvent("TrainStopEvent", train);
								Bukkit.getServer().getPluginManager().callEvent(event);
								sender.sendMessage(getMessage("Message.trainStopped"));
							}else{
								//Given track is invalid
								sender.sendMessage(getMessage("Error.invalidTrack"));
							}
						}else{
							//Command has wrong syntax
							sender.sendMessage(getMessage("usage.stopTrain"));
						}
					}else {
						//User does not have permissions
						sender.sendMessage(getMessage("Error.noPermissions"));
					}
				}else if(args[0].equalsIgnoreCase("addpoint")){
					if(sender.hasPermission("rollercoaster.createtrack")) {
						if(CustomPathBuilder.vectorList.isEmpty()){
				    	CustomPathBuilder.addPoint(((Player) sender).getLocation());
				    	lastLoc = ((Player) sender).getLocation();
						ArmorStand ar = (ArmorStand) ((Player) sender).getLocation().getWorld().spawnEntity(((Player) sender).getLocation(), EntityType.ARMOR_STAND);
						ar.setGravity(false);
						ar.setCustomName("Point");
						pointsVisible.add(ar);
					}else{
						Location prevPoint = lastLoc;
						Location newLoc = ((Player) sender).getLocation();
	                    Vector vector = getDirectionBetweenLocations(prevPoint, newLoc);
	                    int i2 = 1;
	                    for (double i = 1; i <= prevPoint.distance(newLoc); i += 0.5) {
	                        vector.multiply(i);
	                        prevPoint.add(vector);
	                        if(i2 < 3){
	                        	i2 ++;
	                        }else{
	                        	CustomPathBuilder.addPoint(prevPoint.clone());
								ArmorStand ar = (ArmorStand) prevPoint.getWorld().spawnEntity(prevPoint, EntityType.ARMOR_STAND);
								lastLoc = prevPoint.clone();
								ar.setGravity(false);
								ar.setCustomName("Point");
								pointsVisible.add(ar);
								i2 = 0;
	                        }
							prevPoint.subtract(vector);
	                        vector.normalize();
	                    }
					}
					sender.sendMessage(getMessage("Message.addPoint"));
					}else {
						sender.sendMessage(getMessage("Error.noPermissions"));
					}
				}else if(args[0].equalsIgnoreCase("removepoint")){
					if(sender.hasPermission("rollercoaster.createtrack")) {
					CustomPathBuilder.removePoint();
					sender.sendMessage(getMessage("Message.removePoint"));
					}else {
						sender.sendMessage(getMessage("Error.noPermissions"));
					}
				}else if(args[0].equalsIgnoreCase("startpath")){
					if(sender.hasPermission("rollercoaster.createtrack")) {
					CustomPathBuilder.origin = ((Player)sender).getLocation();
					CustomPathBuilder.vectorList.clear();
					sender.sendMessage(getMessage("Message.pathCreated1"));
					sender.sendMessage(getMessage("Message.pathCreated2"));
					}else {
						sender.sendMessage(getMessage("Error.noPermissions"));
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
								sender.sendMessage(getMessage("Message.trainCreated"));
							}else{
								sender.sendMessage(getMessage("Usage.createTrain"));
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
									if(getAPI().isTrain(name)) {
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
															sender.sendMessage(getMessage("Message.trainEdited"));
														}else{
															sender.sendMessage(getMessage("Error.invalidSeatNumber"));
														}
												}else{
													sender.sendMessage(getMessage("Error.noNumber"));
												}
											}else{
												sender.sendMessage(getMessage("Error.noNumber"));
											}
										}else{
											sender.sendMessage(getMessage("Error.noNumber"));
										}
									}else{
										sender.sendMessage(getMessage("Error.invalidTrain"));
									}
								}else{
									sender.sendMessage(getMessage("Usage.trainChairPos"));
								}
							}
						}else if(args[1].equalsIgnoreCase("setskin")){
								if(args.length == 4){
									String name = args[2];
										String type = args[3];
										if(type.equalsIgnoreCase("Loco")) {
											if(getAPI().isTrain(name + "loco")) {
												getConfig().set("Trains."+name+"loco.skin.materialid", ((Player)sender).getInventory().getItemInMainHand().getTypeId());
												getConfig().set("Trains."+name+"loco.skin.materialdata", ((Player)sender).getInventory().getItemInMainHand().getDurability() + "");
												saveConfig();
												sender.sendMessage(getMessage("Message.trainEdited"));
											}else {
												sender.sendMessage(getMessage("Error.invalidTrain"));
											}
										}else 
											if(type.equalsIgnoreCase("Cart")) {
												if(getAPI().isTrain(name + "cart")) {
													getConfig().set("Trains."+name+"cart.skin.materialid", ((Player)sender).getInventory().getItemInMainHand().getTypeId());
													getConfig().set("Trains."+name+"cart.skin.materialdata", ((Player)sender).getInventory().getItemInMainHand().getDurability() + "");
													saveConfig();
													sender.sendMessage(getMessage("Message.trainEdited"));
												}else {
													sender.sendMessage(getMessage("Error.invalidTrain"));
												}
											}else {
												sender.sendMessage(getMessage("Error.locoCart"));
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
									sender.sendMessage(getMessage("Message.noTrains"));
								}
								
						}
					}else{
						sender.sendMessage("§8============================================");
						sender.sendMessage("§6/rc train list");
						sender.sendMessage("§6/rc train create");
						sender.sendMessage("§6/rc train chairs");
						sender.sendMessage("§6/rc train setskin");
						sender.sendMessage("§6/rc train setoffset");
						sender.sendMessage("§6/rc train settilt");
						sender.sendMessage("§8============================================");
					}
				}else {
					sender.sendMessage(getMessage("Error.noPermissions"));
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
					List<PathPoint> as = new ArrayList<PathPoint>();
					for (int i = 0; i <= path.getPathLenght(); i++) {
						Location loc = path.getPathPosition((double) i);
						loc.setPitch(0);
						loc.setYaw(0);
						as.add(new PathPoint(loc.getX(),loc.getY(),loc.getZ(),0D));
					}
					for(ArmorStand ar : pointsVisible){
						ar.remove();
					}
					t.locs = (ArrayList<PathPoint>) as;
					tracks.add(t);
					sal.saveTrack(t);
					sender.sendMessage(getMessage("Message.pathBuild"));
				}else{
					sender.sendMessage(getMessage("Usage.pathBuild"));
				}
				}else {
					sender.sendMessage(getMessage("Error.noPermissions"));
				}
			}else if(args[0].equalsIgnoreCase("removetrain")){
				if(sender.hasPermission("rollercoaster.removetrain")) {
				if(args.length == 2){
					String trainname = args[1];
					if(getAPI().getTrain(trainname) != null){
						Train t = getAPI().getTrain(trainname);
						for(Cart c : t.carts){
							c.holder.remove();
							for(Seat s : c.seats){
								s.holder.remove();
							}
						}
						trains.remove(t);
						sender.sendMessage(getMessage("Message.trainRemoved"));
					}
				}else{
					sender.sendMessage(getMessage("Usage.removeTrain"));
				}
				}else {
					sender.sendMessage(getMessage("Error.noPermissions"));
				}
			}else if(args[0].equalsIgnoreCase("tracklist")){
				if(sender.hasPermission("rollercoaster.tracklist")) {
				sender.sendMessage("§3Track List:");
 
				if(getConfig().contains("Tracks")) {
			    for(Track s : tracks){
			    	sender.sendMessage("§2" + s.name);
			    }
				}else {
					sender.sendMessage(getMessage("Message.noTracks"));
				}
				}else {
					sender.sendMessage(getMessage("Error.noPermissions"));
				}
			}else if(args[0].equalsIgnoreCase("locktrain")){
				if(sender.hasPermission("rollercoaster.lock")) {
				if(args.length == 2){
					String st = args[1];
					if(getAPI().getTrain(st) != null){
						Train t = getAPI().getTrain(st);
						t.locked = true;
						TrainLockEvent event = new TrainLockEvent("TrainLockEvent", t);
						Bukkit.getServer().getPluginManager().callEvent(event);
						for(Cart c : t.carts){
							for(Seat s : c.seats){
								s.locked = true;
							}
						}
						sender.sendMessage(getMessage("Message.trainLocked"));
					}
				}else{
					sender.sendMessage(getMessage("Usage.lockTrain"));
				}
				}else {
					sender.sendMessage(getMessage("Error.noPermissions"));
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
							Track t = getAPI().getTrack(track);
							tracks.add(t);
						}
					}
					sender.sendMessage(getMessage("Message.configReloaded"));
				}else {
					sender.sendMessage(getMessage("Error.noPermissions"));
				}
			}else if(args[0].equalsIgnoreCase("unlocktrain")){
				if(sender.hasPermission("rollercoaster.lock")) {
				if(args.length == 2){
					String st = args[1];
					if(getAPI().getTrain(st) != null){
						Train t = getAPI().getTrain(st);
						t.locked = false;
						TrainUnlockEvent event = new TrainUnlockEvent("TrainUnlockEvent", t);
						Bukkit.getServer().getPluginManager().callEvent(event);
						for(Cart c : t.carts){
							for(Seat s : c.seats){
								s.locked = false;
							}
						}
						sender.sendMessage(getMessage("Message.trainUnlocked"));
					}
				}else{
					sender.sendMessage(getMessage("Usage.unlockTrain"));
				}
				}else {
					sender.sendMessage(getMessage("Error.noPermissions"));
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
						sender.sendMessage("§3/rc removepoint");
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
						sender.sendMessage("§3/rc train chairs pos <name> <chair#> <left/right> <front/back> <up/down> <Loco/Cart>");
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
						sender.sendMessage("§3/rc removepoint");
						sender.sendMessage("§3/rc build <name>");
						sender.sendMessage("§3/rc help 2");
						sender.sendMessage("§2===============§8 RollerCoaster Help [1] §2===============");
					}
				}else{
					sender.sendMessage(getMessage("Usage.rcHelp"));
				}
			}else{
				sender.sendMessage("§2===============§8 RollerCoaster Help [1] §2===============");
				sender.sendMessage("§3/rc spawntrain <trainname> <traintype> <amount>");
				sender.sendMessage("§3/rc starttrain <trainname>");
				sender.sendMessage("§3/rc stoptrain <trainname>");
				sender.sendMessage("§3/rc startpath");
				sender.sendMessage("§3/rc addpoint");
				sender.sendMessage("§3/rc removepoint");
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
    Location lastLoc;
	Integer Offset = 0;
	float previous = 0;
	Integer Kantel = 0;
	
    Vector getDirectionBetweenLocations(Location Start, Location End) {
        Vector from = Start.toVector();
        Vector to = End.toVector();
        return to.subtract(from);
    }
    
    //API SECTION
    
    public API getAPI(){
    	API api = new API();
    	api.plugin = this;
    	return api;
    }
}
