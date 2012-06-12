package com.planet_ink.coffee_mud.Items.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/**
 * The interface for all common items, and as a base for RawMaterial, armor, weapons, etc.
 * @author Bo Zimmerman
 */
public interface Item extends Interactable, CMSavable, CMModifiable
{
	public static final Item[] dummyItemArray=new Item[0];
	public boolean damagable();
	public void setDamagable(boolean bool);
	public int wornOut();	//Start at 0 and go up to 10000. At 10000 destroy, at say 2000 or so it shouldn't really be usable anyways
	public void setWornOut(int worn);
	public int value();
	public int baseGoldValue();
	public void setBaseValue(int newValue);
	public int recursiveWeight();
	public String stackableName();
	public void setStackableName(String S);
	
	//public void setMiscText(String newMiscText);
	//public String text();
	public CMObject container();
	public void setContainer(CMObject E);
	//public int ridesNumber();
	public CMObject ride();
	public void setRide(CMObject R);
}
