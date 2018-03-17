package me.legofreak107.rollercoaster.libs;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

public abstract class Moveable {

	private static Location origin;
	private MoveableType type;

	private List<Moveable> children = new ArrayList<Moveable>();

	public abstract void moveTo(Location vector);

	public abstract void runTick(boolean started);

	public abstract void addChild(Moveable child);

	public static Location getOrigin() {
		return origin.clone();
	}

	public void setOrigin(Location origin) {
		this.origin = origin.clone();
	}

	public MoveableType getType() {
		return type;
	}

	public void setType(MoveableType type) {
		this.type = type;
	}

	public List<Moveable> getChildren() {
		return children;
	}

	public void setChildren(List<Moveable> children) {
		this.children = children;
	}

}
