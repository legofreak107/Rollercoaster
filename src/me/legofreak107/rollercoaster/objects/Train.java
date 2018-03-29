package me.legofreak107.rollercoaster.objects;

import java.util.ArrayList;

public class Train {

	public ArrayList<Cart> carts;
	public Cart loco;
	public Boolean riding;
	public Boolean inStation;
	public Boolean passedStation;
	public Integer speed;
	public Integer maxSpeed;
	public Integer minSpeed;
	public Track track;
	public Boolean locked = false;
	public Integer cartOffset;
	public Boolean tilt;
	public String trainName;
	public Boolean hasLoco;
	public Boolean autoSpeed;
	
}
