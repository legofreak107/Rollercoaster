package me.legofreak107.rollercoaster.libs;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

public class CustomPathBuilder {

	public static ArrayList<Location> vectorList = new ArrayList<Location>();
	public static Location origin;

	public CustomPathBuilder(Location vector) {
		origin = vector;
	}

	public static void addPoint(Location vector) {
		vectorList.add(vector.subtract(origin));
	}

	public static void removePoint() {
		if (!vectorList.isEmpty()) {
			vectorList.remove(vectorList.size() - 1);
		}
	}

	public static CustomPath build() {
		return new CustomPath(origin.clone(), vectorList);
	}

}
