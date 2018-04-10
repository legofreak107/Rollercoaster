package me.legofreak107.rollercoaster.libs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Location;

public class CustomPath extends Moveable {

	private List<Location> preLocation = new ArrayList<Location>();
	private HashMap<Moveable, Double> childrenMap = new HashMap<Moveable, Double>();
	public Location[] pathPoints;

	private double speed = 0.25;
	int levelOfDetail = 30;
	boolean stopAtTheEnd = false;
	public CustomPath path = null;
	
	public CustomPath(Location vector, List<Location> vectorList) {
		setOrigin(vector);
		setPreLocation(vectorList);
		setType(MoveableType.PATH);
		generatePath();
	}

	public int getPathLenght() {
		return getPreLocation().size() * levelOfDetail;
	}

	public Location getPathPosition(Double double1) {
		double percent = double1 / (getPreLocation().size() * levelOfDetail);
		return Interp(pathPoints, percent).add(getOrigin());
	}

	public void generatePath() {
		Location[] suppliedPath = getPreLocation().toArray(new Location[getPreLocation().size()]);
		Location[] finalPath;
		finalPath = new Location[suppliedPath.length + 2];
		copyArray(suppliedPath, 0, finalPath, 1, suppliedPath.length);
		finalPath[0] = finalPath[1].clone().add(finalPath[1].clone().subtract(finalPath[2]));
		finalPath[finalPath.length - 1] = finalPath[finalPath.length - 2].clone()
				.add(finalPath[finalPath.length - 2].clone().subtract(finalPath[finalPath.length - 3]));
		pathPoints = finalPath;
	}

	private static Location Interp(Location[] pts, double t) {
		int numSections = pts.length - 3;
		int currPt = (int) Math.min(Math.floor(t * (double) numSections), numSections - 1);
		double u = t * (double) numSections - (double) currPt;

		Location a = pts[currPt];
		Location b = pts[currPt + 1];
		Location c = pts[currPt + 2];
		Location d = pts[currPt + 3];

		return a.clone().multiply(-1).add(b.clone().multiply(3f)).subtract(c.clone().multiply(3f)).add(d)
				.multiply(u * u * u)
				.add(a.clone().multiply(2f).subtract(b.clone().multiply(5f)).add(c.clone().multiply(4f)).subtract(d)
						.multiply(u * u))
				.add(a.clone().multiply(-1).add(c).multiply(u)).add(b.clone().multiply(2f)).multiply(0.5f);
	}

	private void copyArray(Location[] source, int a, Location[] dest, int b, int lenght) {
		for (int i = a; i < lenght; i++) {
			dest[b + i] = source[i];
		}
	}

	public void addChild(Moveable child) {
		getChildren().add(child);
		getChildrenMap().put(child, 0.0);
	}

	public void addChild(Moveable child, double progress) {
		while (progress > getPathLenght()) {
			progress -= getPathLenght();
		}
		getChildren().add(child);
		getChildrenMap().put(child, progress);
	}

	@Override
	public void moveTo(Location vector) {
		setOrigin(vector);
	}

	@Override
	public void runTick(boolean started) {
		if (started) {
			for (Moveable child : getChildrenMap().keySet()) {
				double progress = getChildrenMap().get(child) + getSpeed();
				if (progress > getPathLenght()) {
					if (stopAtTheEnd) {
						progress = getPathLenght();
					} else {
						progress -= getPathLenght();
					}
				}
				getChildrenMap().put(child, progress);
			}
		}
		for (Entry<Moveable, Double> child : getChildrenMap().entrySet()) {
			if (started) {
				child.getKey().moveTo(getPathPosition(child.getValue()));
			}
			child.getKey().runTick(started);
		}
	}

	public List<Location> getPreLocation() {
		return preLocation;
	}

	public void setPreLocation(List<Location> preLocation) {
		this.preLocation = preLocation;
	}

	public HashMap<Moveable, Double> getChildrenMap() {
		return childrenMap;
	}

	public void setChildrenMap(HashMap<Moveable, Double> childrenMap) {
		this.childrenMap = childrenMap;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}
}