package me.legofreak107.rollercoaster.libs;

import me.legofreak107.rollercoaster.Main;

public class LangFile {
	
	public void generateLanguageFile(Main plugin){
		plugin.getCustomLangConfig(plugin.langFile).set("Message.trainSpawned", "&2Train spawned!");
		plugin.getCustomLangConfig(plugin.langFile).set("Error.invalidTrain", "&cInvalid train type, try /rc train list for a list of trains.");
		plugin.getCustomLangConfig(plugin.langFile).set("Error.invalidTrack", "&cInvalid track type, try /rc tracklist for a list of tracks.");
		plugin.getCustomLangConfig(plugin.langFile).set("Usage.spawnTrain", "&cUsage: /rc spawntrain <trainname> <traintype> <cartamount> <haslocomotive> <cartoffset> <minspeed> <maxspeed> <small>");
		plugin.getCustomLangConfig(plugin.langFile).set("Error.noPermissions", "&cYou don't have permissions to excecute this command!");
		plugin.getCustomLangConfig(plugin.langFile).set("Message.trainStarted", "&2Train started!");
		plugin.getCustomLangConfig(plugin.langFile).set("Usage.startTrain", "&cUsage: /rc starttrain <trainname>");
		plugin.getCustomLangConfig(plugin.langFile).set("Error.noNumber", "&cThe argument you entered isn't a valid number!");
		plugin.getCustomLangConfig(plugin.langFile).set("Usage.loop", "&2Usage: /rc loop <trackname> <loop seconds>");
		plugin.getCustomLangConfig(plugin.langFile).set("Message.trainStopped", "&3Train stopped!");
		plugin.getCustomLangConfig(plugin.langFile).set("Usage.stopTrain", "&cUsage: /rc stoptrain <trainname>");
		plugin.getCustomLangConfig(plugin.langFile).set("Message.addPoint", "&2Point added!");
		plugin.getCustomLangConfig(plugin.langFile).set("Message.pathCreated1", "&2Started the creation of a new path!");
		plugin.getCustomLangConfig(plugin.langFile).set("Message.pathCreated2", "&2You can now add points to the path by typing /rc addpoint");
		plugin.getCustomLangConfig(plugin.langFile).set("Message.trainCreated", "&2Train created!");
		plugin.getCustomLangConfig(plugin.langFile).set("Message.trainEdited", "&2Train edited!");
		plugin.getCustomLangConfig(plugin.langFile).set("Usage.createTrain", "&cUsage: /rc train create <typename> <seatsloco> <seatscart>");
		plugin.getCustomLangConfig(plugin.langFile).set("Error.invalidSeatNumber", "&cThis is not a valid seat number, please type a valid number!");
		plugin.getCustomLangConfig(plugin.langFile).set("Usage.setSkin", "&cUsage: /rc train setskin <cart/loco>");
		plugin.getCustomLangConfig(plugin.langFile).set("Usage.trainChairPos", "&cUsage: /rc train chairs pos <name> <seatnumber> <lr> <fb> <cart/loco>");
		plugin.getCustomLangConfig(plugin.langFile).set("Error.locoCart", "&cInvalid args, use Loco/Cart instead!");
		plugin.getCustomLangConfig(plugin.langFile).set("Message.noTrains", "&2You don't have any saved trains!");
		plugin.getCustomLangConfig(plugin.langFile).set("Message.pathBuild", "&2Path build!");
		plugin.getCustomLangConfig(plugin.langFile).set("Message.removePoint", "&2Point removed!");
		plugin.getCustomLangConfig(plugin.langFile).set("Usage.pathBuild", "&cUsage: /rc build <trainname>");
		plugin.getCustomLangConfig(plugin.langFile).set("Message.trainRemoved", "&2Train removed!");
		plugin.getCustomLangConfig(plugin.langFile).set("Usage.removeTrain", "&cUsage: /rc removetrain <trainname>");
		plugin.getCustomLangConfig(plugin.langFile).set("Message.noTracks", "&2You don't have any saved tracks! Please create one first!");
		plugin.getCustomLangConfig(plugin.langFile).set("Message.trainLocked", "&2Train locked!");
		plugin.getCustomLangConfig(plugin.langFile).set("Usage.lockTrain", "&cUsage: /rc locktrain <trainname>");
		plugin.getCustomLangConfig(plugin.langFile).set("Message.configReloaded", "&2Config reloaded!");
		plugin.getCustomLangConfig(plugin.langFile).set("Message.trainUnlocked", "&2Train unlocked!");
		plugin.getCustomLangConfig(plugin.langFile).set("Usage.unlockTrain", "&cUsage: /rc unlocktrain <trainname>");
		plugin.getCustomLangConfig(plugin.langFile).set("Usage.rcHelp", "&cUsage: /rc help <page>");
		plugin.getCustomLangConfig(plugin.langFile).set("Message.loopSet", "&2Loop set to: %seconds% for: %track%");
		plugin.saveCustomLangConfig(plugin.langFile);
	}
	
}
