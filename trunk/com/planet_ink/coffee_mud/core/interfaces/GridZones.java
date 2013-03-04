package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/**
 * GridZones is a cross-object interface that applies both to Areas, and Locales.
 * It represents an area (or room) organized like a Grid.
 * @author Bo Zimmerman
 * Update by Kejardon. Revamped a few things, but this interface is currently unused by anything.
 */
//Generally unsupported file
public interface GridZones extends Environmental.EnvHolder
{
	public boolean isMyGridChild(Room loc);
	public Room getRandomGridChild();
	// Returns the XY coordinates of the given Room in int[]{x,y} format.
	// If not a subroom, return null
	public int[] getRoomXY(Room loc);
	public int xGridSize();
	public int yGridSize();
	public void setXGridSize(int x);
	public void setYGridSize(int y);
	public Room getGridChild(int x, int y);
}
