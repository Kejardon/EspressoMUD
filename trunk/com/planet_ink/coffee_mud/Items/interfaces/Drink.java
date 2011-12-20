package com.planet_ink.coffee_mud.Items.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
public interface Drink extends Item
{
/*
	TODO eventually.
	Also a note: The internal WVector should be synchronized when handled, and only give out clones of itself, not its actual self.
	public WVector ingredients();
	public void removeIngredient(Resource type, boolean andNourishment);
	public void addIngredient(Resource type, int amount);
	public void emptyDrink();
*/
	public int capacity();
	public void setCapacity(int amount);
	public int nourishment();
	public void setNourishment(int amount);
	public int bite();
	public void setBite(int amount);
}
